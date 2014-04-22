package edu.gvsu.cis.campbjos.emulsify;

import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Reuben on 4/14/14.
 */
public class MapImageViewer extends DialogFragment implements View.OnClickListener {
    HorizontalScrollView scroll;
    LinearLayout grid;
    View view;

    Map<ImageView, String> filepaths = new HashMap<ImageView, String>();

    Loader loader;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.map_fragment_image_selection_view, container);

        grid = (LinearLayout) view.findViewById(R.id.linearScroll);
        scroll = (HorizontalScrollView) view.findViewById(R.id.horizontalScrollView);

        Bundle args = getArguments();
        if (args != null) {
            loader = new Loader();
            loader.execute(args);
        }


        return view;
    }

    private class Loader extends AsyncTask<Bundle, Object, Void> {
        @Override
        protected void onPreExecute() {
            getActivity().setProgressBarIndeterminateVisibility(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            getActivity().setProgressBarIndeterminateVisibility(false);

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Bundle... params) {
            Bundle args = params[0];
            ArrayList<String> files = args.getStringArrayList("files");
            ArrayList<Bitmap> images = args.getParcelableArrayList("images");

            for (int i = 0; i < images.size(); i++ ) {

                if (i < files.size()) {
                    String fPath = files.get(i);
                    Bitmap bmp2 = images.get(i);

                    if (bmp2 != null) {
                        publishProgress(bmp2, fPath);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
            Bitmap bmp2 = (Bitmap) values[0];

            ImageView image = new ImageView(getActivity());
            image.setImageBitmap(bmp2);
            image.setOnClickListener(MapImageViewer.this);
            image.setPadding(10, 10, 10, 10);
            grid.addView(image);
            filepaths.put(image, (String) values[1]);

        }
    }


    @Override
    public void onClick(View v) {
        synchronized (grid) {
            for (int i = 0; i < grid.getChildCount(); i++) {
                if (v == grid.getChildAt(i)) {
                    MapActivity callingActivity = (MapActivity) getActivity();
                    if (loader != null) loader.cancel(true);
                    dismiss();//seems to work...
                    callingActivity.onUserSelectValue(filepaths.get(v));
                    break;

                }
            }
        }
    }

}

