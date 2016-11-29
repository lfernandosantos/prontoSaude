package com.lf.prontosaude.JSON;

import android.util.Log;

import com.lf.prontosaude.MODEL.Estabelecimento;

import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ferna on 13/10/2016.
 */

public class BuscaJSON {

    private void buscaEstabelecimentoJsonLocalizacao(final List<Estabelecimento> listaEstabelecimentos) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IEstabelecimentoJSON.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        IEstabelecimentoJSON jsonEstabelecimentos = retrofit.create(IEstabelecimentoJSON.class);

        String lat = "-22.7675157";
        String lon = "-43.4258355";
        String raio = "10";
        Call<List<Estabelecimento>> listaRequest = jsonEstabelecimentos.listaEstabLatLong(lat, lon, raio);

        listaRequest.enqueue(new Callback<List<Estabelecimento>>() {
            @Override
            public void onResponse(Call<List<Estabelecimento>> call, Response<List<Estabelecimento>> response) {

                if (!response.isSuccessful()){
                    Log.i("TAG", "ERRO: " + response.code());
                }else {

                    List<Estabelecimento> estabs = response.body();

                    for (Estabelecimento estabelecimento : estabs){

                        listaEstabelecimentos.add(estabelecimento);
                        Log.i("TAG", "Estabs: " +estabelecimento.nomeFantasia);
                    }

                }
            }

            @Override
            public void onFailure(Call<List<Estabelecimento>> call, Throwable t) {

                Log.i("TAG", "ERRO: " +t);
            }
        });
    }
}
