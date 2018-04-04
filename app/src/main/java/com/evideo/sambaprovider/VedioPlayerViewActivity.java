package com.evideo.sambaprovider;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

public class VedioPlayerViewActivity extends AppCompatActivity {

    private FileDescriptor mFileDescriptor;
    private static final String TAG = "VedioPlayerViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vedio_player_view);

        ImageView viewById = (ImageView) findViewById(R.id.ivff);

        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver()
                    .openFileDescriptor(getIntent().getData(), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mFileDescriptor = parcelFileDescriptor.getFileDescriptor();
        if(mFileDescriptor == null){
            Log.e(TAG, "mFileDescriptor == null" );
        }

    }
}
