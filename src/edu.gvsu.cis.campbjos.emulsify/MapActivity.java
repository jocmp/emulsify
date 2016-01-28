package edu.gvsu.cis.campbjos.emulsify;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//import com.google.maps.android.MarkerManager;
//import com.google.maps.android.ui.IconGenerator;

// created by emulsify team 4/15/2014

public class MapActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, ClusterManager.OnClusterItemClickListener<MapActivity.MyItem>, ClusterManager.OnClusterClickListener<MapActivity.MyItem>,// ClusterManager.OnClusterItemInfoWindowClickListener<MapActivity.MyItem>, //ClusterManager.OnClusterClickListener<MapActivity.MyItem>, //GoogleMap.OnMarkerClickListener,        GoogleMap.OnInfoWindowClickListener,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private int ICON_HEIGHT;

    private SharedPreferences ePrefs;
    private final String infoDialoguePref = "firstInfo";

    GoogleMap worldMap;
    LocationClient mapClient;


    Map<Marker, String> filePaths = new HashMap<Marker, String>();
    Map<MyItem, String> tempFilePaths = new HashMap<MyItem, String>();

    ClusterManager<MyItem> manager;
    Map<MyItem, Bitmap> icons = new HashMap<MyItem, Bitmap>();

    //used to restore the data quickly
    private class DataRestorer {
        ArrayList<String> files = new ArrayList<String>();
        ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
        ArrayList<Float> latArray = new ArrayList<Float>();
        ArrayList<Float> lngArray = new ArrayList<Float>();

        public ArrayList<String> getFiles() {
            return files;
        }

        public ArrayList<Bitmap> getBitmaps() {
            return bitmaps;
        }

        public ArrayList<Float> getLats() {
            return latArray;
        }

        public ArrayList<Float> getLngs() {
            return lngArray;
        }

        public DataRestorer() {
        }

        //TODO: accept a bundle and take data from it (for convenience and so the keys can remain unknown (for chaos control))
        public DataRestorer(ArrayList<String> f, ArrayList<Parcelable> b, float[] lat, float[] lng) {
            files = f;

            for (Parcelable p : b) {
                bitmaps.add((Bitmap) p);
            }
            for (float l : lat) {
                latArray.add(l);
            }
            for (float l : lng) {
                lngArray.add(l);
            }
        }

        public void addData(String s, Bitmap b, float lat, float lng) {
            files.add(s);
            bitmaps.add(b);
            latArray.add(lat);
            lngArray.add(lng);
        }

        public void replaceBitmap(int i, Bitmap b) {
            bitmaps.remove(i);
            bitmaps.add(i, b);
        }


        public void saveData(Bundle bundle) {
            bundle.putStringArrayList("allFiles", files);
            bundle.putParcelableArrayList("allBitmaps", bitmaps);
            float[] lat = new float[latArray.size()];
            for (int i = 0; i < lat.length; i++) {
                lat[i] = latArray.get(i);
            }
            bundle.putFloatArray("allLat", lat);
            float[] lng = new float[lngArray.size()];
            for (int i = 0; i < lng.length; i++) {
                lng[i] = lngArray.get(i);
            }
            bundle.putFloatArray("allLng", lng);
        }

    }

    DataRestorer dataRestorer = new DataRestorer();

    float zoomlevel = 15;
    boolean loaded = false;
    double currentLat = 0.0, currentLng = 0.0;

    String fileToReload;


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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_map);
        /* ActionBar items */
        try {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Toast.makeText(this,
                    "Something went wrong. Try again.",
                    Toast.LENGTH_SHORT).show();
        }
        ICON_HEIGHT = getResources().getDimensionPixelSize(R.dimen.mapIconHeight);

        //Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        FragmentManager fm = getFragmentManager();
        /* Obtain a reference to the UI element */
        MapFragment frag = (MapFragment) fm.findFragmentById(R.id.overworld);

        /* Obtain a reference to GoogleMap object associated with the fragment */
        worldMap = frag.getMap();
        mapClient = new LocationClient(this, this, this);
        mapClient = new LocationClient(this, this, this);
        ePrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean infoShown = ePrefs.getBoolean(infoDialoguePref, false);

        if (!infoShown) {
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

        if (savedInstanceState != null) {
            zoomlevel = savedInstanceState.getFloat("zoom");
            loaded = savedInstanceState.getBoolean("loaded");
            currentLat = savedInstanceState.getDouble("lat");
            currentLng = savedInstanceState.getDouble("lng");
            fileToReload = savedInstanceState.getString("redo");

            dataRestorer = new DataRestorer(savedInstanceState.getStringArrayList("allFiles"),
                    savedInstanceState.getParcelableArrayList("allBitmaps"),
                    savedInstanceState.getFloatArray("allLat"), savedInstanceState.getFloatArray("allLng"));
        }

    }


    @Override
    public boolean onClusterItemClick(MyItem item) {
        //do nothing, absolutely nothing; why muddy the waters?
        return false;
    }


    @Override
    public boolean onClusterClick(Cluster<MyItem> cluster) {
        Collection<MyItem> collection = cluster.getItems();

        showIconDialog(collection);
        return true;
    }


    public class PictureLoader extends AsyncTask<Void, Object, Void> {
        Context context;
        Boolean emptyFlag = false;

        public PictureLoader(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            setProgressBarIndeterminateVisibility(true);
            Toast.makeText
                    (getApplicationContext(), "Loading pictures...", Toast.LENGTH_SHORT).show();
        }


        @Override
        protected Void doInBackground(Void... params) {
            //File directory = new File("/storage/emulated/0/DCIM/Camera");//1396798512418.jpg
            //File directory1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);//+"/Camera");
            File directory = HomeActivity.EMULSIFY_DIRECTORY; //new File(directory1.getAbsolutePath() + "/emulsify");
            //File directory = new File(Environment.DIRECTORY_DCIM+"/camera/");
            File[] files = directory.listFiles();

            if (files != null) {
                for (File f : files) {

                    Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                    Bitmap bmp2 = null;
                    if (bmp.getWidth() > bmp.getHeight())
                        bmp2 = Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth() / bmp.getHeight()) * ICON_HEIGHT), ICON_HEIGHT, false);
                    else if (bmp.getWidth() < bmp.getHeight())
                        bmp2 = Bitmap.createScaledBitmap(bmp, ICON_HEIGHT, (int) (((float) bmp.getHeight() / bmp.getWidth()) * ICON_HEIGHT), false);


                    float Latitude = 0.0F, Longitude = 0.0F;
                    ExifInterface exif = null;

                    try {
                        exif = new ExifInterface(f.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

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
                    } else {
                        // try the second approach, which is not used by emulsify pictures (thus hopefully allowing for
                        // the easy assimilation of ANY picture put in the emulsify directory)
                        float[] d = new float[2];
                        exif.getLatLong(d);
                        Latitude = d[0];
                        Longitude = d[1];
                        publishProgress(bmp2, Latitude, Longitude, f.getAbsolutePath());
                    }


                }
            } else {
                emptyFlag = true;
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

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            if ((Float) values[1] == 0.0F && (Float) values[2] == 0.0F) return;

            MyItem item = new MyItem((Float) values[1], (Float) values[2]);
            tempFilePaths.put(item, (String) values[3]);
            icons.put(item, ((Bitmap) values[0]));

            manager.addItem(item);

            dataRestorer.addData((String) values[3], (Bitmap) values[0], (Float) values[1], (Float) values[2]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setProgressBarIndeterminateVisibility(false);
            loaded = true;
            if (emptyFlag == true) {
                Toast.makeText
                        (getApplicationContext(), "No pictures to load!", Toast.LENGTH_LONG).show();
                emptyFlag = false;
            } else {
                Toast.makeText
                        (getApplicationContext(), "Pictures loaded!", Toast.LENGTH_SHORT).show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            super.onPostExecute(aVoid);
        }
    }


    @Override
    public void onConnected(Bundle bundle) {

        worldMap.clear();

        filePaths = new HashMap<Marker, String>();
        tempFilePaths = new HashMap<MyItem, String>();
        icons = new HashMap<MyItem, Bitmap>();

        manager = new ClusterManager<MyItem>(this, worldMap) {

            @Override
            public void onInfoWindowClick(Marker marker) {
                super.onInfoWindowClick(marker);

                marker.hideInfoWindow();

                currentLat = marker.getPosition().latitude;
                currentLng = marker.getPosition().longitude;

                startEditActivity(filePaths.get(marker));
            }

        };

        manager.setRenderer(new MyClusterRenderer(this, worldMap, manager));

        if (!loaded) {
            PictureLoader loader = new PictureLoader(this);
            loader.execute();
        } else {
            //an Async task actually slows the loading! The loading is almost instantaneous.
            //Reloader reloader = new Reloader();
            //reloader.execute();
            ArrayList<String> files = dataRestorer.getFiles();
            ArrayList<Bitmap> bits = dataRestorer.getBitmaps();
            ArrayList<Float> lats = dataRestorer.getLats();
            ArrayList<Float> lngs = dataRestorer.getLngs();


            for (int i = 0; i < files.size(); i++) {
                Bitmap bitmap = bits.get(i);
                if (fileToReload != null && files.get(i).equals(fileToReload)) {
                    Bitmap bmp = BitmapFactory.decodeFile(fileToReload);
                    bitmap = Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth() / bmp.getHeight()) * 50), 50, false);

                    dataRestorer.replaceBitmap(i, bitmap);
                }
                MyItem item = new MyItem(lats.get(i), lngs.get(i));
                tempFilePaths.put(item, files.get(i));
                icons.put(item, bitmap);

                manager.addItem(item);
            }
        }


        worldMap.setOnCameraChangeListener(manager);
        worldMap.setOnMarkerClickListener(manager);
        worldMap.setOnInfoWindowClickListener(manager);

        manager.setOnClusterClickListener(this);
        manager.setOnClusterItemClickListener(this);

        /* enable MyLocation layer to show the current location as a blue dot */
        worldMap.setMyLocationEnabled(true);
        zoomToCurrentLocation();

        worldMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

    }


    private class Reloader extends AsyncTask<Void, Object, Void> {
        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
            //super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setProgressBarIndeterminateVisibility(false);
            //super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);

            MyItem item = new MyItem((Float) values[0], (Float) values[1]);
            synchronized (tempFilePaths) {
                tempFilePaths.put(item, (String) values[3]);
            }
            synchronized (icons) {
                icons.put(item, (Bitmap) values[2]);
            }
            synchronized (manager) {
                manager.addItem(item);
            }
        }


        @Override
        protected Void doInBackground(Void... params) {

            ArrayList<String> files = dataRestorer.getFiles();
            ArrayList<Bitmap> bits = dataRestorer.getBitmaps();
            ArrayList<Float> lats = dataRestorer.getLats();
            ArrayList<Float> lngs = dataRestorer.getLngs();

            for (int i = 0; i < files.size(); i++) {

                Bitmap bitmap = bits.get(i);
                if (fileToReload != null && files.get(i).equals(fileToReload)) {
                    Bitmap bmp = BitmapFactory.decodeFile(fileToReload);
                    bitmap = Bitmap.createScaledBitmap(bmp, (int) (((float) bmp.getWidth() / bmp.getHeight()) * 50), 50, false);
                    synchronized (dataRestorer) {
                        dataRestorer.replaceBitmap(i, bitmap);
                    }
                }

                publishProgress(lats.get(i), lngs.get(i), bitmap, files.get(i));
            }

            return null;
        }
    }

    //show the fragment
    private void showIconDialog(Collection<MyItem> collection) {
        FragmentManager fm = getFragmentManager();
        MapImageViewer editNameDialog = new MapImageViewer();

        //turn the collection into an ArrayList so we can iterate through it
        ArrayList<MyItem> newCollection = new ArrayList<MyItem>(collection);
        ArrayList<String> files = new ArrayList<String>();

        ArrayList<Bitmap> images = new ArrayList<Bitmap>();

        for (int i = 0; i < newCollection.size(); i++) {
            files.add(tempFilePaths.get(newCollection.get(i)));
            images.add(icons.get(newCollection.get(i)));
        }

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("files", files);
        bundle.putParcelableArrayList("images", images);
        editNameDialog.setArguments(bundle);

        editNameDialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Dialog_NoActionBar_MinWidth);
        editNameDialog.show(fm, "Images");
    }

    public void onUserSelectValue(String s) {
        for (Marker m : filePaths.keySet()) {
            if (filePaths.get(m).equals(s)) {
                currentLat = m.getPosition().latitude;
                currentLng = m.getPosition().longitude;
                break;
            }
        }
        startEditActivity(s);
    }


    protected void startEditActivity(String s) {
        fileToReload = s;

        Intent editIntent = new Intent(MapActivity.this, EditActivity.class);

        editIntent.putExtra("filename", s);
        startActivity(editIntent);

    }

    //credit http://androidfreakers.blogspot.com/2013/08/display-custom-info-window-with.html
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private View view, listview;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.info_window_layout, null);
            listview = getLayoutInflater().inflate(R.layout.info_window_layout, null);

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
            return view;
        }


        @Override
        public View getInfoWindow(Marker marker) {
            return null;//goes to getInfoContents if null
        }


    }


    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Party's over.", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection Failed. Check your connection.", Toast.LENGTH_SHORT).show();
    }

    private void zoomToCurrentLocation() {
        Location myLoc;
        LatLng myGeoLoc;
        myLoc = mapClient.getLastLocation();
        if (currentLng == 0.0 && currentLat == 0.0)
            myGeoLoc = new LatLng(myLoc.getLatitude(), myLoc.getLongitude());
        else
            myGeoLoc = new LatLng(currentLat, currentLng);

    /* zoom level 15: street level. Smaller number zoom-out, bigger: zoom-in */
        CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(myGeoLoc, zoomlevel);

    /* animate camera from (0,0) to current location in 3 seconds */
        worldMap.animateCamera(camUpdate, 3000, null);
    }


    public class MyItem implements ClusterItem {
        private final LatLng mPosition;

        public MyItem(double lat, double lng) {
            mPosition = new LatLng(lat, lng);
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }
    }


    //TODO: credit http://stackoverflow.com/questions/21876809/android-maps-utility-library-for-android
    class MyClusterRenderer extends DefaultClusterRenderer<MyItem> {

        public MyClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<MyItem> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) {
            super.onBeforeClusterItemRendered(item, markerOptions);

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icons.get(item)));

        }

        @Override
        protected void onClusterItemRendered(MyItem clusterItem, Marker marker) {
            super.onClusterItemRendered(clusterItem, marker);

            filePaths.put(marker, tempFilePaths.get(clusterItem));
            //here you have access to the marker itself
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        zoomlevel = worldMap.getCameraPosition().zoom;
        outState.putFloat("zoom", zoomlevel);
        outState.putBoolean("loaded", loaded);

        outState.putDouble("lat", currentLat);
        outState.putDouble("lng", currentLng);
        if (loaded)
            dataRestorer.saveData(outState);
        outState.putString("redo", fileToReload);
    }
}