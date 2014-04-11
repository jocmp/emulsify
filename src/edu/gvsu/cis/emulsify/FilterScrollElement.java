package edu.gvsu.cis.emulsify;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * Created by Reuben on 3/19/14.
 */
public class FilterScrollElement extends LinearLayout {
    private int filterType;

    private TextView text;
    private ImageView image;


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
        //fix the dimensions of the image
        //image.setMaxWidth(R.dimen.filterImageWidth);
        //image.setMaxHeight(R.dimen.filterImageHeight);
        //for some reason, both max AND min dimensions can't be set (that is, I would prefer to have a fixed size)
        //image.setMinimumWidth(R.dimen.filterImageWidth);
        //image.setMinimumHeight(R.dimen.filterImageHeight);

        //image.setImageResource(R.drawable.ic_launcher);
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
}
