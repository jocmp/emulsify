package edu.gvsu.cis.campbjos.emulsify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;
import edu.gvsu.cis.emulsify.R;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * @author Emulsify Team
 * @version Update 2014-04-09
 */
@SuppressWarnings("deprecation") //TODO make sure there are no deprecated methods
public class EditActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "Emulsify:Image Editor";
    private boolean initialized = false;
    /* Image */
    private Bitmap originalBitmap;
    private Bitmap viewedBitmap;
    /* Filter */
    public static int viewMode = FilterApplier.VIEW_MODE_RGBA;
    private final int FILTER_HEIGHT = 80;
    private final int IMAGE_HEIGHT = 80;
    private int currentImageIndex = -1;
    // tells code that side scroll view is active
    private boolean hasScrollView = false;
    private boolean hasUnsavedChanges = false;

    private ImageView mainPhoto;
    private HorizontalScrollView filterScroll;
    private LinearLayout filterScrollLayout;
    private LinearLayout imageScrollLayout;
    private ScrollView imageScroll;
    private ShareActionProvider shareProvider;
    private String originalBitmapString, viewedBitmapString;
    private OnSwipeTouchListener onSwipeTouchListener;
    /* Menu Share Provider */
    private MenuItem shareMenuItem;
    private Uri shareUri;

    @Override
    public void onBackPressed() {
        //final Intent GOHOME = new Intent(this, HomeActivity.class);
        if (hasUnsavedChanges) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.exit_dialog)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialog, int which) {
                                    if (which == -1)
                                        finish();
                                }
                            })
                    .setNegativeButton(android.R.string.no, null).show();
        } else
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_actionbar, menu);
        return true;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //TODO: prevent the user from using openCV methods until openCV is initialized (for instance, after
                    /* the screen is turned back on)
                       this can be accomplished by toggling the click listeners */
                    if (!initialized) {
                        initialize();
                        initialized = true;
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void setImages(String filename) {
        originalBitmap = BitmapFactory.decodeFile(filename);
        /* Set the Main Image */
        mainPhoto.setImageBitmap(originalBitmap);
        /* Reset the filters */
        filterScrollLayout.removeAllViews();

        Mat mat = new Mat(originalBitmap.getWidth(), originalBitmap.getHeight(), originalBitmap.getDensity());
        //from mat --> CvType.CV_8UC1);
        Utils.bitmapToMat(originalBitmap, mat);

        double width = mat.size().width;
        double height = mat.size().height;

        Mat filterMat = new Mat();

        Imgproc.resize(mat,
                filterMat, new Size(),
                (FILTER_HEIGHT
                        * (width / height))
                        / width, (double) (FILTER_HEIGHT)
                / height, Imgproc.INTER_NEAREST);
        addFiltersToScrollView(filterMat);
    }

    public class PictureLoader extends AsyncTask<ArrayList<String>, Object, Void> {
        private Context mContext;

        public PictureLoader(Context context) {
            mContext = context;
        }


        @Override
        protected Void doInBackground(ArrayList<String>... params) {
            ArrayList<String> filenames = params[0];
            for (int i = 0; i < filenames.size(); i++) {
                Bitmap bit = BitmapFactory.decodeFile(filenames.get(i));
                Mat mat = new Mat(bit.getWidth(), bit.getHeight(), bit.getDensity());
                Utils.bitmapToMat(bit, mat);

                double width = mat.size().width;
                double height = mat.size().height;

                Mat imageMat = new Mat();
                Imgproc.resize(mat, imageMat, new Size(),
                        (double) ((IMAGE_HEIGHT * (width / height))
                                / width), (double) ((IMAGE_HEIGHT) / height), Imgproc.INTER_NEAREST);

                //"percent" = ((double) (i+1)/ filenames.size()));
                publishProgress(filenames.get(i), imageMat, i);


            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);

            String filename = (String) values[0];
            Mat mat = (Mat) values[1];
            int index = (Integer) values[2];

            PictureScrollElement p = new PictureScrollElement(mContext);
            p.initialize(filename, mat);
            if (index == 0) p.box();

            p.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int i = 0; i < imageScrollLayout.getChildCount(); i++) {
                        PictureScrollElement e = (PictureScrollElement) imageScrollLayout.getChildAt(i);
                        if (i != currentImageIndex) {
                            if (v == e) {
                                e.box();
                                setImages(e.getFile());
                                PictureScrollElement a =
                                        (PictureScrollElement) imageScrollLayout.getChildAt(currentImageIndex);
                                a.unBox();
                                currentImageIndex = i;
                                onSwipeTouchListener.putIndex(-1);// base);
                            } else if (e.isBoxed()) {
                                e.unBox();
                            }
                        }
                    }
                }
            });

            imageScrollLayout.addView(p);
        }
    }

    public void initialize() {
        Intent temp = getIntent();
        if (temp != null) {
            ArrayList<String> filenames = temp.getStringArrayListExtra("filename");

            Bitmap bm = null;
            if (filenames != null) {
                bm = BitmapFactory.decodeFile(filenames.get(0));

                if (filenames.size() > 1) {
                    currentImageIndex = 0;
                    hasScrollView = true;
                    PictureLoader loader = new PictureLoader(this);
                    loader.execute(filenames);
                }

            } else {
                String filename = temp.getStringExtra("filename");
                bm = BitmapFactory.decodeFile(filename);
            }
            try {
                originalBitmap = bm;
                viewedBitmap = originalBitmap.copy(originalBitmap.getConfig(), originalBitmap.isMutable());

                // set the main image
                mainPhoto.setImageBitmap(originalBitmap);

                Mat mat = new Mat(originalBitmap.getWidth(), originalBitmap.getHeight(), originalBitmap.getDensity());
                Utils.bitmapToMat(originalBitmap, mat);
                double width = mat.size().width;
                double height = mat.size().height;

                Mat filterMat = new Mat();

                Imgproc.resize(mat, filterMat, new Size(),
                        (FILTER_HEIGHT * (width / height))
                                / width, (double) (FILTER_HEIGHT) / height, Imgproc.INTER_NEAREST);

                // add the filters now
                addFiltersToScrollView(filterMat);
            } catch (NullPointerException e) {
                Log.e("NullPointerException", "Initialize bmp null");
                Toast.makeText(this, "Loading error. Try again!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editor);

        shareUri = null;

        // initialize the horizontal scroller (filterScroll) and its linear layout
        filterScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        filterScrollLayout = (LinearLayout) findViewById(R.id.linearLayout);

        imageScroll = (ScrollView) findViewById(R.id.pictureScrollView);
        imageScrollLayout = (LinearLayout) findViewById(R.id.pictureLinearLayout);

        onSwipeTouchListener = new OnSwipeTouchListener(this, imageScrollLayout) {
            public void onSwipeRight() {
                // TODO: ask the user if the photo should be deleted IF the photo has been saved to the gallery (they
                // temporarily lie in the app's storage space, which should be cleaned out upon exit of the editor activity)
                // The above comment ONLY applies if CameraActivity has been used to take the pictures.
                // This activity has been developed without much consideration for CameraActivity (and only with minimal
                // consideration of the scroll view to the left -- TODO: update the upload section so that the proper image is
                // selected)
                if (index != -1) {
                    imageScrollLayout.removeViewAt(index);
                    if (imageScrollLayout.getChildCount() == 0) {
                        //nothing left to edit!
                        //TODO: clear the app's storage space before exit
                        onBackPressed();
                        //finish();
                    } else if (index == currentImageIndex) {
                        PictureScrollElement e = null;
                        if (index < imageScrollLayout.getChildCount()) {
                            e = (PictureScrollElement) imageScrollLayout.getChildAt(currentImageIndex);
                        } else {
                            e = (PictureScrollElement) imageScrollLayout.getChildAt(--currentImageIndex);
                        }
                        e.box();
                        setImages(e.getFile());

                    } else if (index < currentImageIndex) {
                        currentImageIndex--;
                    }
                    index = -1;
                }
            }

        };
        imageScrollLayout.setOnTouchListener(onSwipeTouchListener);

        mainPhoto = (ImageView) findViewById(R.id.Picture);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case R.id.action_share:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this,
                            "Image saved and shared.",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_save:
                savePhoto();
                return true;
            case R.id.action_undo:
                viewedBitmap =
                        originalBitmap.copy(originalBitmap.getConfig(), originalBitmap.isMutable());
                mainPhoto.setImageBitmap(viewedBitmap);

                for (int j = 0; j < filterScrollLayout.getChildCount(); j++) {
                    FilterScrollElement e = (FilterScrollElement) filterScrollLayout.getChildAt(j);
                    e.setImage(viewedBitmap, FILTER_HEIGHT);
                }

                hasUnsavedChanges = false; //we just undid all the changes...
                return true;
            case R.id.action_share:
                savePhoto();
                startActivity(Intent.createChooser(createShareIntent(getPhotoUri()), "Share..."));
                return true;
            case R.id.action_imgur:
                //TODO: properly select the image if the scroll view is in effect
                savePhoto();
                Toast.makeText(this, "Uploading...", Toast.LENGTH_LONG).show();
                new ImgurUploadTask(getPhotoUri()).execute();
                return true;
        }
        return true;
    }

    synchronized public static final String convert(float latitude) {
        StringBuilder sb = new StringBuilder(20);

        latitude = Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude * 1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }

    private void addFiltersToScrollView(Mat image) {

        FilterScrollElement e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_CANNY, "Canny", image);
        e.setPadding(0,0,20,0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_GRAY, "Black & White", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);


        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_SEPIA, "Sepia", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_PIXELIZE, "Pixelize", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_POSTERIZE, "Posterize", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_INVERSE, "Inverse", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_WASH, "Washed Out", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_SAT, "Saturate", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_HUE, "Hue Rotate", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_BLUE, "Sad Day", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_RED, "Warm Day", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_PURPLE, "Purple Haze", image);
        e.setPadding(0, 0, 20, 0);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < filterScrollLayout.getChildCount(); i++) {
            if (v == filterScrollLayout.getChildAt(i)) {
                FilterScrollElement e = (FilterScrollElement) filterScrollLayout.getChildAt(i);
                viewMode = e.getFilterType();
                applyFilter();
                for (int j = 0; j < filterScrollLayout.getChildCount(); j++) {
                    e = (FilterScrollElement) filterScrollLayout.getChildAt(j);
                    //e.applyFilter(viewMode);
                    e.setImage(viewedBitmap, FILTER_HEIGHT);
                }
                break;
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
        ev.getPointerCoords(0, coords);
        int x = (int) coords.x;
        int y = (int) coords.y;

        int base = -1;
        for (int i = 0; i < imageScrollLayout.getChildCount(); i++) {
            if (imageScrollLayout.getChildAt(i).getClass() == PictureScrollElement.class && base == -1)
                base = i;
            PictureScrollElement a = (PictureScrollElement) imageScrollLayout.getChildAt(i);
            int y1 = a.getTop() + getStatusBarHeight() + getActionBar().getHeight();
            int y2 = a.getBottom() + getStatusBarHeight() + getActionBar().getHeight();
            // tests the bounds of each image to determine where the swipe (if it WAS a swipe) took place
            if (y >= y1 && y < y2 && x >= a.getLeft() && x < a.getRight()) {
                onSwipeTouchListener.putIndex(i);// base);
                break;
            }
        }

        onSwipeTouchListener.getGestureDetector().onTouchEvent(ev);

        return super.dispatchTouchEvent(ev);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void applyFilter() {
        Mat mat = new Mat(originalBitmap.getWidth(), originalBitmap.getHeight(), originalBitmap.getDensity());
        Utils.bitmapToMat(viewedBitmap, mat);

        switch (viewMode) {
            case FilterApplier.VIEW_MODE_SOBEL:
                break;

            case FilterApplier.VIEW_MODE_ZOOM:
                break;

            default:
                // notify that there are unsaved changes afoot
                hasUnsavedChanges = true;
                FilterApplier.applyFilter(viewMode, mat, mat);
        }

        Utils.matToBitmap(mat, viewedBitmap);
        mainPhoto.setImageBitmap(viewedBitmap);
    }


    public Intent createShareIntent(Uri photo) {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, photo);
        shareIntent.setType("image/png");

        return shareIntent;
    }

    private String createImageName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentTime = sdf.format(new Date());
        String imageString = "emulsify_" + currentTime;

        return imageString;
    }

    /* IMGUR API CLASS */
    private class ImgurUploadTask extends AsyncTask<Void, Void, String> {

        //private final String TAG = ImgurUploadTask.class.getSimpleName();

        private static final String UPLOAD_URL = "https://api.imgur.com/3/image";

        private ClipboardManager clipboard;
        private Uri mImageUri;
        private String imageId;

        public ImgurUploadTask(Uri imageUri) {
            mImageUri = imageUri;
            clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            imageId = null;
        }

        @Override
        protected String doInBackground(Void... params) {
            InputStream imageIn;
            try {
                imageIn = getContentResolver().openInputStream(mImageUri);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "could not open InputStream", e);
                return null;
            }

            HttpURLConnection conn = null;
            InputStream responseIn = null;

            try {
                conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
                conn.setDoOutput(true);

                edu.gvsu.cis.campbjos.emulsify.Imgur.ImgurAuthorization.getInstance().addToHttpURLConnection(conn);

                OutputStream out = conn.getOutputStream();
                copy(imageIn, out);
                out.flush();
                out.close();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    responseIn = conn.getInputStream();
                    return onInput(responseIn);
                } else {
                    Log.i(TAG, "responseCode=" + conn.getResponseCode());
                    responseIn = conn.getErrorStream();
                    StringBuilder sb = new StringBuilder();
                    Scanner scanner = new Scanner(responseIn);
                    while (scanner.hasNext()) {
                        sb.append(scanner.next());
                    }
                    Log.i(TAG, "error response: " + sb.toString());
                    return null;
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error during POST", ex);
                return null;
            } finally {
                try {
                    responseIn.close();
                } catch (Exception ignore) {
                }
                try {
                    conn.disconnect();
                } catch (Exception ignore) {
                }
                try {
                    imageIn.close();
                } catch (Exception ignore) {
                }
            }
        }

        private int copy(InputStream input, OutputStream output) throws IOException {
            byte[] buffer = new byte[8192];
            int count = 0;
            int n = 0;
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        }

        private String onInput(InputStream in) throws Exception {
            StringBuilder sb = new StringBuilder();
            Scanner scanner = new Scanner(in);
            while (scanner.hasNext()) {
                sb.append(scanner.next());
            }

            JSONObject root = new JSONObject(sb.toString());
            String id = root.getJSONObject("data").getString("id");
            String deletehash = root.getJSONObject("data").getString("deletehash");

            Log.i(TAG, "new imgur url: http://imgur.com/" + id + " (delete hash: " + deletehash + ")");

            imageId = id;
            return id;
        }

        @Override
        protected void onPostExecute(String s) {
            clipboard.setPrimaryClip
                    (ClipData.newPlainText("new imgur url", "http://imgur.com/" + imageId));
            imageId = null;

            if (clipboard.hasPrimaryClip()) {
                Toast.makeText(getApplicationContext(),
                        "Photo uploaded.\nLink pasted to Clipboard.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void savePhoto() {
        //http://stackoverflow.com/questions/8078892/stop-saving-photos-using-android-native-camera

        String path = null;
        if (hasScrollView) {//currentImageIndex != -1) {
            // TODO: test this code (the second condition has been tested)
            PictureScrollElement a = (PictureScrollElement) imageScrollLayout.getChildAt(currentImageIndex);
            path = a.getFile();
        } else {
            Intent temp = getIntent();
            path = temp.getStringExtra("filename");
        }


        ContentValues values = new ContentValues();
        try {
            File f = new File(path);
            ExifInterface exif = null;
            float[] d = null;

            if (f.exists()) { //it should ALWAYS exist
                String originalFilePath = f.getAbsolutePath();
                exif = new ExifInterface(f.getAbsolutePath());
                d = new float[2];
                exif.getLatLong(d);

                //TODO: credit http://stackoverflow.com/questions/6390163/deleting-a-gallery-image-after-camera-intent-photo-taken
                String[] filePathColumn = { //MediaStore.Images.ImageColumns.SIZE,
                        //MediaStore.Images.ImageColumns.DISPLAY_NAME,
                        MediaStore.Images.ImageColumns.DATA,
                        BaseColumns._ID,};

                Uri u = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

                Cursor cursor = getContentResolver().query(u, filePathColumn, null, null, null);

                cursor.moveToFirst();
                boolean cont = true;
                if (cursor.getCount() != 0) {
                    do {
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String filePath = cursor.getString(columnIndex);
                        if (filePath.equals(originalFilePath)) {
                            ContentResolver cr = getContentResolver();
                            cr.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                    BaseColumns._ID + "=" + cursor.getString(1), null);

                            cont = false;
                        }
                    } while (cursor.moveToNext() && cont);


                    cursor.close();
                }


                f.delete();
            }


            FileOutputStream fos = new FileOutputStream(new File(path));
            viewedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            exif = new ExifInterface(f.getAbsolutePath());

            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convert(d[0]));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convert(d[1]));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, d[0] < 0.0F ? "S" : "N");
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, d[1] < 0.0F ? "W" : "E");

            exif.saveAttributes();

            hasUnsavedChanges = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, path);
        values.put(MediaStore.Images.Media.TITLE, "emulsify");
        this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Toast.makeText(this,
                "Image saved.",
                Toast.LENGTH_SHORT).show();
                /*
                MediaStore.Images.Media.insertImage(getContentResolver(),
                        viewedBitmap,
                        name,
                        "Generated by emulsify!");
                */
    }

    private Uri getPhotoUri() {
        Intent photoIntent = getIntent();
        String path = photoIntent.getStringExtra("filename");
        File f = new File(path);

        return Uri.fromFile(f);
    }
}