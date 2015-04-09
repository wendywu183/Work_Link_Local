package com.centraltrillion.worklink.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centraltrillion.worklink.MainActivity;
import com.centraltrillion.worklink.R;
import com.centraltrillion.worklink.data.CompanyData;
import com.centraltrillion.worklink.utils.ActionBarUtility;
import com.centraltrillion.worklink.utils.ImageDownLoader;
import com.centraltrillion.worklink.utils.Utility;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class CompanyMapActivity extends ActionBarActivity implements LocationListener {

    private static final String DEBUG = "CompanyInfoDetailFragment";

    TextView nameTV;
    TextView addressTV;
    LinearLayout topLayout;

    String mTitle;
    String mAddress;

    /**
     * Map
     */
    LatLng mCompanyPlace = new LatLng(25.047924, 121.517081);
    private GoogleMap mMap;
    private Marker mMyMarker;

    /**
     * GPS
     */
    private LocationManager locationMgr;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private Location location;
    private final static int MIN_TIME_BW_UPDATES = 1000; // ms
    private final static int MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;

    @Override
    public void onResume() {
        initMap();
        getLocation();
        checkService();
        super.onResume();
    }

    private void checkService() {
        if(locationMgr==null){

        }

        if (!locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
            MyAlertDialog.setMessage(R.string.map_open_gps);
            MyAlertDialog.setPositiveButton(R.string.map_setting,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
            MyAlertDialog.setNegativeButton(R.string.map_ignore, null);
            MyAlertDialog.show();
            return;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.company_map_activity);
        ActionBarUtility.setActionBar(this, getString(R.string.map_title), R.drawable.ic_menu_back, true);

        mAddress = getIntent().getStringExtra("address");
        mTitle = getIntent().getStringExtra("title");

        try {
            double lat = Double.parseDouble(getIntent().getStringExtra("lat"));
            double lng = Double.parseDouble(getIntent().getStringExtra("lng"));
            mCompanyPlace = new LatLng(lat, lng);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();
    }

    private void initView() {
        nameTV = (TextView) this.findViewById(R.id.nameTV);
        addressTV = (TextView) this.findViewById(R.id.addressTV);
        topLayout = (LinearLayout) this.findViewById(R.id.top_layout);
        topLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDestination();
            }
        });

        nameTV.setText(mTitle);
        addressTV.setText(mAddress);

    }

    void initMap() {
        try {
            if (null == mMap) {

                SupportMapFragment mf = (SupportMapFragment) this.getSupportFragmentManager()
                        .findFragmentById(R.id.mapFragment);

                if (null != mf)
                    mMap = mf.getMap();
            }

            if (null != mMap) {
                UiSettings uiSettings = mMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setCompassEnabled(true);
                uiSettings.setMapToolbarEnabled(true);

                mMap.setMyLocationEnabled(true);
                mMap.setTrafficEnabled(true);
                mMap.setBuildingsEnabled(true);
                mMap.setIndoorEnabled(true);
                mMap.clear();
                Marker marker = mMap.addMarker(new MarkerOptions().position(mCompanyPlace)
                        .title(mTitle).snippet(mAddress));

                marker.showInfoWindow();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCompanyPlace, 16));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Location getLocation() {
        try {
            locationMgr = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if (locationMgr != null) {
                            location = locationMgr
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
                if (isNetworkEnabled) {
                    locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (locationMgr != null) {
                        location = locationMgr
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_company_map, menu);
        ActionBarUtility.setMenuItemColor(this,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            onBackPressed();
        else if (item.getItemId() == R.id.action_navigation) {
            if (null == mMyMarker) {
                AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
                MyAlertDialog.setMessage(R.string.gps_searching_fail);
                MyAlertDialog.setNegativeButton(R.string.alert_sure, null);
                MyAlertDialog.show();
            }else {
                Intent intent = new Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr="
                                + (double) (mMyMarker.getPosition().latitude)
                                + ","
                                + (double) (mMyMarker.getPosition().longitude)
                                + "&daddr="
                                + (double) (mCompanyPlace.latitude)
                                + ","
                                + (double) (mCompanyPlace.longitude)));
                intent.setClassName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Utility.startAnimation(this, false);
    }

    private void setMyLocationMarker(Location location) {

        if (null == location || null == mMap)
            return;

        if (mMyMarker != null) {
            mMyMarker.remove();
        }

        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(location.getLatitude(), location.getLongitude()));
        markerOpt.title(getResources().getString(R.string.map_tab_current));
        markerOpt.icon(BitmapDescriptorFactory.defaultMarker());
        mMyMarker = mMap.addMarker(markerOpt);
    }

    void showAllPlace() {
        if (null == mMap)
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mCompanyPlace);
        builder.include(mMyMarker.getPosition());
        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    public void showDestination() {
        if (null == mMap)
            return;

        CameraPosition cameraPosition = new CameraPosition.Builder().target(mCompanyPlace).zoom(16)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void whereAmI() {
        if (location == null)
            return;

        setMyLocationMarker(location);

        CameraPosition camPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(16)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
    }

    @Override
    public void onPause() {
        if (locationMgr != null)
            locationMgr.removeUpdates(this);

        if (null != mMap)
            mMap.setMyLocationEnabled(false);
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (locationMgr != null)
            locationMgr.removeUpdates(this);

        if (location != null)
            setMyLocationMarker(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                //Log.d(DEBUG, "Status Changed: Out of Service");
                Toast.makeText(this, "Status Changed: Out of Service", Toast.LENGTH_SHORT)
                        .show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                //Log.d(DEBUG, "Status Changed: Temporarily Unavailable");
                Toast.makeText(this, "Status Changed: Temporarily Unavailable",
                        Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.AVAILABLE:
                // Log.d(DEBUG, "Status Changed: Available");
                Toast.makeText(this, "Status Changed: Available", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
        setMyLocationMarker(null);
    }
}
