package com.imagem.poc.poccensoagrodmc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.ProjectionTransformation;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureResult;
import com.esri.core.map.Graphic;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;
import com.esri.core.tasks.query.QueryParameters;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {

    public FrameLayout mViewContainer;
    private Point p1;
    private Point p2;
    final private double selectionScale = 18055.954822;
    final private double locationScale = 9027.977411;
    MapView mapView = null;
    Polygon initialExtent = null;
    private Toolbar toolbar;

    final private String basemapPath = "/storage/extSdCard/data/RiodeJaneiro/basemap/basemap.tpk";
    final private String gdbPath = "/storage/extSdCard/data/RiodeJaneiro/gdb/poc.geodatabase";
    final private String shpPath = "/storage/extSdCard/data/RiodeJaneiro/shp/";

    private List<GeodatabaseFeatureTable> gdbTables = null;
    private GraphicsLayer locationLayer = null;

    LocationManager locationManager = null;
    LocationListener locationListener = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final Button button = (Button) findViewById(R.id.btn_camadas);
        button.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                  visualizacaoCamadas(v);
              }
        });

        ArcGISRuntime.setClientId("gZK3c64UFVTUmPcI");

        ArcGISLocalTiledLayer basemap = new ArcGISLocalTiledLayer(basemapPath);
        basemap.setName(getResources().getString(R.string.mapabase));
        initialExtent = basemap.getExtent();
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

        FeatureLayer municipiosLayer = new FeatureLayer(gdbTables.get(0));
        municipiosLayer.setName(getResources().getString(R.string.municipios));
        mapView.addLayer(municipiosLayer);

        FeatureLayer setoresLayer = new FeatureLayer(gdbTables.get(1));
        setoresLayer.setName(getResources().getString(R.string.setores));
        mapView.addLayer(setoresLayer);

        mapView.addLayer(layerShpPonto);
        mapView.addLayer(layerShpLinha);
        mapView.addLayer(locationLayer);

        mViewContainer = (FrameLayout) findViewById(R.id.main_activity_view_container);
        mViewContainer.addView(mapView);

        // Carga de Endereços na lista e no mapa
        final Endereco enderecos[] = loadEnderecos();
        loadGraphiLayerFromArray(enderecos);
        loadListFromArray(enderecos);

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


    private void loadGraphiLayerFromArray(Endereco[] enderecos) {

        final GraphicsLayer graphicsLayer = new GraphicsLayer();

        final SimpleMarkerSymbol dentroAOI = new SimpleMarkerSymbol(
                Color.GREEN, 16, SimpleMarkerSymbol.STYLE.CIRCLE);

        final SimpleMarkerSymbol foraAOI = new SimpleMarkerSymbol(
                Color.RED, 16, SimpleMarkerSymbol.STYLE.CIRCLE);

        for (final Endereco e : enderecos) {

            SimpleMarkerSymbol simpleMarker = null;

            QueryParameters setorOI = new QueryParameters();
            setorOI.setWhere("COD_MUN_CEP5 = '330455 20211'");
            setorOI.setOutFields(new String[]{"COD_MUN_CEP5"});
            setorOI.setGeometry(e.getPoint());
            setorOI.setSpatialRelationship(SpatialRelationship.WITHIN);

            GeodatabaseFeatureTable gdbFeatureTable = gdbTables.get(0);
            Future resultFuture = gdbFeatureTable.queryFeatures(
                    setorOI, new CallbackListener<FeatureResult>() {

                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                public void onCallback(FeatureResult featureIterator) {
                    if (featureIterator.featureCount() > 0) {
                        graphicsLayer.addGraphic(new Graphic(e.getPoint(), dentroAOI));;
                    } else {
                        graphicsLayer.addGraphic(new Graphic(e.getPoint(), foraAOI));
                    }

                }
            });

        }

        graphicsLayer.setName(getResources().getString(R.string.enderecos));
        mapView.addLayer(graphicsLayer);
    }

    private void loadListFromArray(final Endereco[] enderecos) {

        ListView lvEnderecos = (ListView) findViewById(R.id.listViewEnderecos);
        List<String> alEnderecos = new ArrayList<String>();
        for (Endereco e : enderecos) {
            alEnderecos.add(e.getNome());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
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


    private Endereco[] loadEnderecos() {
        return new Endereco[]
        {
                new Endereco("Praça da República", new Point(-4807724.667, -2620682.984)),
                new Endereco("Aeroporto Santos Dummont", new Point(-4804787.787, -2621778.362))
        };

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
        locationLayer.addGraphic(new Graphic(DrawCircle(locationPoint),circleSymbol));

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

}