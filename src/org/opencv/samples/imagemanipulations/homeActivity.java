package org.opencv.samples.imagemanipulations;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by Josiah on 3/17/14.
 */
public class homeActivity extends Activity implements View.OnClickListener {

    Button camButton, galButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_item);

        camButton = (Button) findViewById(R.id.camera);
        galButton = (Button) findViewById(R.id.galleryIn);
        camButton.setOnClickListener(this);
        galButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == camButton) {
            startActivity(new Intent(this, mainActivity.class));
        if (v == galButton) {
            Toast.makeText(this, "It won't now, but it will soon!", Toast.LENGTH_LONG).show();
            }
        }
    }
}