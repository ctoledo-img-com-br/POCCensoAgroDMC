package com.imagem.poc.poccensoagrodmc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.esri.core.map.Graphic;

/**
 * Created by Wlima on 29/03/2016.
 */

public class GraphicsAdapter extends ArrayAdapter<Object>{

    Object[] data;
    Context context;



    public GraphicsAdapter(Context context,int resourceId, Object[] objects) {
        super(context, resourceId,objects);

        this.data = objects;
        this.context = context;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(android.R.layout.simple_dropdown_item_1line, null);
        }

        Graphic graphic = (Graphic)data[position];
        TextView title = (TextView)row.findViewById(android.R.id.text1);
        title.setText(graphic.getAttributes().get("NOME").toString());
        return row;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        Graphic graphic = (Graphic)this.data[position];
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setText(graphic.getAttributes().get("NOME").toString());

        return label;
    }
}
