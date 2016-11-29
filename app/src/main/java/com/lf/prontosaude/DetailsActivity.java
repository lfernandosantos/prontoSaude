package com.lf.prontosaude;

import android.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lf.prontosaude.MODEL.Estabelecimento;

import java.util.ArrayList;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

public class DetailsActivity extends AppCompatActivity {


    private Estabelecimento selecionado;
    private TextView nomeFantasia;
    private TextView categoria;
    private TextView cnpj;
    private TextView atendimento;
    private FloatingActionButton fabCall;
    private List<String> strListaServicos;
    private TextView endereco;
    private TextView endereco2;
    private TextView cep;
    private Button btnVerNoMapa;
    private Button btnVerServicos;
    private MaterialDialog mDialog;
    private static final int REQUEST_PERMISSIONS_CODE = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        selecionado = (Estabelecimento) getIntent().getSerializableExtra("selecionado");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame, new DetailFragment());
        transaction.commit();

        findViews();
        preencheCampos();
    }


    private void findViews() {
        View layout = findViewById(R.id.include);
        nomeFantasia = (TextView) layout.findViewById(R.id.textViewNome);
        categoria = (TextView) layout.findViewById(R.id.tCategoria);
        cnpj = (TextView) layout.findViewById(R.id.tCNPJ);
        atendimento = (TextView) layout.findViewById(R.id.tAtendimento);
        fabCall = (FloatingActionButton) layout.findViewById(R.id.fabCall);
        endereco = (TextView) layout.findViewById(R.id.tEndereco);
        endereco2 = (TextView) layout.findViewById(R.id.tEndereco2);
        cep = (TextView) layout.findViewById(R.id.tCEP);
        btnVerNoMapa = (Button) layout.findViewById(R.id.btnVerMapa);
        btnVerServicos = (Button) layout.findViewById(R.id.btnVerServicos);
    }

    private void preencheCampos() {
        nomeFantasia.setText(selecionado.nomeFantasia);
        categoria.setText(selecionado.categoriaUnidade);
        atendimento.setText(selecionado.turnoAtendimento);
        endereco.setText(selecionado.logradouro + ", "
                +selecionado.numero + ", " + selecionado.bairro );
        endereco2.setText(selecionado.cidade + "-"+ selecionado.uf);
        cep.setText("CEP: " + selecionado.cep);

//        if (selecionado.cnpj != null) {
//            if (Double.valueOf(selecionado.cnpj) <= 0.0) {
//                cnpj.setVisibility(View.INVISIBLE);
//                lblCNPJ.setVisibility(View.INVISIBLE);
//            } else {
        cnpj.setText("CNPJ: :" + selecionado.cnpj);
//            }
//        }

        if (selecionado.telefone != null) {
//            if (Double.valueOf(selecionado.telefone) <= 0.0) {
//                telefone.setVisibility(View.INVISIBLE);
//                imgTel.setVisibility(View.INVISIBLE);
//            }else {

            fabCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle("TELEFONE");
                    builder.setMessage("Ligar para " + selecionado.telefone + " ?");
                    builder.setPositiveButton("LIGAR", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri uri = Uri.parse("tel:" + selecionado.telefone);
                            Intent ligarPara = new Intent(Intent.ACTION_CALL, uri);

                            if (ActivityCompat.checkSelfPermission(DetailsActivity.this,
                                    android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                callAcessLocation();
                            }else {
                                startActivity(ligarPara);
                            }
                        }
                    });
                    builder.setNegativeButton("CANCELAR", null);
                    builder.create();
                    builder.show();
                }
            });

//            }
        }else {
            fabCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailsActivity.this);
                    builder.setTitle("SEM TELEFONE");
                    builder.setMessage("Unidade não possui telefone!");
                    builder.create();
                    builder.show();
                }
            });
        }

        btnVerServicos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                carregaListaServicos();
            }
        });

        btnVerNoMapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selecionado.lon.equals("")){
                    Toast.makeText(DetailsActivity.this, "sem local", Toast.LENGTH_LONG).show();
                }else {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+selecionado.lat+","+selecionado.lon);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            }
        });



    }

    private void carregaListaServicos(){
        LayoutInflater layoutInflater = getLayoutInflater();

        View view = layoutInflater.inflate(R.layout.layout_dialog_servicos_estab, null);

        ListView listaServicos = (ListView) view.findViewById(R.id.listView_servicos_dialog);

        strListaServicos = getServicos();
        if (strListaServicos != null) {
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, strListaServicos);
            listaServicos.setAdapter(adapter);
        }else {
            String[] strElse = {"Não possui!"};
            ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, strElse);
            listaServicos.setAdapter(adapter);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SERVIÇOS");
        builder.setView(view);
        builder.create();
        builder.show();
    }

    private List<String> getServicos(){
        List<String> listGetServicos = new ArrayList<>();
        if (selecionado.temAtendimentoUrgencia.equals("Sim")){
            listGetServicos.add("Atendimento Urgencia");
        }
        if (selecionado.temAtendimentoAmbulatorial.equals("Sim")){
            listGetServicos.add("Atendimento Ambulatorial");
        }
        if (selecionado.temCentroCirurgico.equals("Sim")){
            listGetServicos.add("Centro Cirurgico");
        }
        if (selecionado.temObstetra.equals("Sim")){
            listGetServicos.add("Obstetra");
        }
        if (selecionado.temNeoNatal.equals("Sim")){
            listGetServicos.add("Neo Natal");
        }
        if (selecionado.temDialise.equals("Sim")){
            listGetServicos.add("Dialise");
        }

        return listGetServicos;
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
                        ActivityCompat.requestPermissions(DetailsActivity.this, permissions, REQUEST_PERMISSIONS_CODE);
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
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CALL_PHONE)) {

                callDialog("É necessário habilitar as permissões para utilizar o App.", new String[]{android.Manifest.permission.CALL_PHONE});
            } else {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, REQUEST_PERMISSIONS_CODE);
            }
        }
    }
}
