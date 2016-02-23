package com.camerademo;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.camerademo.interfaces.OnFocusListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jaydipsinh Zala on 22/2/16.
 */
public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private OnFocusListener onFocusListener;

    private boolean needToTakePic = false;

    private Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0) {
                mCamera.cancelAutoFocus();
            }
        }
    };

    // Constructor that obtains context and camera
    @SuppressWarnings("deprecation")
    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mSurfaceHolder = this.getHolder();
        this.mSurfaceHolder.addCallback(this);
        this.mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.onFocusListener = (OnFocusListener) context;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        } catch (IOException e) {
            // left blank for now
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(surfaceHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            // intentionally left blank for a test
        }
    }

    /**
     * Called from PreviewSurfaceView to set touch focus.
     *
     * @param - Rect - new area for auto focus
     */
    public void doTouchFocus(final Rect tfocusRect) {
        try {
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters param = mCamera.getParameters();
            param.setFocusAreas(focusList);
            param.setMeteringAreas(focusList);
            mCamera.setParameters(param);

            mCamera.autoFocus(myAutoFocusCallback);

            if (isNeedToTakePic()) {
                onFocusListener.onFocused();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            Rect touchRect = new Rect(
                    (int) (x - 100),
                    (int) (y - 100),
                    (int) (x + 100),
                    (int) (y + 100));


            final Rect targetFocusRect = new Rect(
                    touchRect.left * 2000 / this.getWidth() - 1000,
                    touchRect.top * 2000 / this.getHeight() - 1000,
                    touchRect.right * 2000 / this.getWidth() - 1000,
                    touchRect.bottom * 2000 / this.getHeight() - 1000);

            doTouchFocus(targetFocusRect);
        }

        return false;
    }

    public boolean isNeedToTakePic() {
        return needToTakePic;
    }

    public void setNeedToTakePic(boolean needToTakePic) {
        this.needToTakePic = needToTakePic;
    }
}