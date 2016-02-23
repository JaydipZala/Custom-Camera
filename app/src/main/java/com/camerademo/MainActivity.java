package com.camerademo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.camerademo.interfaces.OnFocusListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity
        implements OnFocusListener {
    private Button captureButton;
    private Camera mCamera;
    private CameraPreview mCameraPreview;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Obtain MotionEvent object
                v.setEnabled(false);
                mCameraPreview.setNeedToTakePic(true);
                long downTime = SystemClock.uptimeMillis();
                long eventTime = SystemClock.uptimeMillis() + 100;
                float x = mCameraPreview.getWidth() / 2;
                float y = mCameraPreview.getHeight() / 2;
                // List of meta states found here:     developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                int metaState = 0;
                MotionEvent motionEvent = MotionEvent.obtain(
                        downTime,
                        eventTime,
                        MotionEvent.ACTION_DOWN,
                        x,
                        y,
                        metaState
                );

                // Dispatch touch event to view
                mCameraPreview.dispatchTouchEvent(motionEvent);
            }
        });
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
//                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + "DEMO_" + ".jpg");
        if (mediaFile.exists()) mediaFile.delete();

        return mediaFile;
    }

    @Override
    public void onFocused() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.takePicture(null, null, mPicture);
                mCameraPreview.setNeedToTakePic(false);
                captureButton.setEnabled(true);
            }
        }, 1500);
    }
}