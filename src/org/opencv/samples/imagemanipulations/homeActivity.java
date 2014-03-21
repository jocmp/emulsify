package org.opencv.samples.imagemanipulations;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by Josiah on 3/17/14.
 */
public class homeActivity extends Activity implements View.OnClickListener {

    Button camButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.home_item);

        //camButton = (Button) findViewById(R.id.camera);
        //camButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == camButton) {
            startActivity(new Intent(this, mainActivity.class));
        }
    }
}