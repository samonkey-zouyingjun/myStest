package com.evideo.sambaprovider;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;

import java.io.FileDescriptor;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoActivity extends AppCompatActivity {


    public static Context context;
    public final static String URL_TEST_LOCAL_VIDEO_PATH = "/storage/emulated/0/test/samba/test.mp4";
    public final static String URL_TEST_LOCAL_3GP_PATH = "/storage/emulated/0/baile.3gp";
    public final static String URL_TEST_REMOTE_3gp = "http://f.pepst.com/c/d/EF25EB/480964-8231/ssc3/home/005/tikowap.wap/albums/baile.3gp";
    public final static String URL_TEST_REMOTE_MP4 = "http://vjs.zencdn.net/v/oceans.mp4";
    //    public final static String TAG = "VideoActivity";
    public final static String TAG = "VideoActivity";
    public final static String ACTION_KEY_URL = "URL";
    private final String MP4_PATH = "/mnt/nfs/k20s/4k.mp4";
    private IjkMediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private int position;
    private SeekBar mSeekBar;
    private Uri mData;
    private FileDescriptor mFileDescriptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_video);

        mData = getIntent().getData();

        Log.d(TAG, "onCreate: uri: "+ mData);


        initSurface();
        mSeekBar = (SeekBar) findViewById(R.id.seekbar);
        mSeekBar.setMax(100);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int duration = (int) mediaPlayer.getDuration();

                float p = (float) progress/100f;

                mediaPlayer.seekTo((int) (duration*p));

            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
//        pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        stop();
    }

    private void initSurface() {

        mediaPlayer = new IjkMediaPlayer();
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                log("surfaceDestroyed");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                log("surfaceCreated");
                setData();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                log("surfaceChanged");
            }
        });
        mediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.e(TAG, "onError: i"+i );
                return false;
            }
        });
    }

    private void setData() {
        /*ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver()
                    .openFileDescriptor(mData, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mFileDescriptor = parcelFileDescriptor.getFileDescriptor();
        if(mFileDescriptor == null){
            Log.e(TAG, "mFileDescriptor == null" );
        }else*/
        prepare();
    }

    private void prepare() {
        try {
            //reset
            mediaPlayer.reset();
            log("reset");
            //audio
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            log("setAudioStreamType");

//            mediaPlayer.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            log("setOutputFormat");
            //DataSource
            mediaPlayer.setDataSource(MP4_PATH);

            log("setDataSource");

            //attach surfaceView
            mediaPlayer.setDisplay(surfaceView.getHolder());
            log("setDisplay");

            //prepare
            mediaPlayer.prepareAsync();
//            mediaPlayer.prepare();
            log("prepare");

            //start
            mediaPlayer.start();
            log("start");
        } catch (Exception e) {
            e.printStackTrace();
            log("prepare    Exception:" + e.getMessage() + " " + e.getClass().getName());
        }
    }

    /*private void stop() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void pause() {
        if (mediaPlayer == null) {
            return;
        }
        if (mediaPlayer.isPlaying()) {
            position = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
        }
    }

    private void resume() {
        if (mediaPlayer == null || mediaPlayer.isPlaying()) {
            return;
        }
//        mediaPlayer.seekTo(position);
//        mediaPlayer.s
    }*/

    private void log(String msg) {
        Log.d(TAG, "VideoActivity   " + msg);
    }

    public void onBackPress(View v){
        onBackPressed();
    }
}
