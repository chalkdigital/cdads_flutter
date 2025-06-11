package com.chalkdigital.interstitial.ads;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.CloseableLayout;
import com.chalkdigital.common.CloseableLayout.OnCloseListener;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.response.TypeParser;

import java.util.HashMap;

import static com.chalkdigital.common.DataKeys.AD_SERVER_EXTRAS_KEY;
import static com.chalkdigital.common.DataKeys.BROADCAST_IDENTIFIER_KEY;

abstract class BaseInterstitialActivity extends Activity {
    @Nullable
    protected AdReport mAdReport;
    @Nullable
    private CloseableLayout mCloseableLayout;
    @Nullable
    private Long mBroadcastIdentifier;

    public abstract View getAdView(@NonNull final HashMap<String, String[]> events);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mBroadcastIdentifier = getBroadcastIdentifierFromIntent(intent);
        mAdReport = getAdReportFromIntent(intent);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View adView = getAdView(getAdEventsFromIntent(intent));

        mCloseableLayout = new CloseableLayout(this);
        mCloseableLayout.setOnCloseListener(new OnCloseListener() {
            @Override
            public void onClose() {
                finish();
            }
        });
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        HashMap<String, String> serverExtras = getServerExtrasFromIntent(intent);
        if (serverExtras.containsKey(DataKeys.STRETCH) && serverExtras.get(DataKeys.STRETCH).toString().equals(CDAdConstants.StretchType.STRETCH_TYPE_ASPECT_FILL.toString()) && this instanceof CDAdActivity){
            Utils.scaleLayoutParams(this, serverExtras.get(DataKeys.WIDTH), serverExtras.get(DataKeys.HEIGHT),layoutParams, serverExtras.get(DataKeys.STRETCH).toString(), TypeParser.parseInteger(serverExtras.get(DataKeys.CREATIVE_ORIENTATION_KEY), ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED));
        }
        mCloseableLayout.addView(adView,
                layoutParams);
        setContentView(mCloseableLayout);

//        // Lock the device orientation
//        Serializable orientationExtra = CreativeOrientation.fromString(serverExtras.get(DataKeys.CREATIVE_ORIENTATION_KEY));
//        CreativeOrientation requestedOrientation;
//        if (orientationExtra == null || !(orientationExtra instanceof CreativeOrientation)) {
//            requestedOrientation = CreativeOrientation.UNDEFINED;
//        } else {
//            requestedOrientation = (CreativeOrientation) orientationExtra;
//        }

//        if ( requestedOrientation == CreativeOrientation.UNDEFINED && serverExtras!=null && serverExtras.containsKey(DataKeys.STRETCH) && serverExtras.get(DataKeys.STRETCH).equals("2")){
//            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//            final int deviceOrientation = getResources().getConfiguration().orientation;
//            if (deviceOrientation == Configuration.ORIENTATION_LANDSCAPE)
//                requestedOrientation = CreativeOrientation.LANDSCAPE;
//            else requestedOrientation = CreativeOrientation.PORTRAIT;
//        }

//        DeviceUtils.lockOrientation(this, CreativeOrientation.fromString("d"));
        DeviceUtils.lockOrientation(this, serverExtras.get(DataKeys.CREATIVE_ORIENTATION_KEY));


    }

    @Override
    protected void onDestroy() {
        if (mCloseableLayout != null) {
            mCloseableLayout.removeAllViews();
        }
        super.onDestroy();
    }

    @Nullable
    protected CloseableLayout getCloseableLayout() {
        return mCloseableLayout;
    }

    @Nullable
    Long getBroadcastIdentifier() {
        return mBroadcastIdentifier;
    }

    protected void showInterstitialCloseButton() {
        if (mCloseableLayout != null) {
            mCloseableLayout.setCloseVisible(true);
        }
    }

    protected void hideInterstitialCloseButton() {
        if (mCloseableLayout != null) {
            mCloseableLayout.setCloseVisible(false);
        }
    }

    protected static Long getBroadcastIdentifierFromIntent(Intent intent) {
        if (intent.hasExtra(BROADCAST_IDENTIFIER_KEY)) {
            return intent.getLongExtra(BROADCAST_IDENTIFIER_KEY, -1L);
        }
        return null;
    }

    protected static HashMap<String, String > getServerExtrasFromIntent(Intent intent) {
        if (intent.hasExtra(AD_SERVER_EXTRAS_KEY)) {
            return (HashMap<String , String>)intent.getSerializableExtra(AD_SERVER_EXTRAS_KEY);
        }
        return null;
    }

    @Nullable
    protected static AdReport getAdReportFromIntent(Intent intent) {
        try {
            return (AdReport) intent.getSerializableExtra(DataKeys.AD_REPORT_KEY);
        } catch (ClassCastException e) {
                        Utils.logStackTrace(e);
            return null;
        }
    }

    @Nullable
    protected static HashMap<String, String[]> getAdEventsFromIntent(Intent intent) {
        try {
            return (HashMap<String, String[]>) intent.getSerializableExtra(DataKeys.AD_EVENTS_KEY);
        } catch (ClassCastException e) {
                        Utils.logStackTrace(e);
            return new HashMap<>();
        }
    }
}
