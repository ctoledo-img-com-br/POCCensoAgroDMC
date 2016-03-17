package com.imagem.poc.poccensoagrodmc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.runtime.ArcGISRuntime;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvEnderecos;
    public FrameLayout mViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArcGISRuntime.setClientId("gZK3c64UFVTUmPcI");

        MapView mapView;

        String basemapPath = "/storage/extSdCard/basemap/basemap.tpk";
        ArcGISLocalTiledLayer local = new ArcGISLocalTiledLayer(basemapPath);
        mapView = new MapView(MainActivity.this, local.getSpatialReference(),local.getFullExtent());
        mapView.addLayer(local);

        mViewContainer = (FrameLayout) findViewById(R.id.main_activity_view_container);
        mViewContainer.addView(mapView);

        lvEnderecos = (ListView) findViewById(R.id.listViewEnderecos);
        List<String> alEnderecos = new ArrayList<String>();
        alEnderecos.add("Endereço 1");
        alEnderecos.add("Endereço 2");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                alEnderecos );

        lvEnderecos.setAdapter(arrayAdapter);
        
    }
}
