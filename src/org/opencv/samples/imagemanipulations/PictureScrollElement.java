package org.opencv.samples.imagemanipulations;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

/**
 * Created by Reuben on 3/23/14.
 */
public class PictureScrollElement extends ImageView{
    private String filename;
    private boolean isBoxed = false;
    private Mat image;

    //private Mat box;
    private int width, height, density;

    public PictureScrollElement(Context context) {
        super(context);
    }

    public void initialize (String filepath, Mat image) {
        filename = filepath;
        this.image = image;
        Bitmap im = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, im);
        setImageBitmap(im);

        width = image.cols();
        height = image.rows();
    }

    public void box () {
        Mat boxedImage = image.clone();
        Core.rectangle(boxedImage, new Point(0,0), new Point(boxedImage.cols(), boxedImage.rows()), new Scalar(0,0,0), 5);
        Bitmap im = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(boxedImage, im);
        setImageBitmap(im);

        isBoxed = true;
    }

    public void unBox () {
        Bitmap im = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, im);
        setImageBitmap(im);

        isBoxed = false;
    }


    public boolean isBoxed() { return isBoxed; }

    public String getFile() { return filename; }

    public int getWdth() {
        return width;
    }

    public int getHght() {
        return height;
    }

    public void setDensity(int density) {
        this.density = density;
    }

    public int getDensity() {return density;}
}
