package edu.gvsu.cis.emulsify;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import org.opencv.samples.imagemanipulations.R;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.about_this:
                Intent toAbout = new Intent(this, aboutActivity.class);
                startActivity(toAbout);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    Bitmap selectedPhoto = BitmapFactory.decodeFile(filePath);
                    /** Fire up editActivity */
                    Intent editIntent = new Intent(this, editActivity.class);

                    editIntent.putExtra("filename", selectedPhoto);
                    startActivity(editIntent);
                }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == camButton) {
            startActivity(new Intent(this, mainActivity.class));
            // added by Attenr 3/26/14
            finish();
        }
        if (v == libButton) {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            final int ACTIVITY_SELECT_IMAGE = 10;
            startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE);
        }
    }
}