package com.chalkdigital.common;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import com.chalkdigital.R;

import java.util.ArrayList;

/**
 * Created by arungupta on 15/09/17.
 */

public class CDAdPermissionActivity extends AppCompatActivity implements CDAdPermissionViewListener {

    public final static int LOCATION_ACCESS_PERMISSION = 21;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        requestPermissions();
    }

    public void requestPermissions(){
        ArrayList<String> requiredPermissions = new ArrayList<String>();
        requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (requiredPermissions.size()>0){
            String[] reqPermissions = new String[requiredPermissions.size()];
            ActivityCompat.requestPermissions(this,
                    requiredPermissions.toArray(reqPermissions),
                    LOCATION_ACCESS_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case LOCATION_ACCESS_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.LocationPermissionGranted), Toast.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(CDAdActions.LOCATION_PERMISSION_GRANTED));
                } else {
                    //Toast.makeText(getApplicationContext(), getResources().getString(R.string.LocationPermissionDenied), Toast.LENGTH_SHORT).show();
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(CDAdActions.LOCATION_PERMISSION_DENIED));
                }
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                finish();
                return;

            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionGranted() {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(CDAdActions.TRACKING_PERMISSION_GRANTED));
        finish();
    }

    @Override
    public void onPermissionRejected() {
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(CDAdActions.TRACKING_PERMISSION_DENIED));
        finish();
    }
}
