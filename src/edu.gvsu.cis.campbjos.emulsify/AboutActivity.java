package edu.gvsu.cis.campbjos.emulsify;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author  Emulsify Team
 * @version Winter 2014
 */
public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_item);

        /* ActionBar items */
        try {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Toast.makeText(this,
                    "Something went wrong. Try again.",
                    Toast.LENGTH_SHORT).show();
        }

        getActionBar().setTitle("About");

        TextView aboutTitle = (TextView) findViewById(R.id.aboutTitle);
        TextView versionNum = (TextView) findViewById(R.id.versionView);
        TextView aboutText = (TextView) findViewById(R.id.bodyText);
        aboutTitle.setText("emulsify");
        versionNum.setText("v1.0.3");
        aboutText.setText("Emulsify is a photo editing app created by the Emulsify Team:\n\n" +
                "Josiah Campbell\n\nReuben Wattenhofer\n\nSean Holloway\n\n\n" +
                "Emulsify includes features from:\n\n" +
                "OpenCV\nOpenCV for Android\nhttp://opencv.org\n\n" +
                "Google Play Map Services\nhttp://developers.android.com/google/play-services\n\n" +
                "Google Maps Android API utility library\nhttps://github.com/googlemaps/android-maps-utils\n\n" +
                "feColorMatrix\nFilter effect reference\nhttp://apike.ca/\n\n" +
                "Imgur API\nhttp://api.imgur.com\n\n"+
                "The Emulsify Team thanks the Grand Valley State University Computer Science " +
                "Department for releasing this app.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
