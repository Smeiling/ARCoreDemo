package com.sml.arcore.arcoredmo;

import android.Manifest;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.logging.Handler;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private Button mArButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mArButton = findViewById(R.id.btn_ar);
        mArButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session != null) {
                    //TODO: start ar activity
                    startActivity(new Intent(MainActivity.this, MyFirstArActivity.class));
                }
            }
        });
        maybeEnableArButton();
        new RxPermissions(this).request(Manifest.permission.CAMERA)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        checkARCore();
                    } else {

                    }
                });
    }

    /**
     * 检查设备是否支持ARCore
     */
    public void maybeEnableArButton() {
        // Likely called from Activity.onCreate() of an activity with AR buttons.
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        Log.d("ARCore", "maybeEnableArButton");
        if (availability.isTransient()) {
            // re-query at 5Hz while we check compatibility.
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    maybeEnableArButton();
                }
            }).start();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    maybeEnableArButton();
//                }
//            }, 200);
        }

        if (availability.isSupported()) {
            Log.d("ARCore", "isSupported");
            mArButton.setVisibility(View.VISIBLE);
            mArButton.setEnabled(true);
        } else { // unsupported or unknown
            Log.d("ARCore", "unsupported");
            mArButton.setVisibility(View.INVISIBLE);
            mArButton.setEnabled(false);
        }
    }

    private boolean mUserRequestedInstall = true;

    private Session session;

    /**
     * 检测ARCore是否已经安装
     */
    public void checkARCore() {
        if (session == null) {
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:
                        session = new Session(this);
                        break;
                    case INSTALL_REQUESTED:
                        mUserRequestedInstall = false;
                        break;
                    default:
                        break;
                }
            } catch (UnavailableDeviceNotCompatibleException e) {
                e.printStackTrace();
            } catch (UnavailableArcoreNotInstalledException e) {
                e.printStackTrace();
            } catch (UnavailableSdkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableApkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableUserDeclinedInstallationException e) {
                e.printStackTrace();
            }
        }
    }
}