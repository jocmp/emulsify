package org.opencv.samples.imagemanipulations;

import org.opencv.core.Mat;

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

    public static void applyFilter(int mode, Mat image) {
        Mat mIntermediateMat;

        switch (mode) {
            case VIEW_MODE_RGBA:
                break;

            case VIEW_MODE_HIST:
                break;

            case VIEW_MODE_CANNY:
                break;

            case VIEW_MODE_SEPIA:
                break;

            case VIEW_MODE_SOBEL:
                break;

            case VIEW_MODE_ZOOM:
                break;

            case VIEW_MODE_PIXELIZE:
                break;

            case VIEW_MODE_POSTERIZE:
                break;
        }
    }
}
