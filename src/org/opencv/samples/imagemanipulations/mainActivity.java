package org.opencv.samples.imagemanipulations;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.*;
import android.widget.*;
import org.opencv.android.*;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class mainActivity extends Activity implements CvCameraViewListener2, View.OnClickListener{
    private static final String  TAG                 = "OCVSample::Activity";

    //holds the menu items
    private ArrayList<Map<String, Object>>  menuItems;
    private boolean menuInitialized = false;
    //private CameraBridgeViewBase mOpenCvCameraView;
    private PictureCameraView    mOpenCvCameraView;

    private Size                 mSize0;

    private Mat                  mIntermediateMat;
    private Mat                  mMat0;
    private MatOfInt             mChannels[];
    private MatOfInt             mHistSize;
    private int                  mHistSizeNum = 25;
    private MatOfFloat           mRanges;
    private Scalar               mColorsRGB[];
    private Scalar               mColorsHue[];
    private Scalar               mWhilte;
    private Point                mP1;
    private Point                mP2;
    private float                mBuff[];
    //private Mat                  mSepiaKernel;

    //public static int           viewMode = FilterApplier.VIEW_MODE_RGBA;

    // 3/18/14 10:00 AM <-
    //private HorizontalScrollView filterScroll;
    // holds the row of filters
    //private LinearLayout       scrollLayout;

    // ->

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // at this point, we can start using the library, so add the filters (this is a temporary hack, since the filters are
                    // going to be added on a STATIC image -- so, really, the scroll view should be in the image editor activity, not the
                    // picture-taking activity, where we currently have it)
                    //addFiltersToScrollView(new Mat());
                    mOpenCvCameraView.enableView();
                    //mOpenCvCameraView.setOnTouchListener(mainActivity.this);
                    mOpenCvCameraView.setOnClickListener(mainActivity.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };





    // Handles the scroll view's filter clicks (and the camera clicks)
    @Override
    public void onClick(View v) {

        if (v == mOpenCvCameraView) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            String currentDateandTime = sdf.format(new Date());
            File file = getFilesDir();
            String path = file.getPath();
            String fileName = path + "/sample_picture_" + currentDateandTime + ".jpg";
            //String fileName = Environment.getExternalStorageDirectory().getPath() +
            //        "/sample_picture_" + currentDateandTime + ".jpg";

            mOpenCvCameraView.takePicture(fileName);

            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();

            //add a partially completed menu item to the menu (once the options menu is selected, the process will be finished)
            Map<String, Object> newun = new HashMap<String, Object>();
            newun.put("filename", fileName);
            menuItems.add(newun);

        }
    }


    public mainActivity() {
        /* ".getClass" will show up as an error but it still works! */
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.emulsify_camera_view);

        mOpenCvCameraView = (PictureCameraView) findViewById(R.id.image_manipulations_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //initialize the arraylist of menu items
        menuItems = new ArrayList<Map<String, Object>>();

        // delete internal memory
        File dir = getFilesDir();
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
        // 3/18/14 10:00 AM <-
        // initialize the horizontal scroller (filterScroll) and its linear layout
        //filterScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
        //scrollLayout = (LinearLayout) findViewById(R.id.linearLayout);
        // ->
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

            menu.clear();
            menuInitialized = true;
            menu.add("Pictures taken:");

            for (int i = 0; i < menuItems.size(); i++) {
                Map<String, Object> m = menuItems.get(i);
                if (m.get("filename").equals("delete")) {
                    menuItems.remove(i);
                    //there should only be one item to delete at a time
                    break;
                }
            }

            for (int i = 0; i < menuItems.size(); i++) {
                Map<String, Object> m = menuItems.get(i);
                    String text = (String) m.get("filename");
                    int c = 0;
                    for (int k = 0; k < text.length(); k++) {
                        if (text.charAt(k) == '/') c = k;
                    }
                    //add the submenu
                    m.put("menu", menu.addSubMenu(text.substring(c+1)));
                    SubMenu subMenu = (SubMenu) m.get("menu");
                    subMenu.add("edit");
                    subMenu.add("delete");

            }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    //TODO:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        for (int i = 0; i < menuItems.size(); i++) {
            Map<String, Object> m = menuItems.get(i);
            SubMenu temp = (SubMenu) m.get("menu");
            if (temp != null) {
                for (int k = 0; k < temp.size(); k++) {
                    if (temp.getItem(k) == item) {
                        if (item.getTitle().equals("delete")) {
                            Toast.makeText(this, m.get("fileName") + " deleted", Toast.LENGTH_SHORT).show();
                            File file = new File((String) m.get("filename"));
                            file.delete();
                            //deleteFile((String) m.get("filename")); //I'm not sure if this will work all of the time
                            // (or, for that matter, if the above way will always work, either)
                            m.put("filename", "delete"); //the menu item will be destroyed next time the menu is opened
                        } else if (item.getTitle().equals("edit")) {
                            //fire up the image editor
                            Intent intent = new Intent(this, editActivity.class);
                            ArrayList<String> files = new ArrayList<String>();

                            for (int l = 0; l < menuItems.size(); l++) {
                                String f = (String) menuItems.get(l).get("filename");
                                files.add((String) menuItems.get(l).get("filename"));
                            }
                            // pass the array of filenames to the editor activity
                            intent.putExtra("filename", files);
                            startActivity(intent);

                        }
                    }
                }
            }
        }
        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[] { new MatOfInt(0), new MatOfInt(1), new MatOfInt(2) };
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0  = new Mat();
        mColorsRGB = new Scalar[] { new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255) };
        mColorsHue = new Scalar[] {
                new Scalar(255, 0, 0, 255),   new Scalar(255, 60, 0, 255),  new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255),  new Scalar(20, 255, 0, 255),  new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255),  new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255),  new Scalar(0, 0, 255, 255),   new Scalar(64, 0, 255, 255),  new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255),  new Scalar(255, 0, 0, 255)
        };
        mWhilte = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

        // This code has been moved to FilterApplier
        // Fill sepia kernel
        //mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        //mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        //mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        //mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        //mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
    }

    public void onCameraViewStopped() {
        // Explicitly deallocate Mats
        if (mIntermediateMat != null)
            mIntermediateMat.release();

        mIntermediateMat = null;
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Size sizeRgba = rgba.size();

        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = cols / 8;
        int top = rows / 8;

        int width = cols * 3 / 4;
        int height = rows * 3 / 4;

        /*switch (mainActivity.viewMode) {
        case FilterApplier.VIEW_MODE_RGBA:
            break;

        case FilterApplier.VIEW_MODE_CANNY:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            FilterApplier.applyFilter(FilterApplier.VIEW_MODE_CANNY, rgbaInnerWindow);
            rgbaInnerWindow.release();
            break;

        case FilterApplier.VIEW_MODE_SOBEL:
            Mat gray = inputFrame.gray();
            Mat grayInnerWindow = gray.submat(top, top + height, left, left + width);
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);

            FilterApplier.applyFilter(FilterApplier.VIEW_MODE_SOBEL, rgbaInnerWindow, grayInnerWindow);
            grayInnerWindow.release();
            rgbaInnerWindow.release();
            break;

        case FilterApplier.VIEW_MODE_SEPIA:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            FilterApplier.applyFilter(FilterApplier.VIEW_MODE_SEPIA, rgbaInnerWindow);
            rgbaInnerWindow.release();
            break;

        case FilterApplier.VIEW_MODE_ZOOM:
            Mat zoomCorner = rgba.submat(0, rows / 2 - rows / 10, 0, cols / 2 - cols / 10);
            Mat mZoomWindow = rgba.submat(rows / 2 - 9 * rows / 100, rows / 2 + 9 * rows / 100, cols / 2 - 9 * cols / 100, cols / 2 + 9 * cols / 100);
            FilterApplier.applyFilter(FilterApplier.VIEW_MODE_ZOOM, mZoomWindow, zoomCorner);
            zoomCorner.release();
            mZoomWindow.release();
            break;

        case FilterApplier.VIEW_MODE_PIXELIZE:
            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            FilterApplier.applyFilter(FilterApplier.VIEW_MODE_PIXELIZE, rgbaInnerWindow);
            rgbaInnerWindow.release();
            break;

        case FilterApplier.VIEW_MODE_POSTERIZE:

            //Imgproc.cvtColor(rgbaInnerWindow, mIntermediateMat, Imgproc.COLOR_RGBA2RGB);
            //Imgproc.pyrMeanShiftFiltering(mIntermediateMat, mIntermediateMat, 5, 50);
            //Imgproc.cvtColor(mIntermediateMat, rgbaInnerWindow, Imgproc.COLOR_RGB2RGBA);

            rgbaInnerWindow = rgba.submat(top, top + height, left, left + width);
            FilterApplier.applyFilter(FilterApplier.VIEW_MODE_POSTERIZE, rgbaInnerWindow);
            rgbaInnerWindow.release();
            break;
        }
*/
        return rgba;
    }

}
