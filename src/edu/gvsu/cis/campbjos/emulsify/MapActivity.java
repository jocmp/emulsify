package edu.gvsu.cis.campbjos.emulsify;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import edu.gvsu.cis.emulsify.R;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener, GooglePlayServicesClient.OnConnectionFailedListener {

    GoogleMap worldMap;
    LocationClient mapClient;


    private SharedPreferences ePrefs;
    private final String infoDialoguePref = "firstInfo";
    //    MarkerManager manager;
//    MarkerManager.Collection collection;
    GoogleMap.OnMarkerClickListener markerClick;
    GoogleMap.OnInfoWindowClickListener windowClick;

    Map<Marker, String> filePaths = new HashMap<Marker, String>();


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
        //Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getFragmentManager();
        /* Obtain a reference to the UI element */
        MapFragment frag = (MapFragment) fm.findFragmentById(R.id.overworld);

        /* Obtain a reference to GoogleMap object associated with the fragment */
        worldMap = frag.getMap();

        mapClient = new LocationClient(this, this, this);

        ePrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean infoShown = ePrefs.getBoolean(infoDialoguePref, false);

        //if (!infoShown) { //TODO uncomment this
        String title = "Map Gallery";
        String text = getResources().getString(R.string.mapInfo);
        new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton(
                android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
        SharedPreferences.Editor editor = ePrefs.edit();
        editor.putBoolean(infoDialoguePref, true);
        editor.commit();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        //if (marker.isInfoWindowShown()) {
        //    marker.hideInfoWindow();
        //} else
        marker.showInfoWindow();


        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Intent editIntent = new Intent(this, EditActivity.class);

        //File imgDir =
        //        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //editIntent.putExtra("filename", imgDir.getAbsolutePath()+"/emulsify" + currentPhotoString);
        editIntent.putExtra("filename", filePaths.get(marker));

        startActivity(editIntent);
        //finish();
        //marker.hideInfoWindow();
    }


    public class PictureLoader extends AsyncTask<Void, Object, Void> {
        Context context;

        public PictureLoader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... params) {
            //File directory = new File("/storage/emulated/0/DCIM/Camera");//1396798512418.jpg
            //File directory1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);//+"/Camera");
            File directory = HomeActivity.EMULSIFY_DIRECTORY; //new File(directory1.getAbsolutePath() + "/emulsify");
            //File directory = new File(Environment.DIRECTORY_DCIM+"/camera/");
            File[] files = directory.listFiles();
            for (File f : files) {
                //System.out.println(f.getAbsolutePath() + "\n" + f.getName());
                String fileName = f.getName();//f.getPath();

                Log.d("Reuben", f.getAbsolutePath());

                //String fS =
                //if (fileName.startsWith("emulsify")) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth() / bmp.getHeight()) * 50), 50, false);


                //float[] d = new float[2];
                float Latitude = 0.0F, Longitude = 0.0F;
                try {
                    ExifInterface exif = new ExifInterface(f.getAbsolutePath());
                    //exif.getLatLong(d);

                    //d[0] = Float.parseFloat(exif.getAttribute("lat"));
                    //d[1] = Float.parseFloat(exif.getAttribute("long"));
                    //d[0] = Float.parseFloat(exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
                    //d[1] = Float.parseFloat(exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
                    //TODO: credit http://stackoverflow.com/questions/15403797/how-to-get-the-latititude-and-longitude-of-an-image-in-sdcard-to-my-application
                    String LATITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                    String LATITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                    String LONGITUDE = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                    String LONGITUDE_REF = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                    if ((LATITUDE != null)
                            && (LATITUDE_REF != null)
                            && (LONGITUDE != null)
                            && (LONGITUDE_REF != null)) {

                        if (LATITUDE_REF.equals("N")) {
                            Latitude = convertToDegree(LATITUDE);
                        } else {
                            Latitude = 0 - convertToDegree(LATITUDE);
                        }

                        if (LONGITUDE_REF.equals("E")) {
                            Longitude = convertToDegree(LONGITUDE);
                        } else {
                            Longitude = 0 - convertToDegree(LONGITUDE);
                        }


                        publishProgress(bmp2, Latitude, Longitude, f.getAbsolutePath());
                    }
                    //String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                    //String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                    //lat = convertToDegree(latitude);
                    //lon = convertToDegree(longitude);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                //publishProgress(bmp2, d[0], d[1], f.getAbsolutePath());
                //}
            }

            return null;
        }

        //TODO: credit http://stackoverflow.com/questions/15403797/how-to-get-the-latititude-and-longitude-of-an-image-in-sdcard-to-my-application
        private Float convertToDegree(String stringDMS) {
            Float result = null;
            String[] DMS = stringDMS.split(",", 3);

            String[] stringD = DMS[0].split("/", 2);
            Double D0 = new Double(stringD[0]);
            Double D1 = new Double(stringD[1]);
            Double FloatD = D0 / D1;

            String[] stringM = DMS[1].split("/", 2);
            Double M0 = new Double(stringM[0]);
            Double M1 = new Double(stringM[1]);
            Double FloatM = M0 / M1;

            String[] stringS = DMS[2].split("/", 2);
            Double S0 = new Double(stringS[0]);
            Double S1 = new Double(stringS[1]);
            Double FloatS = S0 / S1;

            result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

            return result;


        }

        ;

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            if ((Float) values[1] == 0.0F && (Float) values[2] == 0.0F) return;

            MarkerOptions options = new MarkerOptions();
        /* GeoLocation of Mackinac Hall */
            options.position(new LatLng((Float) values[1], (Float) values[2]));
            options.icon(BitmapDescriptorFactory.fromBitmap((Bitmap) values[0]));
            //options.title("hi.");
            //options.infoWindowAnchor((Float) values[1], (Float) values[2]);
            //GoogleMap.InfoWindowAdapter a = new GoogleMap.InfoWindowAdapter() {

            //@Override
            //public View getInfoContents(Marker marker) {
            //    return null;
            //}
            //}
            //Marker marker = new Marker();//
            Marker mark = worldMap.addMarker(options);

            filePaths.put(mark, (String) values[3]);
            //Marker marker = collection.addMarker(options);
            //mark.setIcon(BitmapDescriptorFactory.fromBitmap((Bitmap) values[0]));
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
//        manager = new MarkerManager(worldMap);
//        collection = manager.newCollection();
//        markerClick = new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                marker.showInfoWindow();
//                return true;
//            }
//        };
//
//        windowClick = new GoogleMap.OnInfoWindowClickListener() {
//            @Override
//            public void onInfoWindowClick(Marker marker) {
//                marker.hideInfoWindow();
//            }
//        };
//
//
//        collection.setOnMarkerClickListener(markerClick);
//        collection.setOnInfoWindowClickListener(windowClick);
//

        PictureLoader loader = new PictureLoader(this);
        loader.execute();

        //Toast.makeText(this, "onConnected", Toast.LENGTH_SHORT).show();

        //MarkerOptions options = new MarkerOptions();
        /* GeoLocation of Mackinac Hall */
        //options.position(new LatLng(42.9666481,-85.887133));
        //Marker marker = new Marker();//
        //worldMap.addMarker(options);
        //Action_View
        worldMap.setOnMarkerClickListener(this);
        worldMap.setOnInfoWindowClickListener(this);

        /* enable MyLocation layer to show the current location as a blue dot */
        worldMap.setMyLocationEnabled(true);
        zoomToCurrentLocation();

        worldMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    }

    //TODO: credit http://androidfreakers.blogspot.com/2013/08/display-custom-info-window-with.html
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View view;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.info_window_layout, null);
        }

        @Override
        public View getInfoContents(Marker marker) {

        /*    if (marker != null
                    && marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }*/
            ImageView image = (ImageView) view.findViewById(R.id.infoWindow_imageView);
            Bitmap bmp = BitmapFactory.decodeFile(filePaths.get(marker));

            image.setImageBitmap(bmp);
            return view;//null;
        }


        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "onDisconnected", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed", Toast.LENGTH_SHORT).show();
    }

    private void zoomToCurrentLocation() {
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