package com.lf.prontosaude;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.drakeet.materialdialog.MaterialDialog;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private EditText edtEndereco;
    private Button btnPorLocal;
    private Button btnPorEndereco;
    private Spinner spinnerCategorias;
    private SeekBar seekBarRadio;
    private TextView txtSeekBarStatus;
    private TextView bairroCard1;
    private TextView ufCard1;
    ProgressDialog progressDialog;
    private static final int REQUEST_PERMISSIONS_CODE = 128;
    private String itemSpinnerCategoria;
    private Double lat, lon, latGPS, lonGPS;
    private GoogleApiClient locationApiClient;
    private MaterialDialog mDialog;
    private Boolean goEstab = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        findViews();
        setTitle("Home");

        controlSeekBar();
        setSpinnerCategorias();
        setOnclickListners();
        prencherCampos();
        callConnection();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (progressDialog !=null) {
            if (progressDialog.isShowing()) {
                progressDialog.hide();
            }
        }
    }


    private void prencherCampos() {

        if (latGPS != null) {
            getEnderecoPorCoordenadas();
        }else {
            bairroCard1.setText("Localização");
            ufCard1.setText("indisponível!");
        }
    }

    private void findViews() {
        edtEndereco = (EditText) findViewById(R.id.edtEndereco);
        btnPorLocal = (Button) findViewById(R.id.btnLocal);
        btnPorEndereco = (Button) findViewById(R.id.btnEndereco);
        spinnerCategorias = (Spinner) findViewById(R.id.spinnerCategoria);
        seekBarRadio = (SeekBar) findViewById(R.id.seekBarR);
        txtSeekBarStatus = (TextView) findViewById(R.id.txtStatusSeekBar);
        bairroCard1 = (TextView) findViewById(R.id.txtHomeBairro);
        ufCard1 = (TextView) findViewById(R.id.txtHomeUF);
    }

    private void setOnclickListners() {
        btnPorEndereco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnPorEndereco.setClickable(false);
                btnPorEndereco.setPressed(true);

                StatusClass geo = new StatusClass(HomeActivity.this);
                Boolean getStatusGPS = geo.getStatusGPS();
                Boolean getStatusInternet = geo.getStatusInternet();
                if (getStatusInternet) {

                    if (getStatusGPS) {
                        String textEndereco = String.valueOf(edtEndereco.getText());
                        String txtRaio = getRaio();

                        if (textEndereco.equals("")) {

                            AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Alerta")
                                    .setMessage("Informe o endereço!")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", null)
                                    .show();

                        } else {

                            Boolean coordenadasOK = getCoordenadasPorEndereco(textEndereco);

                            if (coordenadasOK) {
                                if (itemSpinnerCategoria.equals("Selecione a categoria")) {

                                    Intent irEstabelecimentoPorEnd = new Intent(HomeActivity.this, EstabelecimentosActivity.class);
                                    irEstabelecimentoPorEnd.putExtra("lat", lat);
                                    irEstabelecimentoPorEnd.putExtra("lon", lon);
                                    irEstabelecimentoPorEnd.putExtra("raio", txtRaio);
                                    irEstabelecimentoPorEnd.putExtra("categoria", "");
                                    startActivity(irEstabelecimentoPorEnd);

                                } else {

                                    Intent irEstabelecimentoPorEnd = new Intent(HomeActivity.this, EstabelecimentosActivity.class);
                                    irEstabelecimentoPorEnd.putExtra("lat", lat);
                                    irEstabelecimentoPorEnd.putExtra("lon", lon);
                                    irEstabelecimentoPorEnd.putExtra("raio", txtRaio);
                                    irEstabelecimentoPorEnd.putExtra("categoria", itemSpinnerCategoria);
                                    startActivity(irEstabelecimentoPorEnd);
                                }
                            }
                        }
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                                .setMessage("Não foi possível capturar sua localização." +
                                        "Verifique se o GPS está ativo!")
                                .setPositiveButton("OK", null)
                                .show();
                    }
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                            .setMessage("Verifique sua conexão!")
                            .setPositiveButton("OK", null)
                            .show();
                }
                btnPorEndereco.setClickable(true);
                btnPorEndereco.setPressed(false);
            }
        });


        btnPorLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnPorLocal.setClickable(false);
                btnPorLocal.setPressed(true);

                goEstab = true;

                callConnection();

                btnPorLocal.setClickable(true);
                btnPorLocal.setPressed(false);


            }
        });
    }

    private void setSpinnerCategorias() {
        List<String> categorias = new ArrayList<String>();

        categorias.add("Selecione a categoria");
        categorias.add("HOSPITAL");
        categorias.add("POSTO DE SAÚDE");
        categorias.add("URGÊNCIA");
        categorias.add("SAMU");
        categorias.add("FARMÁCIA");
        categorias.add("CLÍNICA");
        categorias.add("CONSULTÓRIO");
        categorias.add("LABORATÓRIO");
        categorias.add("APOIO À SAÚDE");
        categorias.add("ATENÇÃO ESPECÍFICA");
        categorias.add("UNIDADE ADMINISTRATIVA");
        categorias.add("ATENDIMENTO DOMICILIAR");

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categorias);

        spinnerCategorias.setAdapter((SpinnerAdapter) adapter);

        spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemSpinnerCategoria = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void controlSeekBar() {
        txtSeekBarStatus.setText( seekBarRadio.getProgress() + "/" + seekBarRadio.getMax());

        seekBarRadio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtSeekBarStatus.setText(progress + "/" + seekBarRadio.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private Boolean getCoordenadasPorEndereco(String textEndereco) {

        boolean retorno = false;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(textEndereco, 2);
            Log.i("TXT: ", addresses.toString());
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                Log.i("GEO", address.toString());

                lat = address.getLatitude();
                lon = address.getLongitude();
                retorno = true;
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("Não foi possível localizar estabelecimentos no endereço informado!")
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .show();

                retorno = false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return retorno;
    }

    private String getRaio() {
        String txtRaio;

        if (seekBarRadio.getProgress() <= 0) {

            txtRaio = "10";
        } else {
            txtRaio = String.valueOf(seekBarRadio.getProgress());
        }
        return txtRaio;
    }

    private void goEstabelecimentosGPS() {
        StatusClass geo = new StatusClass(HomeActivity.this);
        Boolean getStatusGPS = geo.getStatusGPS();
        Boolean getStatusInternet = geo.getStatusInternet();

        if (getStatusGPS) {
            if (getStatusInternet) {
                Intent irEstabelecimento = new Intent(HomeActivity.this, EstabelecimentosActivity.class);
                irEstabelecimento.putExtra("lat", latGPS);
                irEstabelecimento.putExtra("lon", lonGPS);
                startActivity(irEstabelecimento);
            } else {
                AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                        .setMessage("Verifique sua conexão!")
                        .setPositiveButton("OK", null)
                        .show();
            }
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                    .setMessage("Não foi possível capturar sua localização." +
                            "Verifique se o GPS está ativo!")
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private void getEnderecoPorCoordenadas() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latGPS,lonGPS, 1);
            Address address = addresses.get(0);

                bairroCard1.setText(address.getAddressLine(0));
                ufCard1.setText(address.getAddressLine(1));



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void callDialog(final String msg, final String[] permissions) {

        mDialog = new MaterialDialog(this)
                .setTitle("Permission")
                .setMessage(msg)
                .setPositiveButton(" OK ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(HomeActivity.this, permissions, REQUEST_PERMISSIONS_CODE);
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



    private synchronized void callConnection() {

        progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setMessage("Aguarde...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StatusClass geo = new StatusClass(HomeActivity.this);
        Boolean getStatusGPS = geo.getStatusGPS();
        Boolean getStatusInternet = geo.getStatusInternet();
        if (getStatusInternet) {

            if (getStatusGPS) {

                locationApiClient = new GoogleApiClient.Builder(this)
                        .addOnConnectionFailedListener(this)
                        .addConnectionCallbacks(this)
                        .addApi(LocationServices.API)
                        .build();
                locationApiClient.connect();
            }else {
                AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Alerta")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage("Não foi possível capturar sua localização." +
                                "Verifique se o GPS está ativo!")
                        .setPositiveButton("OK", null)
                        .show();
                progressDialog.hide();
            }
        }else {
            AlertDialog alertDialog = new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Alerta")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Verifique sua conexão!")
                    .setPositiveButton("OK", null)
                    .show();
            progressDialog.hide();
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            callAcessLocation();
            progressDialog.hide();
        }
        Location l = LocationServices.FusedLocationApi.getLastLocation(locationApiClient);

                if (l != null) {

                    latGPS = l.getLatitude();
                    lonGPS = l.getLongitude();

                    prencherCampos();

                    progressDialog.hide();
                    if (goEstab == true) {
                        goEstabelecimentosGPS();
                    }

                    goEstab = false;

                }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
