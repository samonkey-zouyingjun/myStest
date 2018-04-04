package com.evideo.sambaprovider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import java.io.FileDescriptor;
import java.io.IOException;

public class ImageActivity extends AppCompatActivity {

    private static final String TAG = "ImageActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        final ImageView iv = (ImageView) findViewById(R.id.imageview);
        final Uri uri = getIntent().getData();

        Log.d(TAG, "onCreate: uri: "+uri);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmapFromUri = getBitmapFromUri(uri);

                    if(bitmapFromUri == null){
                        Log.e(TAG, "getBitmapFromUri null!");
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(bitmapFromUri);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver()
                .openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor
                .getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }
}
