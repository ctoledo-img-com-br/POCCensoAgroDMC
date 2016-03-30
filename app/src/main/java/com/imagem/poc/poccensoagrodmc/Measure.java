package com.imagem.poc.poccensoagrodmc;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geometry.*;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.CompositeSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;

import java.text.DecimalFormat;

/**
 * Created by Wlima on 23/03/2016.
 */
public class Measure {

    public Measure(Context context, MapView map){
        this.mainContext = context;
        this.mapView = map;
        this.drawGeometryLayer = new GraphicsLayer();
        this.mapView.addLayer(this.drawGeometryLayer);
        this.resultText = (TextView)((Activity)context).findViewById(R.id.txtArea);
        btnStopMeasure = (Button)((Activity)context).findViewById(R.id.btnStopMeasure);
        btnStopMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDraw();
            }
        });
        this.myLayout = (RelativeLayout)((Activity)context).findViewById(R.id.linearMedida);


    }
    public boolean enableSketching;
    Context mainContext;
    MapView mapView;
    double measure;
    double value;
    boolean isStartPointSet1 ;

    public static final LinearUnit LINEARUNIT_METER = (LinearUnit) Unit
            .create(LinearUnit.Code.METER);

    public static final AreaUnit AREAUNIT_SQUARE_METER = (AreaUnit) Unit
            .create(AreaUnit.Code.SQUARE_METER);
    GraphicsLayer drawGeometryLayer = null;
    Geometry drawGeometry;
    TextView resultText = null;
    Button btnStopMeasure;
    DecimalFormat twoDForm = new DecimalFormat("#.##");
    RelativeLayout myLayout;

    public void startDraw(){
        enableSketching = true;
        drawGeometry = null;
        drawGeometryLayer.removeAll();
        this.resultText.setText("");
        isStartPointSet1 = false;
        myLayout.setVisibility(View.VISIBLE);
    }
    public void stopDraw(){
        enableSketching = false;
        myLayout.setVisibility(View.INVISIBLE);
        drawGeometryLayer.removeAll();
    }
    void singleTapAct(float x, float y) throws Exception{
        Point point = this.mapView.toMapPoint(x, y);

        if(drawGeometry == null){
            drawGeometry = new Polygon();
            ((MultiPath)drawGeometry).startPath(point);
            isStartPointSet1 = true;
        }

        drawGeomOnGraphicLyr(drawGeometry,point,isStartPointSet1);
    }
    void drawGeomOnGraphicLyr(Geometry geometryToDraw,Point point,boolean startPointSet) {
        int color = Color.argb(120,0,0,255);
        if (startPointSet) {
            ((Polygon) geometryToDraw).lineTo(point);
            // Simplify the geometry and project to spatial ref with
            // WKID for World Cylindrical Equal Area 54034
            Geometry geometry = GeometryEngine.simplify(geometryToDraw,
                    mapView.getSpatialReference());
            Geometry g2 = GeometryEngine.project(geometry,
                    mapView.getSpatialReference(),
                    SpatialReference.create(54034));
            // Get the area for the polygon
            measure = Math.abs(g2.calculateArea2D());
            if (measure != 0.0)
                doConvert();
        }

        Geometry[] geoms = new Geometry[1];
        geoms[0] = geometryToDraw;

        try {
            drawGeometryLayer.removeAll();
            highlightGeometriesWithColor(geoms, color);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void highlightGeometriesWithColor(Geometry[] geoms,int color){

        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(color,SimpleFillSymbol.STYLE.SOLID);
        SimpleLineSymbol outLine = new SimpleLineSymbol(
                Color.YELLOW, 2, SimpleLineSymbol.STYLE.DASHDOT);

        CompositeSymbol compositeSymbol = new CompositeSymbol();
        compositeSymbol.add(fillSymbol);
        compositeSymbol.add(outLine);

        for(Geometry geo: geoms){
            if(geo.getType() == Geometry.Type.POLYGON){
                drawGeometryLayer.addGraphic(new Graphic(geo,compositeSymbol));
            }
        }
    }
    @SuppressWarnings("boxing")
    void doConvert() {

        int toUnit = measure > 1000 ? AreaUnit.Code.SQUARE_KILOMETER : AreaUnit.Code.SQUARE_METER;

        if (toUnit == AreaUnit.Code.SQUARE_METER) {
            // value = Double.valueOf(twoDForm.format(measure));
            resultText.setText(twoDForm.format(measure) + "m²");
            return;
        }else {
            value = Unit.convertUnits(measure, AREAUNIT_SQUARE_METER,
                    Unit.create(toUnit));
            resultText.setText(twoDForm.format(value) + " km²");
        }
    }
}
