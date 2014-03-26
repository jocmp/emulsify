package org.opencv.samples.imagemanipulations;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Reuben on 3/23/14.
 */
public class editActivity extends Activity implements View.OnClickListener{
    private static final String  TAG                 = "Emulsify:Image Editor";

    private final int FILTER_HEIGHT = 75;
    private final int IMAGE_HEIGHT = 75;

    private HorizontalScrollView filterScroll;
    // holds the row of filters
    private LinearLayout filterScrollLayout;

    private ScrollView imageScroll;
    private LinearLayout imageScrollLayout;
    private int currentImageIndex = 0;

    public static int           viewMode = FilterApplier.VIEW_MODE_RGBA;


    ImageView mainPhoto;
    Bitmap mainPhotoBitmap;

    private boolean initialized = false;

    OnSwipeTouchListener onSwipeTouchListener;

    ArrayList<PictureScrollElement> ps = new ArrayList<PictureScrollElement>();

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //TODO: prevent the user from using openCV methods until openCV is initialized (for instance, after
                    //the screen is turned back on)
                    //this can be accomplished by toggling the click listeners
                    if (!initialized) {
                        initialize();
                        initialized = true;
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void setImages(String filename) {
        mainPhotoBitmap = BitmapFactory.decodeFile(filename);
        // set the main image
        mainPhoto.setImageBitmap(mainPhotoBitmap);

        //reset the filters
        filterScrollLayout.removeAllViews();

        Mat mat = new Mat (mainPhotoBitmap.getWidth(), mainPhotoBitmap.getHeight(), mainPhotoBitmap.getDensity());// CvType.CV_8UC1);
        Utils.bitmapToMat(mainPhotoBitmap, mat);

        double width = mat.size().width;
        double height = mat.size().height;

        Mat filterMat = new Mat();

        Imgproc.resize(mat, filterMat, new Size(), (double) (FILTER_HEIGHT * (width/ height))/width, (double) (FILTER_HEIGHT)/height, Imgproc.INTER_NEAREST);
        addFiltersToScrollView(filterMat);
    }


    public class PictureLoader extends AsyncTask <ArrayList<String>, Object, Void> {
        private Context mContext;
        public PictureLoader (Context context){
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
                Imgproc.resize(mat, imageMat, new Size(), (double) ((IMAGE_HEIGHT * (width/ height))/width), (double) ((IMAGE_HEIGHT)/height), Imgproc.INTER_NEAREST);

                //"percent" = ((double) (i+1)/ filenames.size()));
                publishProgress(filenames.get(i), imageMat, i);


            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {//HashMap<String, Object>... values) {
            super.onProgressUpdate(values);
                //HashMap<String, Object> value = values[0];

                String filename = (String) values[0];
                Mat mat = (Mat) values[1];
                int index = (Integer) values[2];

                PictureScrollElement p = new PictureScrollElement(mContext);
                p.initialize(filename, mat);
                if (index == 0) p.box();

                //p.setDensity(bit.getDensity());
                p.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < imageScrollLayout.getChildCount(); i++) {
                            PictureScrollElement e = (PictureScrollElement) imageScrollLayout.getChildAt(i);
                            if (v == e) {
                                e.box();
                                setImages(e.getFile());
                                currentImageIndex = i;
                            } else if (e.isBoxed()) {
                                e.unBox();
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


            mainPhotoBitmap = BitmapFactory.decodeFile(filenames.get(0));

            //ContentValues values = new ContentValues();

            //values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            //values.put(MediaStore.Images.Media.MIME_TYPE, "image/bmp");
            //values.put(MediaStore.MediaColumns.DATA, mainPhotoBitmap);

            //this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            //MediaStore.Images.Media.insertImage(getContentResolver(), mainPhotoBitmap, "emulsify photo" , "Hello");
            // set the main image
            mainPhoto.setImageBitmap(mainPhotoBitmap);

            Mat mat = new Mat (mainPhotoBitmap.getWidth(), mainPhotoBitmap.getHeight(), mainPhotoBitmap.getDensity());// CvType.CV_8UC1);
            Utils.bitmapToMat(mainPhotoBitmap, mat);
            double width = mat.size().width;
            double height = mat.size().height;

            Mat filterMat = new Mat();

            Imgproc.resize(mat, filterMat, new Size(), (double) (FILTER_HEIGHT * (width/ height))/width, (double) (FILTER_HEIGHT)/height, Imgproc.INTER_NEAREST);

            // add the filters now
            addFiltersToScrollView(filterMat);

            PictureLoader loader = new PictureLoader(this);
            loader.execute(filenames);

       }
    }

    public void box(PictureScrollElement p) {

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_editor);


        // initialize the horizontal scroller (filterScroll) and its linear layout
        filterScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        filterScrollLayout = (LinearLayout) findViewById(R.id.linearLayout);

        imageScroll = (ScrollView) findViewById(R.id.pictureScrollView);
        imageScrollLayout = (LinearLayout) findViewById(R.id.pictureLinearLayout);

        onSwipeTouchListener = new OnSwipeTouchListener(this, imageScrollLayout) {
            public void onSwipeRight() {
                    // TODO: ask the user if the photo should be deleted IF the photo has been saved to the gallery (they
                // temporarily lie in the app's storage space, which should be cleaned out upon exit of the editor activity)
                    imageScrollLayout.removeViewAt(index);

                if (imageScrollLayout.getChildCount() == 0) {
                    //nothing left to edit!
                    //TODO: clear the app's storage space before exit
                    finish();
                } else if (index == currentImageIndex) {
                    PictureScrollElement e = null;
                    if (index < imageScrollLayout.getChildCount()) {
                         e = (PictureScrollElement) imageScrollLayout.getChildAt(currentImageIndex);
                    } else {
                         e = (PictureScrollElement) imageScrollLayout.getChildAt(--currentImageIndex);
                    }
                    e.box();
                    setImages(e.getFile());

                }
            }

        };
        imageScrollLayout.setOnTouchListener(onSwipeTouchListener);

        mainPhoto = (ImageView) findViewById(R.id.Picture);


    }


    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }



    private void addFiltersToScrollView(Mat image) {
        FilterScrollElement e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_CANNY, "Canny", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_GRAY, "B & W", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);


        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_SEPIA, "Sepia", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        /*e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_SOBEL, "Sobel", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);*/

        /*e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_ZOOM, "Zoom", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);*/

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_PIXELIZE, "Pixelize", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_INVERSE, "Inverse", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_WASH, "Washed Out", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_SAT, "Saturated", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_LUMIN, "Luminance", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        /*e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_POSTERIZE, "Posterize", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);*/

        // uncomment this code to test the scrolling feature
        /*
        for (int i = 0; i < 20; i++) {
        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_PIXELIZE, "Pixelize", image);
        scrollLayout.addView(e);
        }
        */
    }


    @Override
    public void onClick(View v) {
        for (int i = 0; i < filterScrollLayout.getChildCount(); i++) {
            if (v == filterScrollLayout.getChildAt(i)) {
                FilterScrollElement e = (FilterScrollElement) filterScrollLayout.getChildAt(i);
                viewMode = e.getFilterType();
                applyFilter();
                break;
            }
        }
    }

    //TODO: credit http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
        ev.getPointerCoords(0, coords);
        int x = (int) coords.x;
        int y = (int) coords.y;
        for (int i = 0; i < imageScrollLayout.getChildCount(); i++) {
            PictureScrollElement a = (PictureScrollElement) imageScrollLayout.getChildAt(i);

            // tests the bounds of each image to determine where the swipe (if it WAS a swipe) took place
            if (y >= a.getTop() && y < a.getBottom() && x >= a.getLeft() && x < a.getRight()) {
                    onSwipeTouchListener.putIndex(i-1);
                break;
            }
        }

        onSwipeTouchListener.getGestureDetector().onTouchEvent(ev);

        return super.dispatchTouchEvent(ev);
    }


    public void applyFilter() {
        Mat mat = new Mat(mainPhotoBitmap.getWidth(), mainPhotoBitmap.getHeight(), mainPhotoBitmap.getDensity());// CvType.CV_8UC1);
        Utils.bitmapToMat(mainPhotoBitmap, mat);

        switch (viewMode) {
        case FilterApplier.VIEW_MODE_SOBEL:
        break;

        case FilterApplier.VIEW_MODE_ZOOM:
        break;

        default:
            FilterApplier.applyFilter(viewMode, mat, mat);
        }

        //TODO: have a separate "filter" bitmap, so that the original doesn't get erased
        Utils.matToBitmap(mat, mainPhotoBitmap);
        mainPhoto.setImageBitmap(mainPhotoBitmap);
    }


    //TODO: add save feature
    //TODO: add undo feature
    //TODO: add rename feature
    //TODO: add share feature
    //TODO: more
}