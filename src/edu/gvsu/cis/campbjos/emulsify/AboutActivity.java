package edu.gvsu.cis.campbjos.emulsify;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import edu.gvsu.cis.emulsify.R;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_item);

        /* ActionBar items */
        try {
            getActionBar().setHomeButtonEnabled(true);
        } catch (NullPointerException e) {
            Log.e("getActionBar null", "NullPointerException in About");
            Toast.makeText(this,
                    "Something went wrong. Try again.",
                    Toast.LENGTH_SHORT).show();
        }

        getActionBar().setTitle("About");

        TextView aboutTitle = (TextView) findViewById(R.id.aboutTitle);
        TextView versionNum = (TextView) findViewById(R.id.versionView);
        TextView aboutText = (TextView) findViewById(R.id.bodyText);
        aboutTitle.setText("emulsify");
        versionNum.setText("v0.1");
        aboutText.setText("Emulsify is a photo editing activity created by the Emulsify Team:\n" +
                "Josiah Campbell\nReuben Wattenhofer\nSean Holloway\n\n" +
                "Emulsify includes features from:\n\n" +
                "OpenCV\nOpenCV for Android\nhttp://opencv.org\n\n" +
                "Google Play Map Services\nhttp://developers.android.com/google/play-services\n\n" +
                "feColorMatrix\nFilter effect reference\nhttp://apike.ca/\n\n" +
                "The Emulsify Team thanks the Grand Valley State University Computer Science " +
                "Department for deploying this app.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
