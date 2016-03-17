package com.imagem.poc.poccensoagrodmc;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ctoledo on 17/03/2016.
 */
public class EnderecoAdapter extends ArrayAdapter<Endereco> {
    Context context;
    int layoutResourceId;
    Endereco data[] = null;

    public EnderecoAdapter(Context context, int layoutResourceId, Endereco[] data) {
        super(context, layoutResourceId, data);
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, null);
        }
/*
        Endereco endereco = data[position];
        TextView title = (TextView)row.findViewById(R.id.txtTitle);
        title.setText(endereco.getNome());
*/
        TextView title = (TextView)row.findViewById(R.id.txtTitle);
        title.setText("text");
        return row;
    }
}
