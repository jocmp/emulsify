package org.opencv.samples.imagemanipulations;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Reuben on 3/19/14.
 */
public class FilterApplier {

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_HIST      = 1;
    public static final int      VIEW_MODE_CANNY     = 2;
    public static final int      VIEW_MODE_SEPIA     = 3;
    public static final int      VIEW_MODE_SOBEL     = 4;
    public static final int      VIEW_MODE_ZOOM      = 5;
    public static final int      VIEW_MODE_PIXELIZE  = 6;
    public static final int      VIEW_MODE_POSTERIZE = 7;
    public static final int      VIEW_TEST_GRAYSCALE = 8;
    public static final int      VIEW_TEST_BLUE      = 9;

    private static Mat           mSepiaKernel;
    private static Mat           mBlueKernel;

    static {
        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
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

            case VIEW_TEST_GRAYSCALE:
                Mat grayscaleWindow = images[1];
                Imgproc.cvtColor(grayscaleWindow, rgbaWindow, Imgproc.COLOR_RGB2GRAY);
                break;

            case VIEW_MODE_CANNY:
                Imgproc.Canny(rgbaWindow, mIntermediateMat, 80, 90);
                Imgproc.cvtColor(mIntermediateMat, rgbaWindow, Imgproc.COLOR_GRAY2BGRA, 4);
                break;

            case VIEW_MODE_SEPIA:
                Core.transform(rgbaWindow, rgbaWindow, mSepiaKernel);
                break;

            case VIEW_TEST_BLUE:
                Core.transform(rgbaWindow, rgbaWindow, mSepiaKernel);
                break;

            // images[1] is assumed to be a grayscale image
            case VIEW_MODE_SOBEL:
                grayscaleWindow = images[1];
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
                Core.convertScaleAbs(rgbaWindow, mIntermediateMat, 1./16, 0);
                Core.convertScaleAbs(mIntermediateMat, rgbaWindow, 16, 0);
                break;
        }

    }


}
