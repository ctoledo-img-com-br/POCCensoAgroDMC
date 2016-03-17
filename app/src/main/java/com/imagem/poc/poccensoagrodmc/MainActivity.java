package com.imagem.poc.poccensoagrodmc;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView lvEnderecos;
    public FrameLayout mViewContainer;
    private Point p1;
    private Point p2;
    final private double selectionScale = 18055.954822;
    final private String basemapPath = "/storage/extSdCard/basemap/basemap.tpk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArcGISRuntime.setClientId("gZK3c64UFVTUmPcI");

        final MapView mapView;

        ArcGISLocalTiledLayer local = new ArcGISLocalTiledLayer(basemapPath);
        mapView = new MapView(MainActivity.this, local.getSpatialReference(),local.getFullExtent());
        mapView.addLayer(local);

        final Endereco enderecos[] = new Endereco[]
                {
                        new Endereco("Praça da República", new Point(-4807724.667,-2620682.984)),
                        new Endereco("Aeroporto SDU", new Point(-4804787.787,-2621778.362))
                };

        // Cria e adiciona um GraphicsLayer
        GraphicsLayer graphicsLayer = new GraphicsLayer();
        mapView.addLayer(graphicsLayer);
        SimpleMarkerSymbol simpleMarker = new SimpleMarkerSymbol(
                Color.YELLOW, 16, SimpleMarkerSymbol.STYLE.CIRCLE);
        for (Endereco e: enderecos) {
            graphicsLayer.addGraphic(new Graphic(e.getPoint(), simpleMarker));
        }


        mViewContainer = (FrameLayout) findViewById(R.id.main_activity_view_container);
        mViewContainer.addView(mapView);

        lvEnderecos = (ListView) findViewById(R.id.listViewEnderecos);
        List<String> alEnderecos = new ArrayList<String>();
        for (Endereco e: enderecos) {
            alEnderecos.add(e.getNome());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                alEnderecos );
/*
        EnderecoAdapter adapter = new EnderecoAdapter(
                this,
                R.layout.listview_item_row,
                enderecos
        );
*/
        lvEnderecos.setAdapter(adapter);

        lvEnderecos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                //Endereco e = (Endereco) parent.getItemAtPosition(position);
                //mapView.zoomToScale(e.getPoint(), selectionScale);

                mapView.zoomToScale(enderecos[position].getPoint(), selectionScale);
            }

        });
        
    }
}
