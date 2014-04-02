package edu.gvsu.cis.emulsify;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Reuben, Sean, and Josiah between 3/19-3/24.
 */
public class FilterApplier {

    public static final int VIEW_MODE_RGBA = 0;
    public static final int VIEW_MODE_HIST = 1;
    public static final int VIEW_MODE_CANNY = 2;
    public static final int VIEW_MODE_SEPIA = 3;
    public static final int VIEW_MODE_SOBEL = 4;
    public static final int VIEW_MODE_ZOOM = 5;
    public static final int VIEW_MODE_PIXELIZE = 6;
    public static final int VIEW_MODE_POSTERIZE = 7;
    public static final int VIEW_MODE_GRAY = 8;
    public static final int VIEW_MODE_INVERSE = 9;
    public static final int VIEW_MODE_WASH = 10;
    public static final int VIEW_MODE_SAT = 11;
    public static final int VIEW_MODE_HUE = 12;
    public static final int VIEW_MODE_BLUE = 13;
    public static final int VIEW_MODE_RED = 14;
    public static final int VIEW_MODE_PURPLE = 15;

    private static Mat mSepiaKernel;
    private static Mat mGrayKernel;
    private static Mat mInverseKernel;
    private static Mat mWashKernel;
    private static Mat mSaturatedKernel;
    private static Mat mHueKernel;
    private static Mat mBlueKernel;
    private static Mat mRedKernel;
    private static Mat mPurpleKernel;

    static {
        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        //Fill B&W Kernel
        mGrayKernel = new Mat(4, 4, CvType.CV_32F);
        mGrayKernel.put(0, 0, /* R */0.33f, 0.33f, 0.33f, 0f);
        mGrayKernel.put(1, 0, /* G */0.33f, 0.33f, 0.33f, 0f);
        mGrayKernel.put(2, 0, /* B */0.33f, 0.33f, 0.33f, 0f);
        mGrayKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);

        //Fill Washed Out Kernel
        mWashKernel = new Mat(4, 4, CvType.CV_32F);
        mWashKernel.put(0, 0, /* R */0.8f, 0.0f, 0.0f, 0.2f);
        mWashKernel.put(1, 0, /* G */0.0f, 0.8f, 0.0f, 0.2f);
        mWashKernel.put(2, 0, /* B */0.0f, 0.0f, 0.8f, 0.2f);
        mWashKernel.put(3, 0, /* A */0.000f, 0.000f, 0.0f, 1.0f);

        //Fill Saturated Kernel
        mSaturatedKernel = new Mat(4, 4, CvType.CV_32F);
        mSaturatedKernel.put(0, 0, /* R */1.2f, 0.0f, 0.0f, -0.2f);
        mSaturatedKernel.put(1, 0, /* G */0.0f, 1.2f, 0.0f, -0.2f);
        mSaturatedKernel.put(2, 0, /* B */0.0f, 0.0f, 1.2f, -0.2f);
        mSaturatedKernel.put(3, 0, /* A */0.000f, 0.000f, 0.0f, 1.0f);

        //Fill Inverse Kernel
        mInverseKernel = new Mat(4, 4, CvType.CV_32F);
        mInverseKernel.put(0, 0, /* R */-1f, 0.0f, 0.0f, 1f);
        mInverseKernel.put(1, 0, /* G */0.0f, -1f, 0.0f, 1f);
        mInverseKernel.put(2, 0, /* B */0.0f, 0.0f, -1f, 1f);
        mInverseKernel.put(3, 0, /* A */0.000f, 0.000f, 0.0f, 1.0f);

        //Fill Hue Rotate Kernel
        mHueKernel = new Mat(4, 4, CvType.CV_32F);
        mHueKernel.put(0, 0, /* R */0.556f, -0.292f, 0.737f, 0.0f);
        mHueKernel.put(1, 0, /* G */0.186f, 1.005f, -0.191f, 0.0f);
        mHueKernel.put(2, 0, /* B */-0.527f, 0.803f, 0.724f, 0.0f);
        mHueKernel.put(3, 0, /* A */0.0f, 0.0f, 0.0f, 1.0f);

        //Fill Blue Kernel
        mBlueKernel = new Mat(4, 4, CvType.CV_32F);
        mBlueKernel.put(0, 0, /* R */0.5f, 0.0f, 0.0f, 0.0f);
        mBlueKernel.put(1, 0, /* G */0.5f, 0.5f, 0.4f, 0.0f);
        mBlueKernel.put(2, 0, /* B */1.2f, 0.7f, 1.0f, 0.0f);
        mBlueKernel.put(3, 0, /* A */0.0f, 0.0f, 0.0f, 1.0f);

        //Fill Blue Kernel
        mRedKernel = new Mat(4, 4, CvType.CV_32F);
        mRedKernel.put(0, 0, /* R */1.5, 0.0f, 0.0f, -0.025f);
        mRedKernel.put(1, 0, /* G */0.0f, 0.5f, 0.4f, 0.0f);
        mRedKernel.put(2, 0, /* B */0.0f, 0.0f, 0.0f, 0.0f);
        mRedKernel.put(3, 0, /* A */0.0f, 0.0f, 0.0f, 1.0f);

        //Fill Purple Kernel
        mPurpleKernel = new Mat(4, 4, CvType.CV_32F);
        mPurpleKernel.put(0, 0, /* R */0.3f, 0.05f, 0.1f, 0.1f);
        mPurpleKernel.put(1, 0, /* G */0.25f, 0.5f, 0.1f, 0.01f);
        mPurpleKernel.put(2, 0, /* B */0.2f, 0f, 1.5f, -0.025f);
        mPurpleKernel.put(3, 0, /* A */0.0f, 0.0f, 0.0f, 1.0f);

    }

    public static void applyFilter(int mode, Mat... images) {
        // an "intermediate" matrix
        Mat mIntermediateMat = new Mat();

        // images[0] is assumed to be an rgba image
        Mat rgbaWindow = images[0];

        //the "images" elements are aliases, remember -- the matrices on the other end will also be changed
        //we would have a problem if an alias reference were to be changed in this method, but that doesn't seem to happen (good to keep
        //in mind, though)
        switch (mode) {
            case VIEW_MODE_RGBA:
                break;

            case VIEW_MODE_HIST:
                break;

            case VIEW_MODE_CANNY:
                Imgproc.Canny(rgbaWindow, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, rgbaWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                break;

            case VIEW_MODE_SEPIA:
                Core.transform(rgbaWindow, rgbaWindow, mSepiaKernel);
                break;

            // images[1] is assumed to be a grayscale image
            case VIEW_MODE_SOBEL:
                Mat grayscaleWindow = images[1];
                Imgproc.Sobel(grayscaleWindow, mIntermediateMat, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
                Imgproc.cvtColor(mIntermediateMat, rgbaWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                break;

            // images[0] and images[1] don't have to be rgba
            case VIEW_MODE_ZOOM:
                Mat mZoomWindow = images[0];
                Mat zoomCorner = images[1];
                Imgproc.resize(mZoomWindow, zoomCorner, zoomCorner.size());
                Size wsize = mZoomWindow.size();
                Core.rectangle(mZoomWindow, new Point(1, 1), new Point(wsize.width - 2, wsize.height - 2), new Scalar(255, 0, 0, 255), 2);
                break;

            case VIEW_MODE_PIXELIZE:
                Imgproc.resize(rgbaWindow, mIntermediateMat, new Size(), 0.1, 0.1, Imgproc.INTER_NEAREST);
                Imgproc.resize(mIntermediateMat, rgbaWindow, rgbaWindow.size(), 0., 0., Imgproc.INTER_NEAREST);
                break;

            case VIEW_MODE_POSTERIZE:
                Imgproc.Canny(rgbaWindow, mIntermediateMat, 80, 90);
                rgbaWindow.setTo(new Scalar(0, 0, 0, 255), mIntermediateMat);
                Core.convertScaleAbs(rgbaWindow, mIntermediateMat, 1. / 16, 0);
                Core.convertScaleAbs(mIntermediateMat, rgbaWindow, 16, 0);
                break;

            case VIEW_MODE_GRAY:
                Core.transform(rgbaWindow, rgbaWindow, mGrayKernel);
                break;

            case VIEW_MODE_INVERSE:
                Core.transform(rgbaWindow, rgbaWindow, mInverseKernel);
                break;

            case VIEW_MODE_WASH:
                Core.transform(rgbaWindow, rgbaWindow, mWashKernel);
                break;

            case VIEW_MODE_SAT:
                Core.transform(rgbaWindow, rgbaWindow, mSaturatedKernel);
                break;

            case VIEW_MODE_HUE:
                Core.transform(rgbaWindow, rgbaWindow, mHueKernel);
                break;

            /** COLORS */
            case VIEW_MODE_BLUE:
                Core.transform(rgbaWindow, rgbaWindow, mBlueKernel);
                break;

            case VIEW_MODE_RED:
                Core.transform(rgbaWindow, rgbaWindow, mRedKernel);
                break;

            case VIEW_MODE_PURPLE:
                Core.transform(rgbaWindow, rgbaWindow, mPurpleKernel);
                break;
        }
    }
}
