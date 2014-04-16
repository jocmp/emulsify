package edu.gvsu.cis.campbjos.emulsify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.concurrent.locks.AbstractOwnableSynchronizer;

/**
 * Created by Reuben on 3/19/14.
 */
public class FilterScrollElement extends LinearLayout {
    private int filterType;

    private TextView text;
    private ImageView image;

    Filterer filterer;

    public FilterScrollElement(Context context) {
        super(context);
    }


    public void initialize(int filterType, String string, Mat m) {
        this.filterType = filterType;
        setOrientation(LinearLayout.VERTICAL);

        //bitmap conversion taken from http://answers.opencv.org/question/16993/display-image/

        // convert to bitmap:
        Mat tm = m.clone();
        if (filterType != FilterApplier.VIEW_MODE_SOBEL && filterType != FilterApplier.VIEW_MODE_ZOOM) {
            FilterApplier.applyFilter(filterType, tm);
        }

        Bitmap bm = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tm, bm);

        // find the imageview and draw it!
        image = new ImageView(this.getContext(), null);

        image.setImageBitmap(bm);

        text = new TextView(this.getContext(), null);
        text.setTextColor(Color.parseColor("#FFFFFF"));
        text.setText(string);

        image.setClickable(false);
        text.setClickable(false);

        // add the elements to the layout
        addView(image);
        addView(text);
    }

    public int getFilterType() {
        return filterType;
    }

    public void setImage(Bitmap bitmap, int height) {

        if (filterer != null) filterer.cancel(true);
        filterer = new Filterer();
        filterer.execute(bitmap, height);
    }


    private class Filterer extends AsyncTask<Object, Object, Void> {

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            Bitmap bitmap = (Bitmap) values[0];
            int height = (Integer) values[1];

            int width = bitmap.getWidth();
            image.setImageBitmap(bitmap);
        }


        @Override
        protected Void doInBackground(Object... params) {
            Bitmap bitmap = (Bitmap) params[0];
            int height = (Integer) params[1];

            Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), bitmap.getDensity());// CvType.CV_8UC1);
            Utils.bitmapToMat(bitmap, mat);

            Imgproc.resize(mat, mat, new Size((int) (((float)height/bitmap.getHeight()) * (float)bitmap.getWidth()), height));

            if (filterType != FilterApplier.VIEW_MODE_SOBEL && filterType != FilterApplier.VIEW_MODE_ZOOM) {
                FilterApplier.applyFilter(filterType, mat);
            }

            Bitmap bm = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, bm);


            publishProgress(bm, height);

            return null;
        }


    }

}
