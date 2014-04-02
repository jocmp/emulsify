package edu.gvsu.cis.emulsify;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class aboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_item);

        /* ActionBar items */
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("About");

        TextView aboutTitle = (TextView) findViewById(R.id.aboutTitle);
        TextView versionNum = (TextView) findViewById(R.id.versionView);
        TextView aboutText = (TextView) findViewById(R.id.bodyText);
        aboutTitle.setText("emulsify");
        versionNum.setText("v0.1");
        aboutText.setText("Hey! You found the about page!!!1");
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
