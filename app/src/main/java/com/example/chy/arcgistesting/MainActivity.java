package com.example.chy.arcgistesting;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.LocationDisplayManager;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.core.geodatabase.Geodatabase;
import com.esri.core.geodatabase.GeodatabaseFeatureTable;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected MapView m_mapView;
    protected ArcGISTiledMapServiceLayer m_serviceLayer;
    protected String m_strMapUrl ;
    public ButtonAdd m_btnAdd;
    public ButtonAdd m_EditButton;
    public FrameLayout.LayoutParams m_params;
    public LinearLayout m_ButtonLayout;

    //Esri Part
    public Geodatabase m_geodatabase;
    public GeodatabaseFeatureTable m_geodatabaseFeatureTable;
    public FeatureLayer m_featureLayer;
    public ArcGISLocalTiledLayer m_tpkLayer;


    //Location part
    public LocationDisplayManager m_locationDisplayManager;
    public Point m_Locaton;
    final static double SEARCH_RADIUS = 5;


    //Edit part
    public ArrayList<Point> m_pointList;
    public Point m_point;
    public ArrayList<Polygon> m_polygonList;
    public Polygon m_polygon;
    public ArrayList<Polyline> m_polylineList;
    public Polyline m_polyline;
    public int[] m_selectedFeaturesIDs;
    public ArcGISFeatureLayer m_AGSfeatureLayer;
    //Popup part
    /*
    public PopupContainer m_popupContainer;
    public View m_content;
    public Callout m_callout;
    final static int TITLE_ID = 10;
    final static int REVIEW_ID = 2;
    */





    GraphicsLayer routeLayer, hiddenSegmentsLayer;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        m_ButtonLayout= (LinearLayout)findViewById(R.id.ButtonLayout);
        m_btnAdd = new ButtonAdd();

        Button btn = new Button(this);
        btn.setText("开始");
        btn.setLayoutParams(m_params);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_btnAdd.buttonHide();
                m_EditButton.buttonShow();
            }
        });
        m_btnAdd.buttonListAdd(btn);
        m_btnAdd.addToView(m_ButtonLayout);
        mapInit();

        //Edit Button init
        m_EditButton = EditButtonInit();
        m_EditButton.addToView(m_ButtonLayout);
        m_EditButton.buttonHide();

        //load Local Data
        routeLayer = new GraphicsLayer();
        m_mapView.addLayer(routeLayer);

        //Edit part init
        m_polylineList = new ArrayList<>();
        m_polygonList = new ArrayList<>();
        m_pointList = new ArrayList<>();


        //location part
        m_locationDisplayManager = m_mapView.getLocationDisplayManager();

        //Popup part
        /*
        m_popupContainer = new PopupContainer(m_mapView);

        m_callout = m_mapView.getCallout();
        m_content = new LinearLayout(this);
        */

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            setContentView(R.layout.activity_main);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void click(View v){
        Intent intent=new Intent(this,Main2Activity.class);
        startActivity(intent);
        this.finish();
    }
    public void ButtonLayoutOnClick(View v) {
            if(m_ButtonLayout.getAlpha() < 1){
                m_ButtonLayout.setAlpha((float)(m_ButtonLayout.getAlpha()+0.1));
            }
            else
                m_ButtonLayout.setAlpha((float) 0.1);
    }

    public void mapInit(){
        m_mapView = (MapView)this.findViewById(R.id.map);
        /*
        m_strMapUrl = "http://192.168.20.100:6080/arcgis/rest/services/FeatureAccess/Test_FeatureAccess/FeatureServer/0";
        m_serviceLayer = new ArcGISTiledMapServiceLayer(m_strMapUrl);
        */
        m_AGSfeatureLayer = new ArcGISFeatureLayer(
                "http://192.168.20.100:6080/arcgis/rest/services/FeatureAccess/Test_FeatureAccess/FeatureServer/0",
                ArcGISFeatureLayer.MODE.ONDEMAND);
        m_mapView.addLayer(m_AGSfeatureLayer);

        try {
            m_geodatabase = new Geodatabase(FileAccessor.geoDatabasePath);
            m_geodatabaseFeatureTable = m_geodatabase.getGeodatabaseFeatureTableByLayerId(0);
            m_featureLayer = new FeatureLayer(m_geodatabaseFeatureTable);

            String Path = Environment.getExternalStorageDirectory().getPath() + "/test/test.tpk";
            m_tpkLayer = new ArcGISLocalTiledLayer(Path);
            m_mapView.addLayer(m_tpkLayer);
            m_mapView.addLayer(m_featureLayer);

        }
        catch (Exception e){
            Log.e("MainActivity","exception_______",e);

        }


    }

    public ButtonAdd EditButtonInit(){
        ButtonAdd ba = new ButtonAdd();
        ArrayList<Button> btnlist = new ArrayList<>();
        Button btn1 = new Button(this);
        btn1.setText("点");
        btn1.setLayoutParams(m_params);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_mapView.setOnSingleTapListener(new OnSingleTapListener() { // Set a single tap listener
                    @Override
                    public void onSingleTap(float x, float y) {
                        // Obtain the clicked point from a single tap on the map
                        Point point = m_mapView.toMapPoint(x, y);
                        m_pointList.add(point);
                        Graphic[] adds = new Graphic[m_pointList.size()];
                        for (int i =0;i<m_pointList.size();i++){
                            Map<String, Object> attributes = new HashMap<String, Object>();
                            attributes.put("Type", "Park");
                            attributes.put("Description", "Editing...");
                            Graphic newFeatureGraphic = new Graphic(m_pointList.get(i), new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.STYLE.CIRCLE), attributes, 0);
                            adds[i] = newFeatureGraphic;
                        }
                        routeLayer.addGraphics(adds);
                    }
                });
            }
        });
        btnlist.add(btn1);

        Button btn2 = new Button(this);
        btn2.setText("线");
        btn2.setLayoutParams(m_params);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentGeometryReset();
                m_mapView.setOnSingleTapListener(new OnSingleTapListener() { // Set a single tap listener
                    @Override
                    public void onSingleTap(float x, float y) {
                        Point mapPt = m_mapView.toMapPoint(x, y);
                        if (m_polyline == null){
                            m_polyline = new Polyline();
                            m_polyline.startPath(mapPt);
                        }
                        m_polyline.lineTo(mapPt);
                        //add current Geometry to List
                        m_polylineList.add(m_polyline);
                        //add Geometry to GraphicLayer
                        Graphic[] adds = new Graphic[m_polylineList.size()];
                        for (int i = 0;i<m_polylineList.size();i++){
                            Map<String, Object> attributes = new HashMap<String, Object>();
                            attributes.put("Type", "Park");
                            attributes.put("Description", "Editing...");
                            Graphic newFeatureGraphic = new Graphic(m_polylineList.get(i), new SimpleLineSymbol(Color.RED, 1, SimpleLineSymbol.STYLE.SOLID), attributes, 0);
                            adds[i] = newFeatureGraphic;
                        }
                        routeLayer.addGraphics(adds);
                    }
                });
            }
        });
        btnlist.add(btn2);

        Button btn3 = new Button(this);
        btn3.setText("面");
        btn3.setLayoutParams(m_params);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentGeometryReset();
                m_mapView.setOnSingleTapListener(new OnSingleTapListener() { // Set a single tap listener
                    @Override
                    public void onSingleTap(float x, float y) {
                        Point mapPt = m_mapView.toMapPoint(x, y);
                        if (m_polygon == null){
                            m_polygon = new Polygon();
                            m_polygon.startPath(mapPt);
                        }
                        m_polygon.lineTo(mapPt);
                        //add current Geometry to List
                        m_polygonList.add(m_polygon);
                        //add Geometry to GraphicLayer
                        Graphic[] adds = new Graphic[m_polygonList.size()];
                        for (int i =0;i<m_polygonList.size();i++){
                            Map<String, Object> attributes = new HashMap<String, Object>();
                            attributes.put("Type", "Park");
                            attributes.put("Description", "Editing...");
                            Graphic newFeatureGraphic = new Graphic(m_polygonList.get(i), new SimpleFillSymbol(Color.RED,SimpleFillSymbol.STYLE.SOLID), attributes, 0);
                            adds[i] = newFeatureGraphic;
                        }
                        routeLayer.addGraphics(adds);
                    }
                });


            }
        });
        btnlist.add(btn3);

        Button btn4 = new Button(this);
        btn4.setText("定位");
        btn4.setLayoutParams(m_params);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_locationDisplayManager.isStarted()){
                    m_locationDisplayManager.stop();
                }
                else{
                    m_locationDisplayManager.setAutoPanMode(LocationDisplayManager.AutoPanMode.LOCATION);
                    m_locationDisplayManager.setLocationListener(new LocationListener() {
                        boolean locationChanged = false;
                        // Zooms to the current location when first GPS fix arrives.
                        @Override
                        public void onLocationChanged(Location loc) {
                            if (!locationChanged) {
                                locationChanged = true;
                                double locy = loc.getLatitude();
                                double locx = loc.getLongitude();
                                Point wgspoint = new Point(locx, locy);
                                Point mapPoint = (Point) GeometryEngine.project(wgspoint,SpatialReference.create(4326),m_mapView.getSpatialReference());
                                Unit mapUnit = m_mapView.getSpatialReference().getUnit();
                                double zoomWidth = Unit.convertUnits(SEARCH_RADIUS,Unit.create(LinearUnit.Code.MILE_US),mapUnit);
                                Envelope zoomExtent = new Envelope(mapPoint,zoomWidth, zoomWidth);
                                m_mapView.setExtent(zoomExtent);
                            }

                        }

                        @Override
                        public void onProviderDisabled(String arg0) {

                        }

                        @Override
                        public void onProviderEnabled(String arg0) {
                        }

                        @Override
                        public void onStatusChanged(String arg0, int arg1,
                                                    Bundle arg2) {

                        }
                    });
                    m_locationDisplayManager.start();
                }


            }
        });
        btnlist.add(btn4);

        Button btn5 = new Button(this);
        btn5.setText("test");
        btn5.setLayoutParams(m_params);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                m_mapView.setOnLongPressListener(new OnLongPressListener() {
                    @Override
                    public boolean onLongPress(float v, float v1) {
                        m_selectedFeaturesIDs = routeLayer.getGraphicIDs(v,v1,100);
                        // select the features
                        Graphic graphics = routeLayer.getGraphic(m_selectedFeaturesIDs[0]);
                        routeLayer.setSelectedGraphics(m_selectedFeaturesIDs, true);
                        //Point location = (Point) graphics.getGeometry();
                        //m_callout.setOffset(0, -15);
                        //m_callout.show(location,m_content);
                        return false;
                    }
                });

            }
        });
        btnlist.add(btn5);


        Button btn6 = new Button(this);
        btn6.setText("delete");
        btn6.setLayoutParams(m_params);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeLayer.removeGraphics(m_selectedFeaturesIDs);
            }
        });
        btnlist.add(btn6);

        Button btnLast = new Button(this);
        btnLast.setText("结束");
        btnLast.setLayoutParams(m_params);
        btnLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_EditButton.buttonHide();
                m_btnAdd.buttonShow();
                m_mapView.setOnSingleTapListener(new OnSingleTapListener() { // Set a single tap listener
                    @Override
                    public void onSingleTap(float x, float y) {
                    }
                });
            }
        });
        btnlist.add(btnLast);
        ba.buttonListAdd(btnlist);
        return ba;
    }

    public void currentGeometryReset(){
        m_polyline = null;
        m_point = null;
        m_polygon = null;
    }


}
