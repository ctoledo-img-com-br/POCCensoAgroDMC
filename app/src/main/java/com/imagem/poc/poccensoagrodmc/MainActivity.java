package com.imagem.poc.poccensoagrodmc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.popup.Popup;
import com.esri.android.map.popup.PopupContainer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.android.toolkit.map.MapViewHelper;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.map.popup.PopupInfo;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.query.QueryParameters;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    public FrameLayout mViewContainer;

    final private double selectionScale = 18055.954822;
    final private double locationScale = 9027.977411;
    MapView mapView = null;
    Envelope initialExtent = null;


    final private String basemapPath = "/storage/extSdCard/data/RiodeJaneiro/basemap/basemap.tpk";
    final private String gdbPath = "/storage/extSdCard/data/RiodeJaneiro/gdb/poc.geodatabase";
    final private String shpPath = "/storage/extSdCard/data/RiodeJaneiro/shp/";
    final private String csvPath = "/storage/extSdCard/data/RiodeJaneiro/csv/endereco.csv";


    private List<GeodatabaseFeatureTable> gdbTables = null;
    private GraphicsLayer locationLayer = null;
    private Endereco[] enderecos = null;

    LocationManager locationManager = null;
    LocationListener locationListener = null;

    PopupContainer popupContainer;

    private Measure myMeasure;
    private Measure getMeasure(){
        if(myMeasure == null)
            myMeasure = new Measure(this,mapView);

        return myMeasure;
    }


    MapViewHelper mapViewHelper;
    final GraphicsLayer graphicsLayer = new GraphicsLayer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);



        ArcGISRuntime.setClientId("gZK3c64UFVTUmPcI");


        graphicsLayer.setName(getResources().getString(R.string.enderecos));

        ArcGISLocalTiledLayer basemap = new ArcGISLocalTiledLayer(basemapPath);
        basemap.setName(getResources().getString(R.string.mapabase));
        initialExtent = basemap.getFullExtent();
        FeatureLayer layerShpPonto = null;
        FeatureLayer layerShpLinha = null;
        locationLayer = new GraphicsLayer();

        try {
            Geodatabase localGdb = new Geodatabase(gdbPath);
            gdbTables = localGdb.getGeodatabaseTables();

            ShapefileFeatureTable shpPonto = new ShapefileFeatureTable(
                    shpPath + "VLT_Estacoes.shp");
            layerShpPonto = new FeatureLayer(shpPonto);
            layerShpPonto.setRenderer(
                    new SimpleRenderer(
                            new SimpleMarkerSymbol(
                                    Color.WHITE, 10, SimpleMarkerSymbol.STYLE.TRIANGLE)));
            layerShpPonto.setName(getResources().getString(R.string.estacoes));

            ShapefileFeatureTable shpLinha = new ShapefileFeatureTable(
                    shpPath + "VLT_Percurso.shp");
            layerShpLinha = new FeatureLayer(shpLinha);
            layerShpLinha.setRenderer(
                    new SimpleRenderer(
                            new SimpleLineSymbol(
                                    Color.WHITE, (float)1.5, SimpleLineSymbol.STYLE.SOLID)));
            layerShpLinha.setName(getResources().getString(R.string.percurso));

        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(),
                    "Arquivo shp não encontrado no SD Card", Toast.LENGTH_LONG).show();
        }

        mapView = new MapView(
                MainActivity.this, basemap.getSpatialReference(), basemap.getFullExtent());

        mapView.addLayer(basemap);

        FeatureLayer municipiosLayer = new FeatureLayer(gdbTables.get(1));
        municipiosLayer.setName(getResources().getString(R.string.municipios));
        mapView.addLayer(municipiosLayer);

        FeatureLayer setoresLayer = new FeatureLayer(gdbTables.get(0));
        setoresLayer.setName(getResources().getString(R.string.setores));
        mapView.addLayer(setoresLayer);

        mapView.addLayer(layerShpPonto);
        mapView.addLayer(layerShpLinha);
        mapView.addLayer(locationLayer);
        mapView.addLayer(graphicsLayer);

        mViewContainer = (FrameLayout) findViewById(R.id.main_activity_view_container);
        mViewContainer.addView(mapView);


        enderecos = loadEnderecos(csvPath);
        loadGraphiLayerFromArray(enderecos, "");

        loadListFromArray(enderecos);


        mapViewHelper = new MapViewHelper(mapView);


        mapView.setOnLongPressListener(new OnLongPressListener() {
            @Override
            public boolean onLongPress(float v, float v1) {

                    int[] ids = graphicsLayer.getGraphicIDs(v, v1, 10);
                    if (ids.length > 0) {
                        Graphic[] graphics = new Graphic[ids.length];
                        for (int i = 0; i < ids.length; i++) {
                            Graphic graphic = graphicsLayer.getGraphic(ids[i]);
                            graphics[i] = graphic;
                        }

                        if (ids.length > 1)
                            selectEndereco(graphics, true);
                        else
                            showPopup(graphics[0]);

                    }

                return false;
            }
        });


        mapView.setOnSingleTapListener(new OnSingleTapListener() {
            @Override
            public void onSingleTap(float v, float v1) {
                if (getMeasure().enableSketching) {
                    try {
                        getMeasure().singleTapAct(v, v1);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }else {
                    int[] ids = graphicsLayer.getGraphicIDs(v, v1, 10);
                    if (ids.length > 0) {
                        Graphic[] graphics = new Graphic[ids.length];
                        for (int i = 0; i < ids.length; i++) {
                            Graphic graphic = graphicsLayer.getGraphic(ids[i]);
                            graphics[i] = graphic;
                        }

                        if (ids.length > 1)
                            selectEndereco(graphics, false);
                        else
                            showInfoWindow(graphics[0]);
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_tools, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.zoomToFullExtent:
                mapView.setExtent(initialExtent);
                return true;

            case R.id.zoomIn:
                mapView.zoomin();
                return true;

            case R.id.zoomOut:
                mapView.zoomout();
                return true;

            case R.id.camadas:
                visualizacaoCamadas(findViewById(R.id.my_toolbar));
                return true;

            case R.id.filtro:
                filtroSetorCensitario();
                return true;

            case R.id.measure:
                enableMeasure();
                return true;

            case R.id.localizacao_on:
                startLocation();
                return true;

            case R.id.localizacao_off:
                stopLocation();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    private void loadGraphiLayerFromArray(Endereco[] enderecos, String setorCensitario) {


        graphicsLayer.removeAll();

        final SimpleMarkerSymbol dentroAOI = new SimpleMarkerSymbol(
                Color.GREEN, 16, SimpleMarkerSymbol.STYLE.CIRCLE);

        final SimpleMarkerSymbol foraAOI = new SimpleMarkerSymbol(
                Color.RED, 16, SimpleMarkerSymbol.STYLE.CIRCLE);

        for (final Endereco e : enderecos) {

            SimpleMarkerSymbol simpleMarker = null;

            String where = "COD_MUN_CEP5 = '" + setorCensitario  + "'";

            QueryParameters setorOI = new QueryParameters();
            setorOI.setWhere(where);
            setorOI.setOutFields(new String[]{"COD_MUN_CEP5"});
            setorOI.setGeometry(e.getPoint());
            setorOI.setSpatialRelationship(SpatialRelationship.WITHIN);

            GeodatabaseFeatureTable gdbFeatureTable = gdbTables.get(0);
            Future resultFuture = gdbFeatureTable.queryFeatures(
                    setorOI, new CallbackListener<FeatureResult>() {

                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }
                        public void onCallback(FeatureResult featureIterator){
                            Graphic graphic;
                            Map<String, Object> attributes = new HashMap<>();
                            attributes.put("NOME", e.getNome());
                            if (featureIterator.featureCount() > 0) {
                                graphic = new Graphic(e.getPoint(), dentroAOI,attributes);
                            } else {
                                graphic= new Graphic(e.getPoint(), foraAOI,attributes);
                            }
                            graphicsLayer.addGraphic(graphic);
                        }
                    });

        }

        graphicsLayer.setName(getResources().getString(R.string.enderecos));
        mapView.addLayer(graphicsLayer);

    }

    private void loadListFromArray(final Endereco[] enderecos) {

        ListView lvEnderecos = (ListView) findViewById(R.id.listViewEnderecos);
        List<String> alEnderecos = new ArrayList<>();
        for (Endereco e : enderecos) {
            alEnderecos.add(e.getNome());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                alEnderecos);
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


    private Endereco[] loadEnderecos(String csvPath) {


        ArrayList<Endereco> e = new ArrayList<>();
        try {
            InputStream csvStream = new FileInputStream(csvPath);
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            BufferedReader reader = new BufferedReader(csvStreamReader);
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] RowData = line.split(",");
                    String nome = RowData[0];
                    double x = Double.parseDouble(RowData[1]);
                    double y = Double.parseDouble(RowData[2]);

                    e.add(new Endereco(nome, new Point(x, y)));
                }
            } catch (IOException ex) {
                // handle exception
            } finally {
                try {
                    csvStreamReader.close();
                } catch (IOException eio) {
                    // handle exception
                }
            }
        } catch (IOException ex) {
            Toast.makeText(getApplicationContext(),
                    ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();


        }
        Endereco[] enderecos = new Endereco[e.size()];
        enderecos = e.toArray(enderecos);
        return enderecos;
    }

    private void startLocation() {
        if (locationManager == null) {

            locationManager =
                    (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            // Define a listener that responds to location updates
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    updateLocation(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onProviderDisabled(String provider) {
                }
            };

        }
        // Register the listener with the Location Manager to receive location updates
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 10000, 10, locationListener);
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(),
                    "Não possui permissão para obter dados do GPS", Toast.LENGTH_LONG).show();

        }
    }

    private void updateLocation(Location location) {
        Point locationWGS84 = new Point(location.getLongitude(),location.getLatitude());

        SpatialReference wgsSR = SpatialReference.create(SpatialReference.WKID_WGS84);
        SpatialReference webSR = SpatialReference.create(
                SpatialReference.WKID_WGS84_WEB_MERCATOR_AUXILIARY_SPHERE_10);

        Geometry locationWebM = GeometryEngine.project(locationWGS84, wgsSR, webSR);
        Point locationPoint = (Point)locationWebM;

        locationLayer.removeAll();

        final SimpleMarkerSymbol locationSymbol = new SimpleMarkerSymbol(
                Color.YELLOW, 16, SimpleMarkerSymbol.STYLE.DIAMOND);
        locationLayer.addGraphic(new Graphic(locationWebM, locationSymbol));

        final SimpleMarkerSymbol circleSymbol = new SimpleMarkerSymbol(
                Color.YELLOW, 2, SimpleMarkerSymbol.STYLE.CIRCLE);
        locationLayer.addGraphic(new Graphic(DrawCircle(locationPoint), circleSymbol));

        mapView.zoomToScale((Point) locationWebM, locationScale);
    }

    private void stopLocation() {
        locationLayer.removeAll();
        try {
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        } catch (SecurityException e) {
            Toast.makeText(getApplicationContext(),
                    "Não possui permissão para obter dados do GPS", Toast.LENGTH_LONG).show();

        }
    }

    private MultiPoint DrawCircle(Point center) {
        MultiPoint multiPoint = new MultiPoint();
        int pointsCount = 360;
        int radius = 100;
        double slice = 2 * Math.PI / pointsCount;
        for (int i = 0; i <= pointsCount; i++) {
            double rad = slice * i;
            double px = center.getX() + radius * Math.cos(rad);
            double py = center.getY() + radius * Math.sin(rad);
            multiPoint.add(px, py);
        }
        return multiPoint;
    }

    private void visualizacaoCamadas(View v) {

        PopupMenu popup = new PopupMenu(this, v);

        popup.inflate(R.menu.map_layers);

        Layer[] layers = mapView.getLayers();
        for(Layer layer: layers) {
            String layerName = layer.getName();
            boolean layerVisibility = layer.isVisible();

            for (int i = 0; i < popup.getMenu().size(); i++) {
                if (popup.getMenu().getItem(i).getTitle() == layerName) {
                    popup.getMenu().getItem(i).setChecked(layerVisibility);
                }
            }
        }
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Layer[] layers = mapView.getLayers();

                for (Layer layer : layers) {
                    String layerName = layer.getName();

                    if (item.getTitle() == layerName) {
                        layer.setVisible(!item.isChecked());
                        item.setChecked(layer.isVisible());
                    }

                }
                return false;
            }
        });
    }

    private void enableMeasure(){
        getMeasure().startDraw();
    }


    private void showInfoWindow(final Graphic graphic){

        mapView.centerAt((Point)graphic.getGeometry(),true);

        View content = MainActivity.this.getLayoutInflater().inflate(R.layout.info_window,null);
        TextView text = (TextView)content.findViewById(android.R.id.text1);
        text.setText(graphic.getAttributes().get("NOME").toString());

        TextView txtZoom = (TextView)content.findViewById(R.id.txtZoom);
        TextView txtMore = (TextView)content.findViewById(R.id.txtMore);
        ImageButton btnClose = (ImageButton) content.findViewById(R.id.btnClose);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.getCallout().hide();
            }
        });

        txtZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapView.zoomTo((Point) graphic.getGeometry(), 8);
            }
        });

        txtMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(graphic);
            }
        });


        mapView.getCallout().animatedShow((Point) graphic.getGeometry(), content);
    }

    public void selectEndereco(final Graphic[] graphics,final boolean isPopup){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
        builderSingle.setTitle("Selecione um endereço");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                MainActivity.this,
                android.R.layout.select_dialog_singlechoice);
        for(int i=0;i<graphics.length;i++){
            arrayAdapter.add(graphics[i].getAttributes().get("NOME").toString());
        }

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isPopup)
                    showPopup(graphics[which]);
                else
                    showInfoWindow(graphics[which]);
            }
        });

        builderSingle.show();
    }


    private void showPopup(Graphic graphic) {
        PopupInfo popupinfo = new PopupInfo();
        popupinfo.setTitle(graphic.getAttributes().get("NOME").toString());
        Popup popup = new Popup(mapView, popupinfo, graphic);
        CustomPopup mPopupFragment = new CustomPopup(mapView);
        mPopupFragment.addPopup(popup);
        mPopupFragment.show();
    }

    private void filtroSetorCensitario() {
        LayoutInflater inflater = getLayoutInflater();
        final View dialoglayout = inflater.inflate(R.layout.dialog_filter, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialoglayout);
        builder.setTitle(R.string.filtro);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {
                EditText etCodSetor = (EditText)(dialoglayout.findViewById(R.id.filtro));
                String codSetor = etCodSetor.getText().toString();
                loadGraphiLayerFromArray(enderecos, codSetor);
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }
}