package com.lf.prontosaude;

import android.*;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.lf.prontosaude.JSON.IEstabelecimentoJSON;
import com.lf.prontosaude.MODEL.Estabelecimento;
import com.lf.prontosaude.adapter.AdapterListaEstabelecimentos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import me.drakeet.materialdialog.MaterialDialog;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EstabelecimentosActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CODE = 128;
    private ListView listViewEstabs;
    private Spinner spinner;
    public List<Estabelecimento> listaView;
    ProgressDialog progressDialog;
    private MaterialDialog mDialog;
    private Double latDevice;
    private Double lonDevice;
    private String strRaio;
    private String categoriaSelecionada;
    private int tentativasJSON = 0;
    private Boolean semCategoria;
    private TextView endrecoCapturado;
    private TextView txtTotalResultados;
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estabelecimentos);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        callAcessLocation();
        latDevice = (Double) getIntent().getSerializableExtra("lat");
        lonDevice = (Double) getIntent().getSerializableExtra("lon");
        categoriaSelecionada = getIntent().getStringExtra("categoria");
        strRaio = getIntent().getStringExtra("raio");

        progressDialog = new ProgressDialog(this);
        listaView = new ArrayList<>();

        spinner = (Spinner) findViewById(R.id.spinner2);
        endrecoCapturado = (TextView) findViewById(R.id.txtEndereco);
        listViewEstabs = (ListView) findViewById(R.id.listaEstabelecimentos);
        txtTotalResultados = (TextView) findViewById(R.id.txtTotalBusca);

        carregaSpinnerCategorias();
        carregaListaEstabelecimentosJSON();

        listViewEstabs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Estabelecimento estabelecimentoSelecionado = (Estabelecimento) parent.getItemAtPosition(position);

                Intent irEstabelecimentoDetail = new Intent(EstabelecimentosActivity.this, DetailsActivity.class);
                irEstabelecimentoDetail.putExtra("selecionado", estabelecimentoSelecionado);
                startActivity(irEstabelecimentoDetail);
            }
        });

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private OkHttpClient getRequestHeader() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        return okHttpClient;
    }

    private void carregaSpinnerCategorias() {
        List<String> categorias = new ArrayList<String>();

        Boolean statusCategoria = verificaCategoria();

        if (statusCategoria){
            categorias.add(categoriaSelecionada);
            ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorias);
            spinner.setAdapter(adapter);
            spinner.setEnabled(false);
        }else {
            spinner.setEnabled(false);
        }
    }

    private void getEnderecoPorCoordenadas(Double latitude, Double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1);
            Address address = addresses.get(0);


            if (address.getAddressLine(3)!=null) {
                endrecoCapturado.setText(address.getAddressLine(1) + ", " +
                        address.getAddressLine(2) + ", " + address.getAddressLine(3));
            }else {
                endrecoCapturado.setText(address.getAddressLine(1) + ", " +
                        address.getAddressLine(2));
            }

            setTitle("Busca: " +address.getAddressLine(1));


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Boolean verificaCategoria() {
        if (categoriaSelecionada == null){
            semCategoria = false;

        }else {
            if (categoriaSelecionada.equals("")) {
                semCategoria = false;

            } else {
                semCategoria = true;

            }
        }

        return semCategoria;
    }

    private void carregaListaEstabelecimentosJSON() {
        StatusClass geo = new StatusClass(EstabelecimentosActivity.this);
        Boolean getStatusGPS = geo.getStatusGPS();
        Boolean getStatusInternet = geo.getStatusInternet();

        if(latDevice != null && lonDevice != null){

            getEnderecoPorCoordenadas(latDevice, lonDevice);

            if (getStatusInternet) {
                if (getStatusGPS) {
                    progressDialog.setMessage("Localizando...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Boolean statusCategoria = verificaCategoria();

                    if (statusCategoria){
                        buscaEstabelecimentoJsonComCategoria();
                    }else {
                        buscaEstabelecimentoJsonLocalizacao();
                    }
                    carregaListaEstabelecimentos(listaView);

                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(EstabelecimentosActivity.this)
                            .setMessage("Não foi possível capturar sua localização." +
                                    "Verifique se o GPS está ativo!")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }else {
                AlertDialog alertDialog = new AlertDialog.Builder(EstabelecimentosActivity.this)
                        .setMessage("Verifique sua conexão!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(EstabelecimentosActivity.this)
                    .setMessage("Não foi possível capturar sua localização." +
                            "Verifique sua conexão com o GPS com a internet!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void carregaListaEstabelecimentos(List<Estabelecimento> listaEstabelecimentos) {


        AdapterListaEstabelecimentos adapterListaEstabelecimentos = new AdapterListaEstabelecimentos(this, listaEstabelecimentos);
        listViewEstabs.setAdapter(adapterListaEstabelecimentos);

        txtTotalResultados.setText(String.valueOf(listaEstabelecimentos.size() + " Resultados"));
    }

    private void buscaEstabelecimentoJsonLocalizacao() {

        OkHttpClient okHttpClient = getRequestHeader();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IEstabelecimentoJSON.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        IEstabelecimentoJSON jsonEstabelecimentos = retrofit.create(IEstabelecimentoJSON.class);

        String lat = String.valueOf(latDevice);
        String lon = String.valueOf(lonDevice);
        String raio = "10";
        Call<List<Estabelecimento>> listaRequest = jsonEstabelecimentos.listaEstabLatLong(lat, lon, raio);

        listaRequest.enqueue(new Callback<List<Estabelecimento>>() {
            @Override
            public void onResponse(Call<List<Estabelecimento>> call, Response<List<Estabelecimento>> response) {

                if (!response.isSuccessful()) {
                    Log.i("TAG", "ERRO: " + response.code());
                } else {

                    List<Estabelecimento> estabs = response.body();

                    for (Estabelecimento estabelecimento : estabs) {

                        listaView.add(estabelecimento);
                        Log.i("TAG", "Estabs: " + estabelecimento.nomeFantasia);
                    }

                    carregaListaEstabelecimentos(listaView);

                    progressDialog.hide();
                }
            }

            @Override
            public void onFailure(Call<List<Estabelecimento>> call, Throwable t) {
                if (tentativasJSON >= 1){
                    AlertDialog alertDialog = new AlertDialog.Builder(EstabelecimentosActivity.this)
                            .setMessage("Não foi possível realizar a busca!" +
                                    "Verifique sua conexão ou tente mais tarde.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                    tentativasJSON = 0;
                }else {
                    tentativasJSON = 1;

                    carregaListaEstabelecimentosJSON();

                    Log.i("TAG", "ERRO: " + t);
                }

            }
        });
    }

    private void buscaEstabelecimentoJsonComCategoria() {
        OkHttpClient okHttpClient = getRequestHeader();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IEstabelecimentoJSON.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        IEstabelecimentoJSON jsonEstabelecimentos = retrofit.create(IEstabelecimentoJSON.class);

        String lat = String.valueOf(latDevice);
        String lon = String.valueOf(lonDevice);
        String raio;
        if (strRaio != null && Integer.valueOf(strRaio) > 0) {
            raio =  strRaio;
        }else {
            raio = "10";
        }
        String categoria = categoriaSelecionada;

        Call<List<Estabelecimento>> listaRequest = jsonEstabelecimentos.listaEstabPorCategoria(lat, lon, raio ,categoria);
        listaRequest.enqueue(new Callback<List<Estabelecimento>>() {
            @Override
            public void onResponse(Call<List<Estabelecimento>> call, Response<List<Estabelecimento>> response) {

                if (!response.isSuccessful()) {
                    Log.i("TAG", "ERRO: " + response.code());
                } else {

                    List<Estabelecimento> estabs = response.body();

                    for (Estabelecimento estabelecimento : estabs) {

                        listaView.add(estabelecimento);
                        Log.i("TAG", "Estabs: " + estabelecimento.nomeFantasia);
                    }

                    carregaListaEstabelecimentos(listaView);

                    progressDialog.hide();
                }
            }

            @Override
            public void onFailure(Call<List<Estabelecimento>> call, Throwable t) {
                if (tentativasJSON >= 1){
                    AlertDialog alertDialog = new AlertDialog.Builder(EstabelecimentosActivity.this)
                            .setMessage("Não foi possível realizar a busca!" +
                                    "Verifique sua conexão ou tente mais tarde.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();
                    tentativasJSON = 0;
                }else {
                    tentativasJSON = 1;

                    carregaListaEstabelecimentosJSON();

                    Log.i("TAG", "ERRO: " + t);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void callDialog(final String msg, final String[] permissions) {

        mDialog = new MaterialDialog(this)
                .setTitle("Permission")
                .setMessage(msg)
                .setPositiveButton(" OK ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(EstabelecimentosActivity.this, permissions, REQUEST_PERMISSIONS_CODE);
                        mDialog.dismiss();
                    }
                })
                .setNegativeButton(" Cancel ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

        mDialog.show();
    }

    public void callAcessLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                callDialog("É necessário habilitar as permissões para utilizar o App.", new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION});
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE);
            }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Estabelecimentos Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }


}
