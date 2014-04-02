package edu.gvsu.cis.emulsify;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Josiah on 3/17/14.
 */
public class homeActivity extends Activity implements View.OnClickListener {

    private Button camButton, libButton, mapButton;
    private final int TAKE_PHOTO_REQUEST= 1;
    private final int ACTIVITY_SELECT_IMAGE = 10;
    private String currentPhotoString;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_item);

        camButton = (Button) findViewById(R.id.camera);
        libButton = (Button) findViewById(R.id.galleryIn);
        mapButton = (Button) findViewById(R.id.mapButton);

        camButton.setOnClickListener(this);
        libButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);

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
            case TAKE_PHOTO_REQUEST:
                if (!currentPhotoString.isEmpty()) {
                    Intent editIntent = new Intent(this, editActivity.class);

                    File imgDir =
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    editIntent.putExtra("filename", imgDir.getAbsolutePath() + currentPhotoString);

                    startActivity(editIntent);
                }
                else {
                    Toast.makeText(this, "Photo Error. Try again", Toast.LENGTH_SHORT).show();
                } break;
            case ACTIVITY_SELECT_IMAGE:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();

                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    /* Fire up editActivity */
                    Intent editIntent = new Intent(this, editActivity.class);

                    editIntent.putExtra("filename", filePath);
                    startActivity(editIntent);
                } break;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == camButton) {
            /* Clear Photo Name */
            currentPhotoString = null;
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (captureIntent.resolveActivity(getPackageManager()) != null) {
                /* Format Image Name */
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                String currentTime = sdf.format(new Date());
                currentPhotoString = "/sample_picture_" + currentTime + ".jpg";
                /** Saving the Image */
                File imageDir =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(imageDir, currentPhotoString);
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
                /* Start Activity Result */
                startActivityForResult(captureIntent, TAKE_PHOTO_REQUEST);
            } else {
                Toast.makeText(this,
                               "Something bad happened, sorry mate.",
                               Toast.LENGTH_SHORT).show();
                }
            } else if (v == libButton) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, ACTIVITY_SELECT_IMAGE);
            }else if (v == mapButton) {
                Intent toMap = new Intent(this, mapActivity.class);
                startActivity(toMap);
            }
    }
}