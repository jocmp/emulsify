package edu.gvsu.cis.emulsify;

import android.app.Activity;
import android.app.FragmentManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class mapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    GoogleMap worldMap;
    LocationClient mapClient;

    @Override
    protected void onStart() {
        super.onStart();
   /* attempt to connect to the Google Map services should be done
      in onStart(), i.e. when the app is visible */
        if (mapClient != null)
            mapClient.connect();  /* connect to the GooglePlayService */
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapClient != null)
            mapClient.disconnect(); /* disconnect when our app is invisible */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getFragmentManager();
        /* Obtain a reference to the UI element */
        MapFragment frag = (MapFragment) fm.findFragmentById (R.id.overworld);

        /* Obtain a reference to GoogleMap object associated with the fragment */
        worldMap = frag.getMap();

        mapClient = new LocationClient(this, this, this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();
        MarkerOptions options = new MarkerOptions();
        /* GeoLocation of Mackinac Hall */
        //options.position(new LatLng(42.9666481,-85.887133));
        //worldMap.addMarker(options);

        /* enable MyLocation layer to show the current location as a blue dot */
        worldMap.setMyLocationEnabled(true);
        zoomToCurrentLocation();
    }

    @Override
    public void onDisconnected() {
        //Toast.makeText(this, "onDisconnected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    private void zoomToCurrentLocation()
    {
        Location myLoc;
        LatLng myGeoLoc;
        myLoc = mapClient.getLastLocation();
        myGeoLoc = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());

    /* zoom level 15: street level. Smaller number zoom-out, bigger: zoom-in */
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(myGeoLoc, 15);

    /* Title will pop up when the icon is tapped */
        //MarkerOptions options = new MarkerOptions();
        //worldMap.addMarker (options.position(myGeoLoc).title("Mackinac Hall"));

    /* animate camera from (0,0) to current location in 3 seconds */
        worldMap.animateCamera(camUpdate, 3000, null);
    }
}