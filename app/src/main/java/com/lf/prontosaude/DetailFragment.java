package com.lf.prontosaude;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lf.prontosaude.MODEL.Estabelecimento;

import java.io.IOException;
import java.util.List;
import java.util.Locale;



/**
 * Created by ferna on 27/10/2016.
 */


public class DetailFragment extends SupportMapFragment implements OnMapReadyCallback {

    private Estabelecimento selecionado;
    private Double latEnd, lonEnd;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        selecionado = (Estabelecimento) getActivity().getIntent().getSerializableExtra("selecionado");

        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.frame);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap map) {

        Double lat, lon;

        if (selecionado.lon != null) {
            lat = Double.valueOf(selecionado.lat);
            lon = Double.valueOf(selecionado.lon);

        }else {
            String end = selecionado.bairro + ", " + selecionado.bairro + ", "
                    + selecionado.logradouro + ", " + selecionado.numero;

            getCoordenadasPorEndereco(end);


            lat = latEnd;
            lon = lonEnd;
        }

        LatLng local = new LatLng(lat, lon);
        map.addMarker(new MarkerOptions().position(local).title(selecionado.nomeFantasia));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(local,15));


    }

    @Override
    public void onResume() {
        super.onResume();
        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.frame);
        mapFragment.getMapAsync(this);

        
    }


    private void getCoordenadasPorEndereco(String textEndereco) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(textEndereco, 1);
            Address address = addresses.get(0);
            Log.i("GEO", address.toString());

            latEnd = address.getLatitude();
            lonEnd = address.getLongitude();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
