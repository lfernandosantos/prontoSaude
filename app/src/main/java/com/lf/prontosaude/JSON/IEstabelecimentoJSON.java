package com.lf.prontosaude.JSON;

import com.lf.prontosaude.MODEL.Estabelecimento;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by ferna on 26/09/2016.
 */

public interface IEstabelecimentoJSON {



    String BASE_URL = "http://mobile-aceite.tcu.gov.br/mapa-da-saude/rest/";

    @GET("estabelecimentos/latitude/-22.7675157/longitude/-43.4258355/raio/10")
    Call<List<Estabelecimento>> listaEstabelecimentos();

    @GET("estabelecimentos/latitude/{lat}/longitude/{lon}/raio/{raio}")
    Call<List<Estabelecimento>> listaEstabLatLong(@Path("lat") String lat, @Path("lon") String lon, @Path("raio") String raio);

    @GET("estabelecimentos/latitude/{lat}/longitude/{lon}/raio/{raio}")
    Call<List<Estabelecimento>> listaEstabPorCategoria(@Path("lat") String lat, @Path("lon") String lon,
                                                       @Path("raio") String raio, @Query("categoria") String categoria);

}
