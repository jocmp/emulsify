package org.opencv.samples.imagemanipulations;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
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

    public static int           viewMode = FilterApplier.VIEW_MODE_RGBA;


    ImageView mainPhoto;
    Bitmap mainPhotoBitmap;

    private boolean initialized = false;

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



    public void initialize() {
        Intent temp = getIntent();
        if (temp != null) {
            ArrayList<String> filenames = temp.getStringArrayListExtra("filename");

            mainPhotoBitmap = BitmapFactory.decodeFile(filenames.get(0));
            // set the main image
            mainPhoto.setImageBitmap(mainPhotoBitmap);

            Mat mat = new Mat (mainPhotoBitmap.getWidth(), mainPhotoBitmap.getHeight(), mainPhotoBitmap.getDensity());// CvType.CV_8UC1);
            Utils.bitmapToMat(mainPhotoBitmap, mat);
            double width = mat.size().width;
            double height = mat.size().height;
            //int width2 = R.dimen.filterImageWidth;
            //int height2 = R.dimen.filterImageHeight;

            Mat filterMat = new Mat();

            Imgproc.resize(mat, filterMat, new Size(), (double) (FILTER_HEIGHT * (width/ height))/width, (double) (FILTER_HEIGHT)/height, Imgproc.INTER_NEAREST);

            // add the filters now
            addFiltersToScrollView(filterMat);


            for (int i = 0; i < filenames.size(); i++) {
                Bitmap bit = BitmapFactory.decodeFile(filenames.get(i));
                mat = new Mat(bit.getWidth(), bit.getHeight(), bit.getDensity());
                Utils.bitmapToMat(bit, mat);

                width = mat.size().width;
                height = mat.size().height;

                Mat imageMat = new Mat();
                Imgproc.resize(mat, imageMat, new Size(), (double) (IMAGE_HEIGHT * (width/ height))/width, (double) (IMAGE_HEIGHT)/height, Imgproc.INTER_NEAREST);

                PictureScrollElement p = new PictureScrollElement(this);
                p.initialize(filenames.get(i), imageMat);
                p.setOnClickListener(this);
                imageScrollLayout.addView(p);
            }
        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.photo_editor);


        // initialize the horizontal scroller (filterScroll) and its linear layout
        filterScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        filterScrollLayout = (LinearLayout) findViewById(R.id.linearLayout);

        imageScroll = (ScrollView) findViewById(R.id.pictureScrollView);
        imageScrollLayout = (LinearLayout) findViewById(R.id.pictureLinearLayout);


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
        e.initialize(FilterApplier.VIEW_MODE_RGBA, "Normal(temp)", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_CANNY, "Canny", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_SEPIA, "Sepia", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_SOBEL, "Sobel", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_ZOOM, "Zoom", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_PIXELIZE, "Pixelize", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

        e = new FilterScrollElement(this);
        e.initialize(FilterApplier.VIEW_MODE_POSTERIZE, "Posterize", image);
        e.setOnClickListener(this);
        filterScrollLayout.addView(e);

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

        for (int i = 0; i < imageScrollLayout.getChildCount(); i++) {
            if (v == imageScrollLayout.getChildAt(i)) {
                PictureScrollElement e = (PictureScrollElement) imageScrollLayout.getChildAt(i);
                setImages(e.getFile());
                break;
            }
        }
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