package com.example.mike.iscope;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.ZoomControls;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.security.Policy;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Button Left, Right, Up, Down, NV, Lzo, Mzo;
    private boolean NVon, NVable;
    private Camera cam;
    Camera.Parameters par;
    SurfaceView sView;
    SurfaceHolder sHolder;
    ImageView xhair;
    ZoomControls zoomControls;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private int AimX, AimY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        sView = (SurfaceView) findViewById(R.id.surfaceView);
        sView.getHolder().addCallback(this);
        sView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


        Left = (Button) findViewById(R.id.LeftArr);
        Right = (Button) findViewById(R.id.RightArr);
        Up = (Button) findViewById(R.id.UpArr);
        Down = (Button) findViewById(R.id.DownArr);
        xhair = (ImageView) findViewById(R.id.CrossHair);
        NV = (Button) findViewById(R.id.LightEm);
        NVon = true;
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        AimX = size.x / 2;
        AimY = size.y / 2;
        RelativeLayout.LayoutParams kek = (RelativeLayout.LayoutParams)     xhair.getLayoutParams();
        kek.leftMargin = AimX;
        kek.topMargin = AimY;
        xhair.setLayoutParams(kek);

        NVable = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        if (!NVable) {
            AlertDialog achtung = new AlertDialog.Builder(MainActivity.this).create();
            achtung.setTitle("ERROR");
            achtung.setMessage("You cannot use NV mode because your cam does NOT have Flash! Sorry :(");
            achtung.show();
            return;
        }

        getCamera();

        NV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NVon) {
                    turnOffFlash();
                    NV.setText("NV OFF");
                } else {
                    turnOnFlash();
                    NV.setText("NV ON");
                }
            }
        });

        zoomControls = (ZoomControls) findViewById(R.id.zoomControls);
        final int[] currentZoomLevel = {par.getZoom()};
        if (par.isZoomSupported()) {
            final int maxZoomLevel = par.getMaxZoom();
            Log.i("max ZOOM ", "is " + maxZoomLevel);
            zoomControls.setIsZoomInEnabled(true);
            zoomControls.setIsZoomOutEnabled(true);

            zoomControls.setOnZoomInClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(currentZoomLevel[0] < maxZoomLevel){
                        currentZoomLevel[0]++;
                        //mCamera.startSmoothZoom(currentZoomLevel);
                        par.setZoom(currentZoomLevel[0]);
                        cam.setParameters(par);
                    }
                }
            });

            zoomControls.setOnZoomOutClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    if(currentZoomLevel[0] > 0){
                        currentZoomLevel[0]--;
                        par.setZoom(currentZoomLevel[0]);
                        cam.setParameters(par);
                    }
                }
            });
        }
        else
            zoomControls.setVisibility(View.GONE);


        Left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams mPar = (RelativeLayout.LayoutParams)
                        xhair.getLayoutParams();
                AimX -= 2;
                mPar.leftMargin = AimX;
                xhair.setLayoutParams(mPar);
            }
        });

        Right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams mPar = (RelativeLayout.LayoutParams)
                        xhair.getLayoutParams();
                AimX += 2;
                mPar.leftMargin = AimX;
                xhair.setLayoutParams(mPar);
            }
        });

        Up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams mPar = (RelativeLayout.LayoutParams)
                        xhair.getLayoutParams();
                AimY -= 2;
                mPar.topMargin = AimY;
                xhair.setLayoutParams(mPar);
            }
        });

        Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout.LayoutParams mPar = (RelativeLayout.LayoutParams)
                        xhair.getLayoutParams();
                AimY += 2;
                mPar.topMargin = AimY;
                xhair.setLayoutParams(mPar);
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    private void getCamera() {
        if (cam == null) {
            try {
                cam = Camera.open();
                par = cam.getParameters();
            } catch (Exception e) {

            }
        }
    }

    private void turnOnFlash() {

        if (!NVon) {
            if (cam == null || par == null) {
                return;
            }

            par = cam.getParameters();
            par.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(par);
            cam.startPreview();
            NVon = true;
        }

    }

    private void turnOffFlash() {

        if (NVon) {
            if (cam == null || par == null) {
                return;
            }

            par = cam.getParameters();
            par.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(par);
            //cam.stopPreview();
            NVon = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cam.stopPreview();
        turnOffFlash();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        cam.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCamera();
        cam.startPreview();
        // on resume turn on the flash
        if (NVable) turnOnFlash();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();

        // on starting the app get the camera params
        getCamera();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.mike.iscope/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.mike.iscope/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);

        // on stop release the camera
        if (cam != null) {
            cam.release();
            cam = null;
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters params = cam.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        params.setPreviewSize(selected.width, selected.height);
        cam.setParameters(params);

        cam.startPreview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            cam.setPreviewDisplay(sView.getHolder());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("PREVIEW", "surfaceDestroyed");
    }

}
