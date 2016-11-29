package com.lf.prontosaude.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lf.prontosaude.MODEL.Estabelecimento;
import com.lf.prontosaude.R;

import java.util.List;




public class AdapterListaEstabelecimentos extends BaseAdapter {

    private Activity activity;
    private final List<Estabelecimento> estabelecimentos;


    public AdapterListaEstabelecimentos(Activity activity, List<Estabelecimento> estabelecimentos){

        this.activity = activity;
        this.estabelecimentos = estabelecimentos;

    }
    @Override
    public int getCount() {
        return estabelecimentos.size();
    }

    @Override
    public Object getItem(int position) {
        return estabelecimentos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(estabelecimentos.get(position).codUnidade);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = activity.getLayoutInflater().inflate(R.layout.adapter_lista_estabelecimento, parent,false);

        Estabelecimento estabelecimento = estabelecimentos.get(position);

        TextView txtNome = (TextView) view.findViewById(R.id.txtNomeEstabAdapter);
        TextView txtCategoria = (TextView) view.findViewById(R.id.txtCatEstabAdapter);
        TextView txtEsfera = (TextView) view.findViewById(R.id.txtEsferaAdmAdapter);
        TextView txtEndereco = (TextView) view.findViewById(R.id.txtEndAdapter);

        txtNome.setText(estabelecimento.nomeFantasia);
        txtCategoria.setText(estabelecimento.categoriaUnidade + "  -");
        txtEsfera.setText(estabelecimento.esferaAdministrativa);
        txtEndereco.setText(estabelecimento.logradouro + ", " + estabelecimento.bairro);

        return view;
    }
}
