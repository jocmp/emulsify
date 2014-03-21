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

    Button camButton, libButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_item);

        camButton = (Button) findViewById(R.id.camera);
        libButton = (Button) findViewById(R.id.galleryIn);
        camButton.setOnClickListener(this);
        libButton.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                Toast.makeText(this, "onActivityResult"
                        + "\n\t\t\t\tcase 10", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == camButton) {
            startActivity(new Intent(this, mainActivity.class));
        }
        if (v == libButton) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 10;
            startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE);
        }
    }
}