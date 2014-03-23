package org.opencv.samples.imagemanipulations;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by Reuben on 3/23/14.
 */
public class PictureScrollElement extends ImageView{
    private String filename;

    public PictureScrollElement(Context context) {
        super(context);
    }

    public void initialize (String filepath, Mat image) {
        filename = filepath;

        Bitmap im = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(image, im);
        setImageBitmap(im);
    }

    public String getFile() { return filename; }
}
