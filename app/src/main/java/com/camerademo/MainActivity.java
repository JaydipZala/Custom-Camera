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
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity
        implements OnFocusListener {
    private Button captureButton, switchCameraButton;
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private int currentCameraId;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getIntent().hasExtra("camera_id")) {
            currentCameraId = getIntent().getIntExtra("camera_id", Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }

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

        switchCameraButton = (Button) findViewById(R.id.button_switch_camera);
        switchCameraButton.setVisibility(
                Camera.getNumberOfCameras() > 1 ? View.VISIBLE : View.GONE);
        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.stopPreview();
                //NB: if you don't release the current camera before switching, you app will crash
                mCameraPreview.getHolder().removeCallback(mCameraPreview);
                mCamera.release();

                //swap the id of the camera to be used
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }

                mCamera = getCameraInstance(currentCameraId);
                mCameraPreview = new CameraPreview(MainActivity.this, mCamera);
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.removeAllViews();
                preview.addView(mCameraPreview);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = getCameraInstance(currentCameraId);
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance(int currentCameraId) {
        Camera camera = null;
        try {
            camera = Camera.open(currentCameraId);
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
            } catch (IOException e) {
                e.printStackTrace();
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