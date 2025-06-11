package com.chalkdigital.nativeads;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.ExternalViewabilitySession;
import com.chalkdigital.common.ExternalViewabilitySessionManager;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.event.EventDetails;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Json;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.nativeads.NativeVideoController.NativeVideoProgressRunnable;
import com.chalkdigital.network.TrackingRequest;
import com.chalkdigital.spark.PlacementType;
import com.chalkdigital.spark.SparkBridge;
import com.chalkdigital.spark.SparkVideoViewController;
import com.chalkdigital.spark.SparkWebViewDebugListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.chalkdigital.common.DataKeys.CLICK_ACTION;
import static com.chalkdigital.common.DataKeys.IMPRESSION_MIN_VISIBLE_PERCENT;
import static com.chalkdigital.common.DataKeys.IMPRESSION_MIN_VISIBLE_PX;
import static com.chalkdigital.common.DataKeys.IMPRESSION_VISIBLE_MS;
import static com.chalkdigital.common.DataKeys.MAX_BUFFER_MS;
import static com.chalkdigital.common.DataKeys.PAUSE_VISIBLE_PERCENT;
import static com.chalkdigital.common.DataKeys.PLAY_VISIBLE_PERCENT;
import static com.chalkdigital.common.DataKeys.VIDEO_TRACKERS_KEY;
import static com.chalkdigital.nativeads.NativeVideoController.VisibilityTrackingEvent;

public class CDAdCustomEventVideoNative extends CustomEventNative {


    @Override
    protected void loadNativeAd(@NonNull final Context context,
            @NonNull final CustomEventNativeListener customEventNativeListener,
            @NonNull final Map<String, Object> localExtras,
            @NonNull final Map<String, String> serverExtras) {


        if (serverExtras==null || serverExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY)==null) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.INVALID_RESPONSE);
            return;
        }

        final CDAdVideoNativeAd videoNativeAd = new CDAdVideoNativeAd(context,
                customEventNativeListener, serverExtras);
        try {
            videoNativeAd.loadAd();
        } catch (IllegalArgumentException e) {
                        Utils.logStackTrace(e);
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
        }
    }

    public static class CDAdVideoNativeAd extends VideoNativeAd
            implements NativeVideoProgressRunnable.ProgressListener,
            AudioManager.OnAudioFocusChangeListener {

        enum Parameter {
            IMPRESSION_TRACKER("imptracker", true),
            CLICK_TRACKER("clktracker", true),
            TITLE("title", false),
            TEXT("text", false),
            IMAGE_URL("mainimage", false),
            ICON_URL("iconimage", false),
            CLICK_DESTINATION("clk", false),
            FALLBACK("fallback", false),
            CALL_TO_ACTION("ctatext", false),
            VAST_VIDEO("video", false);

            @NonNull final String mName;
            final boolean mRequired;

            Parameter(@NonNull final String name, final boolean required) {
                Preconditions.checkNotNull(name);
                mName = name;
                mRequired = required;
            }

            @Nullable
            static Parameter from(@NonNull final String name) {
                Preconditions.checkNotNull(name);
                for (final Parameter parameter : values()) {
                    if (parameter.mName.equals(name)) {
                        return parameter;
                    }
                }

                return null;
            }

            @NonNull
            @VisibleForTesting
            static final Set<String> requiredKeys = new HashSet<String>();
            static {
                for (final Parameter parameter : values()) {
                    if (parameter.mRequired) {
                        requiredKeys.add(parameter.mName);
                    }
                }
            }
        }

        public enum VideoState {
            CREATED, LOADING, BUFFERING, PAUSED, PLAYING, PLAYING_MUTED, ENDED, FAILED_LOAD
        }

        static final String PRIVACY_INFORMATION_CLICKTHROUGH_URL = "https://www.chalkdigital.com/optout/";

        @NonNull private final Context mContext;
        @NonNull private VideoState mVideoState;
        @NonNull private final VisibilityTracker mVideoVisibleTracking;
        @NonNull private final CustomEventNativeListener mCustomEventNativeListener;
        @NonNull private final NativeVideoControllerFactory mNativeVideoControllerFactory;
        @Nullable private NativeVideoController mNativeVideoController;

        @Nullable private MediaLayout mMediaLayout;
        @Nullable private ViewGroup mRootView;

        @Nullable private JSONObject mVideoTrackers;
        @Nullable private Map<String, String> mExternalViewabilityTrackers;

        private final long mId;
        private boolean mNeedsSeek;
        private boolean mNeedsPrepare;
        private boolean mPauseCanBeTracked = false;
        private boolean mResumeCanBeTracked = false;

        // These variables influence video state.
        private int mLatestVideoControllerState;
        private boolean mError;
        private boolean mLatestVisibility;
        private boolean mMuted;
        private boolean mEnded;
        private String mVastResponse;
        private String mVpaidSourceUrl;
        private String mClickAction;

        @Nullable
        private SparkVideoViewController mSparkVideoViewController;
        @Nullable
        private SparkWebViewDebugListener mDebugListener;
        @Nullable
        private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;

        private Map<String, String > mServerExtras;


        protected void extractExtras(Map<String, String> serverExtras) {
            mServerExtras = serverExtras;
            mVpaidSourceUrl = serverExtras.get(DataKeys.VPAID_SOURCE_URL);
            mVastResponse = serverExtras.get(DataKeys.HTML_RESPONSE_BODY_KEY);
            mClickAction = serverExtras.get(CLICK_ACTION);
//            mVastResponse = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><VAST version=\"2.0\">  <Ad id=\"1222\">  <Wrapper>    <AdSystem>Chalk Digital</AdSystem>    <VASTAdTagURI><![CDATA[http://bs.serving-sys.com/Serving?cn=display&c=23&pl=VAST&pli=24850877&PluID=0&pos=2406&ord=1530734706741&cim=1]]></VASTAdTagURI>    <Creatives><Creative AdID=\"5370\"><Linear><TrackingEvents><Tracking event=\"start\"><![CDATA[http://api.cdange.mobi/xp/evt?srid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&osrid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&t=1530734706&pubid=0cf8d6d643e13d86a5b6374148a4afac&impid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&cip=223.225.56.64&cid=1222&crid=5370&b=chalkdigital.com&c=IND&wh=1024x768&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_13_5%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F67.0.3396.99+Safari%2F537.36&carrier=&ct=New+Delhi&st=DL&z=&bek=8c830e1706e9d12a5771daddb3678023_20180704_20&le=0&userid=2c3e7bb4f7cceb6dc5bb66d2b24cf43a4797911f&bidid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&ifa=&m=1&act=6]]></Tracking><Tracking event=\"start\"><![CDATA[http://google.com]]></Tracking><Tracking event=\"start\"><![CDATA[http://api.cdange.mobi/xp/evt?pp=UijKZvPbC7Z84Jbgaqfkm2Xp38Ywp7R02N7v41FvceknAMzf55UDPG6pqAJwsZs7FYIJSFwm13P2eIBnLxHYyGPcYaRxglngqzZXlyQyrRKrjvtNcrp4yUsxgUZIk6q11yivEIoWlHPvqxwgyHa6MlzjnbGsIUsRv4z8o671oGa0EGngZHVtr1GkQZ3ya0kjVM3qXTupLgsLqIaolYgdTlspqPZalvSCdON8NOjxTOimtOo23ZaTFUzUtffqHbhuDN9cmDaZCRNyLgQ1k0HFt558viV8FSXjz93Vm6C0L95XoTXQrxSoMCVd49LKMDZ8rniDaYH8vGSJbCEujsjm3d6Xorc49FxL9bDhK2zPAXXzwqNesQkBQfuCDyiY0ZKTm7K3irSCKX0nMfOmGuODCvLmjLxr1iaOOJRfNoNkBYssnpCQFkjHZYX41fQwt7ZmHkNxBjHNazPxH9zwKqW0ne9IWJHxawiSqyUgdOn3COAvAcSl6PvSlcQqmtnm70BDlQxjjFBn1Son3Y0PyMfZXqG1oBmXJSgrQBjKOPw4wAc3n1a2KY6WGyKFh1EYadVqJl80JX3dXIcFPHYJA3vRltfpkAszOBp5mhyG9mk2NyGZo0lxtuBM6isHMJNLXXGWpu4246myDIC5LDzwmXjgrPfXXhVe7j0u36oxfYFsbVhEBJCJaYsolHALoohPlA14sWhDliryqjb0mPhDHEdKtZ2rg6XtqtI1LGtrvMF385UZy87DoVBgdvpXq4UXF9yWKql1VANdcM4zvoYR1sS6p0DtEUCavLpJEFU01C23y7vO5eOJfwkd0EKILWb4N5Ky5bAugHmgaY2k6NPBFiegn9IlPHYACfUl0Zo8LAxInDk7PPCvkAkWnHx5unn0dCnLm26MFi3IKL2eE8QRTUPwkIJZB9qsXbjHFHSTQA6fUXw__]]></Tracking><Tracking event=\"firstQuartile\"><![CDATA[http://api.cdange.mobi/xp/evt?srid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&osrid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&t=1530734706&pubid=0cf8d6d643e13d86a5b6374148a4afac&impid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&cip=223.225.56.64&cid=1222&crid=5370&b=chalkdigital.com&c=IND&wh=1024x768&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_13_5%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F67.0.3396.99+Safari%2F537.36&carrier=&ct=New+Delhi&st=DL&z=&bek=8c830e1706e9d12a5771daddb3678023_20180704_20&le=0&userid=2c3e7bb4f7cceb6dc5bb66d2b24cf43a4797911f&bidid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&ifa=&m=1&act=7]]></Tracking><Tracking event=\"firstQuartile\"><![CDATA[http://google.com]]></Tracking><Tracking event=\"midpoint\"><![CDATA[http://api.cdange.mobi/xp/evt?srid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&osrid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&t=1530734706&pubid=0cf8d6d643e13d86a5b6374148a4afac&impid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&cip=223.225.56.64&cid=1222&crid=5370&b=chalkdigital.com&c=IND&wh=1024x768&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_13_5%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F67.0.3396.99+Safari%2F537.36&carrier=&ct=New+Delhi&st=DL&z=&bek=8c830e1706e9d12a5771daddb3678023_20180704_20&le=0&userid=2c3e7bb4f7cceb6dc5bb66d2b24cf43a4797911f&bidid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&ifa=&m=1&act=8]]></Tracking><Tracking event=\"midpoint\"><![CDATA[http://google.com]]></Tracking><Tracking event=\"thirdQuartile\"><![CDATA[http://api.cdange.mobi/xp/evt?srid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&osrid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&t=1530734706&pubid=0cf8d6d643e13d86a5b6374148a4afac&impid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&cip=223.225.56.64&cid=1222&crid=5370&b=chalkdigital.com&c=IND&wh=1024x768&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_13_5%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F67.0.3396.99+Safari%2F537.36&carrier=&ct=New+Delhi&st=DL&z=&bek=8c830e1706e9d12a5771daddb3678023_20180704_20&le=0&userid=2c3e7bb4f7cceb6dc5bb66d2b24cf43a4797911f&bidid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&ifa=&m=1&act=9]]></Tracking><Tracking event=\"thirdQuartile\"><![CDATA[http://google.com]]></Tracking><Tracking event=\"complete\"><![CDATA[http://api.cdange.mobi/xp/evt?srid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&osrid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&t=1530734706&pubid=0cf8d6d643e13d86a5b6374148a4afac&impid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&cip=223.225.56.64&cid=1222&crid=5370&b=chalkdigital.com&c=IND&wh=1024x768&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_13_5%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F67.0.3396.99+Safari%2F537.36&carrier=&ct=New+Delhi&st=DL&z=&bek=8c830e1706e9d12a5771daddb3678023_20180704_20&le=0&userid=2c3e7bb4f7cceb6dc5bb66d2b24cf43a4797911f&bidid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&ifa=&m=1&act=10]]></Tracking><Tracking event=\"complete\"><![CDATA[http://google.com]]></Tracking><Tracking event=\"close\"><![CDATA[http://api.cdange.mobi/xp/evt?srid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&osrid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&t=1530734706&pubid=0cf8d6d643e13d86a5b6374148a4afac&impid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&cip=223.225.56.64&cid=1222&crid=5370&b=chalkdigital.com&c=IND&wh=1024x768&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_13_5%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F67.0.3396.99+Safari%2F537.36&carrier=&ct=New+Delhi&st=DL&z=&bek=8c830e1706e9d12a5771daddb3678023_20180704_20&le=0&userid=2c3e7bb4f7cceb6dc5bb66d2b24cf43a4797911f&bidid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&ifa=&m=1&act=11]]></Tracking></TrackingEvents><VideoClicks><ClickTracking><![CDATA[http://api.cdange.mobi/xp/evt?pp=5BuvkUW3r39lUHHkEtTJ1dVLvKKvxPhtRXfQuE4rAFU6WQzJMApgxtdhSBsOLMz5jjLOC0q252YEbTmwUeF8XIX6QlpnYqPIiZD4zthUfhjQNKe5QNNvHvPDig7jVFlPl2Wr290sNOLCloANv6GFxcYIYRCVFDAdMH7QTij8vsfUv5OIpZDibhIzdSixROVKTSq4FfmKb9RhvE7U5VsBsQmZDLgnD5AhbUs92a1hpWAAJv1ec9qHynKLsyrlJJ5RDU5VVicd8uvDTfI0dLFSSxlyLaGyP1NyKFdw6CpYAxBlg6cOjGfhNSJqCpJK55mPJjKilterNOAPvAwDzE4mTj7X9kmBiyBdxxS34G1u2ryphE8s8fNHjHBKzB8GfuRJxqk3zpVMdxSdrwKCEBYgjTKod4VpJmrXnAoikI814SaugWBeq8Iue35cQJk4VXakxcd5W8H5GJJdqN3tLvkidNBYyrJ5bMsjF7LHEQ5rRVx2zZImUhEAhapYMdsXbUIjpiJLSeoP5R0SKqLTE4rl8FrtysOpOahDnFB16tbLzld47MBZLXNE4FpuHN0OVIOQZkXOtgscVOgM0HNU3tUGgzbTg1dv5QozV9RjvagIl3Zl3LuoXmqDm6Fn6gDsQRl4kOdzbjl08eZe3hAz3JkpeN5iZ2qnQqZyLE5Hv3LN52eGDFZrnINZxz3vUIzNMqhm6TcoHv6QljKS9ZN6IiotPJE2fNQhcEzvbN8TJXWiX2IvZBYLfDCZCAB22bxqf5DmFKlOGycJtjms1XkXHttEV8LKrU4nm76BZWI4JYVfbkcO5LEcgkTTLgIAlIncJKavTjTiI3tmjm9hxmJwxM6V1luasny3XnYyduwn1RwIghZHSHbY7cDoWoBqzUK4fhPpJYhAVEqO5YBm1B__]]></ClickTracking></VideoClicks></Linear></Creative></Creatives>  <Impression><![CDATA[http://api.cdange.mobi/xp/evt?srid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&osrid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&t=1530734706&pubid=0cf8d6d643e13d86a5b6374148a4afac&impid=c3fb7fda-758e-46b2-92b8-1e01e64bc770&cip=223.225.56.64&cid=1222&crid=5370&b=chalkdigital.com&c=IND&wh=1024x768&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Macintosh%3B+Intel+Mac+OS+X+10_13_5%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Chrome%2F67.0.3396.99+Safari%2F537.36&carrier=&ct=New+Delhi&st=DL&z=&bek=8c830e1706e9d12a5771daddb3678023_20180704_20&le=0&userid=2c3e7bb4f7cceb6dc5bb66d2b24cf43a4797911f&bidid=d26014f7-3bf4-4e84-ab2f-0446a0b563d6&ifa=&m=1&act=5]]></Impression></Wrapper>  </Ad></VAST>";
//            mVastResponse = CDAdConstants.sparkJSPrefix.replace("vpaid_xml", mVastResponse).replace("ad_width", serverExtras.get(DataKeys.WIDTH)).replace("ad_height", "auto");
            mVastResponse = mVastResponse.replace(System.getProperty("line.separator"), "");

            final String externalViewabilityTrackers =
                    serverExtras.get(DataKeys.EXTERNAL_VIDEO_VIEWABILITY_TRACKERS_KEY);
            try {
                mExternalViewabilityTrackers = Json.jsonStringToMap(externalViewabilityTrackers);
            } catch (JSONException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Failed to parse video viewability trackers to JSON: " +
                        externalViewabilityTrackers);
            }

            final String videoTrackers = serverExtras.get(DataKeys.VIDEO_TRACKERS_KEY);
            if (TextUtils.isEmpty(videoTrackers)) {
                return;
            }
            try {
                mVideoTrackers = new JSONObject(videoTrackers);
            } catch (JSONException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Failed to parse video trackers to JSON: " + videoTrackers, e);
                mVideoTrackers = null;
            }
        }


        public CDAdVideoNativeAd(
                @NonNull final Context context,
                @NonNull final CustomEventNativeListener customEventNativeListener,
                @NonNull final Map<String, String> serverExtras) {
            this(context, customEventNativeListener, serverExtras,
                    new VisibilityTracker(context), new NativeVideoControllerFactory());
        }

        @VisibleForTesting
        CDAdVideoNativeAd(
                @NonNull final Context context,
                @NonNull final CustomEventNativeListener customEventNativeListener,
                @NonNull final Map<String, String> serverExtras,
                @NonNull final VisibilityTracker visibilityTracker,
                @NonNull final NativeVideoControllerFactory nativeVideoControllerFactory) {
            Preconditions.checkNotNull(context);
            Preconditions.checkNotNull(customEventNativeListener);
            Preconditions.checkNotNull(serverExtras);
            Preconditions.checkNotNull(visibilityTracker);
            Preconditions.checkNotNull(nativeVideoControllerFactory);



            mContext = context.getApplicationContext();

            extractExtras(serverExtras);



            mCustomEventNativeListener = customEventNativeListener;


            mNativeVideoControllerFactory = nativeVideoControllerFactory;


            mId = Utils.generateUniqueId();
            mNeedsSeek = true;
            mVideoState = VideoState.CREATED;

            mNeedsPrepare = true;
            mLatestVideoControllerState = NativeVideoController.STATE_IDLE;
            mMuted = true;
            mVideoVisibleTracking = visibilityTracker;
            mVideoVisibleTracking.setVisibilityTrackerListener(new VisibilityTracker
                    .VisibilityTrackerListener() {
                @Override
                public void onVisibilityChanged(final List<View> visibleViews,
                        final List<View> invisibleViews) {
                    if (!visibleViews.isEmpty() && !mLatestVisibility) { // State transition
                        mLatestVisibility = true;
                        maybeChangeState();
                    } else if (!invisibleViews.isEmpty() && mLatestVisibility) { // state transition
                        mLatestVisibility = false;
                        maybeChangeState();
                    }
                }
            });


        }

        private void postViewabilityEvent(ExternalViewabilitySession.VideoEvent event, Integer duration, Integer playHeadMillis, int volume, String message){
            if (mExternalViewabilitySessionManager != null) {
                mExternalViewabilitySessionManager.recordVideoEvent(event, duration, playHeadMillis, volume);
            }
        }

        @VisibleForTesting
        public void setDebugListener(@Nullable SparkWebViewDebugListener debugListener) {
            mDebugListener = debugListener;
            if (mSparkVideoViewController != null) {
                mSparkVideoViewController.setDebugListener(debugListener);
            }
        }

        void loadAd() throws IllegalArgumentException {

            mSparkVideoViewController = new SparkVideoViewController(
                    mContext,null, PlacementType.INLINE, null, mClickAction);

            mSparkVideoViewController.setDebugListener(mDebugListener);
            mSparkVideoViewController.setSparkListener(new SparkVideoViewController.SparkListener() {
                @Override
                public void onLoaded(View view) {

                }

                @Override
                public void onFailedToLoad() {
                    CDAdLog.d("SparkActivity failed to load. Finishing the activity");
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.RECORD_AD_ERROR, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                    mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.INVALID_RESPONSE);
                }

                public void onClose() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_STOPPED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onExpand() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PLAYER_STATE_CHNAGED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onOpen() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_CLICK_THRU, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                    mCustomEventNativeListener.onNativeAdClicked(CDAdVideoNativeAd.this);
                }

                @Override
                public void onPlaying() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PLAYING, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onPlay() {

                }

                @Override
                public void onError(String errorDesc) {
                    NativeErrorCode errorCode = NativeErrorCode.UNSPECIFIED;
                    if (errorDesc!=null){
                        errorCode.setMessage(errorDesc);
                    }
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.RECORD_AD_ERROR, null, null, mSparkVideoViewController.isMuted()?0:1, errorDesc);
                    mCustomEventNativeListener.onNativeAdFailed(errorCode);
                }


                @Override
                public void onReady() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_LOADED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onEnded() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_COMPLETE, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                    applyState(VideoState.ENDED);
                }

                @Override
                public void onCancelled() {

                }

                @Override
                public void onTimeout() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.RECORD_AD_ERROR, null, null, mSparkVideoViewController.isMuted()?0:1, "AD Timeout");
                }

                @Override
                public void onStarted() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_STARTED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onPause() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PAUSED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onResume() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_RESUME, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onFirstQuatile() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_FIRST_QUARTILE, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onMidpoint() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_MIDPOINT, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onThirdQuartile() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VIDEO_THIRD_QUARTILE, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onMuted() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VOLUME_CHANGED, null, null, 0, null);
                }

                @Override
                public void onVolumeChanged() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_VOLUME_CHANGED, null, null, 1, null);
                }

                @Override
                public void onStateChanged(final Integer currentState) {
                    if (currentState!=null)
                        postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_PLAYER_STATE_CHNAGED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onImpression() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_IMPRESSED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }

                @Override
                public void onSkipped() {
                    postViewabilityEvent(ExternalViewabilitySession.VideoEvent.AD_SKIPPED, null, null, mSparkVideoViewController.isMuted()?0:1, null);
                }
            });



            mCustomEventNativeListener.onNativeAdLoaded(CDAdVideoNativeAd.this);
        }


        private boolean containsRequiredKeys(@NonNull final JSONObject jsonObject) {
            Preconditions.checkNotNull(jsonObject);

            final Set<String> keys = new HashSet<String>();
            final Iterator<String> jsonKeys = jsonObject.keys();
            while (jsonKeys.hasNext()) {
                keys.add(jsonKeys.next());
            }

            return keys.containsAll(Parameter.requiredKeys);
        }

//        private void addInstanceVariable(@NonNull final Parameter key,
//                @Nullable final Object value) throws ClassCastException {
//            Preconditions.checkNotNull(key);
//            Preconditions.checkNotNull(value);
//
//            try {
//                switch (key) {
//                    case IMPRESSION_TRACKER:
//                        addImpressionTrackers(value);
//                        break;
//                    case TITLE:
//                        setTitle((String) value);
//                        break;
//                    case TEXT:
//                        setText((String) value);
//                        break;
//                    case IMAGE_URL:
//                        setMainImageUrl((String) value);
//                        break;
//                    case ICON_URL:
//                        setIconImageUrl((String) value);
//                        break;
//                    case CLICK_DESTINATION:
//                        setClickDestinationUrl((String) value);
//                        break;
//                    case CLICK_TRACKER:
//                        parseClickTrackers(value);
//                        break;
//                    case CALL_TO_ACTION:
//                        setCallToAction((String) value);
//                        break;
//                    case VAST_VIDEO:
//                        setVastVideo((String) value);
//                        break;
//                    default:
//                        CDAdLog.d("Unable to add JSON key to internal mapping: " + key.mName);
//                        break;
//                }
//            } catch (ClassCastException e) {
//                        Utils.logStackTrace(e);
//                if (!key.mRequired) {
//                    CDAdLog.d("Ignoring class cast exception for optional key: " + key.mName);
//                } else {
//                    throw e;
//                }
//            }
//        }

        private void parseClickTrackers(@NonNull final Object clickTrackers) {
            if (clickTrackers instanceof JSONArray) {
                addClickTrackers(clickTrackers);
            } else {
                addClickTracker((String) clickTrackers);
            }
        }


        public void render(@NonNull ViewGroup viewGroup) {
            RelativeLayout relativeLayout = new RelativeLayout(mContext);
            final RelativeLayout.LayoutParams adViewLayout = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            adViewLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
            relativeLayout.addView(mSparkVideoViewController.getAdContainer(), 0, adViewLayout);
            viewGroup.addView(relativeLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        public void render(@NonNull MediaLayout mediaLayout) {
            Preconditions.checkNotNull(mediaLayout);

//            mVideoVisibleTracking.addView(mRootView,
//                    mediaLayout,
//                    50,
//                    50,
//                    50);

            mMediaLayout = mediaLayout;
            mMediaLayout.initForVideo();

            mMediaLayout.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int
                        height) {

//                    mNativeVideoController.setListener(CDAdVideoNativeAd.this);
//                    mNativeVideoController.setOnAudioFocusChangeListener(CDAdVideoNativeAd.this);
//                    mNativeVideoController.setProgressListener(CDAdVideoNativeAd.this);
//                    mNativeVideoController.setTextureView(mMediaLayout.getTextureView());
//                    mMediaLayout.resetProgress();
//
//                    // If we're returning to an ended video, make a note of that so we don't flash
//                    // a bunch of UI changes while we prepare the data.
//                    final long duration = mNativeVideoController.getDuration();
//                    final long currentPosition = mNativeVideoController.getCurrentPosition();
//                    if (mLatestVideoControllerState == NativeVideoController.STATE_ENDED
//                        || (duration > 0 && duration - currentPosition < NativeVideoController.RESUME_FINISHED_THRESHOLD)) {
//                        mEnded = true;
//                    }
//
//                    if (mNeedsPrepare) {
//                        mNeedsPrepare = false;
//                        mNativeVideoController.prepare(CDAdVideoNativeAd.this);
//                    }
//
//                    mNeedsSeek = true;
//                    maybeChangeState();
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                        int height) { }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
//                    mNeedsPrepare = true;
//                    mNativeVideoController.release(CDAdVideoNativeAd.this);
//                    applyState(VideoState.PAUSED);
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
            });

            mMediaLayout.setPlayButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mMediaLayout.resetProgress();
//                    mNativeVideoController.seekTo(0);
                    mEnded = false;
//                    mNeedsSeek = false;
                    if (mVideoState == VideoState.PAUSED){
                        mSparkVideoViewController.loadJavascript("resume()");
                        applyState(VideoState.PLAYING);
                    }else if(mVideoState == VideoState.ENDED){
                        mSparkVideoViewController.loadJavascript("start()");
                        applyState(VideoState.PLAYING);
                    }
                }
            });

            mMediaLayout.setPauseButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSparkVideoViewController.loadJavascript("pause()");
                    applyState(VideoState.PAUSED);
                }
            });

            mMediaLayout.setMuteControlClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    mMuted = !mMuted;
                    mMediaLayout.setMuteState(mMuted?MediaLayout.MuteState.MUTED: MediaLayout.MuteState.UNMUTED);
                    mSparkVideoViewController.loadJavascript("muteAds("+(!mMuted?"1)":"0)"));
                }
            });

            mMediaLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                }
            });


            Utils.getSavedVpaidHTML(mVpaidSourceUrl, mContext, new Utils.VpaidSourceListener() {
                @Override
                public void onSuccess(final String html) {
                    loadVpaidContent(html);
                }

                @Override
                public void onFailure() {
                    mCustomEventNativeListener.onNativeAdFailed(NativeErrorCode.VPAID_SOURCE_ERROR);
                    return;
                }
            });

        }

        public void loadVpaidContent(String vpaidSource){
            float density = mContext.getResources().getDisplayMetrics().density;
            if (density <= 0) {
                density = 1;
            }
            if (mMediaLayout == null)
                return;
            mVastResponse = vpaidSource.replace("vast_response", mVastResponse).replace("ad_width", mMediaLayout.getLayoutParams().width/density +"").replace("ad_height", mMediaLayout.getLayoutParams().width*MediaLayout.ASPECT_MULTIPLIER_WIDTH_TO_HEIGHT/density +"").replace("timer_bottom_margin", "0px");
            mSparkVideoViewController.fillContent(Long.parseLong("1"), mVastResponse,
                    new SparkVideoViewController.SparkWebViewCacheListener() {
                        @Override
                        public void onReady(@NonNull final SparkBridge.SparkWebView webView,
                                            @Nullable final ExternalViewabilitySessionManager viewabilityManager) {
                            if (viewabilityManager != null) {
                                mExternalViewabilitySessionManager = viewabilityManager;
                            } else {
                                mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager(mContext);
//                                mExternalViewabilitySessionManager.createVideoSession(Utils.getActivity(mContext), webView, mServerExtras);
                            }
                        }
                    });
            final RelativeLayout.LayoutParams adViewLayout = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            adViewLayout.addRule(RelativeLayout.CENTER_IN_PARENT);
            mMediaLayout.addView(mSparkVideoViewController.getAdContainer(), 0, adViewLayout);
            mMediaLayout.setMode(MediaLayout.Mode.PLAYING);
            mMediaLayout.setMuteState(MediaLayout.MuteState.MUTED);
            applyState(VideoState.PLAYING);
        }

        // Lifecycle Handlers
        @Override
        public void prepare(@NonNull final View view) {
            Preconditions.checkNotNull(view);
//            mRootView = view;
//            mRootView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    prepareToLeaveView();
//                    // No need to call notifyAdClicked since handleCtaClick does clickTracking
////                    mNativeVideoController.triggerImpressionTrackers();
////                    mNativeVideoController.handleCtaClick(mContext);
//                }
//            });
        }

        @Override
        public void clear(@NonNull final View view) {
            Preconditions.checkNotNull(view);
//            mNativeVideoController.clear();
            cleanUpMediaLayout();
        }

        @Override
        public void destroy() {
            cleanUpMediaLayout();
//            mNativeVideoController.setPlayWhenReady(false);
//            mNativeVideoController.release(this);
//            NativeVideoController.remove(mId);
//            mVideoVisibleTracking.destroy();
        }


        public void onStateChanged(final boolean playWhenReady, final int playbackState) {
            mLatestVideoControllerState = playbackState;
            maybeChangeState();
        }


        public void onError(final Exception e) {
                        Utils.logStackTrace(e);
            CDAdLog.w("Error playing back video.", e);
            mError = true;
            maybeChangeState();
        }

        @Override
        public void updateProgress(final int progressTenthPercent) {
            mMediaLayout.updateProgress(progressTenthPercent);
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                    || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                // Mute the video
                mMuted = true;
                maybeChangeState();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume
//                mNativeVideoController.setAudioVolume(0.3f);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Resume playback
//                mNativeVideoController.setAudioVolume(1.0f);
                maybeChangeState();
            }
        }

        private void cleanUpMediaLayout() {
            // When clearing, we also clear medialayout references so if we're rendered again
            // with the same view, we reset the video state correctly.
            if (mMediaLayout != null) {
                mMediaLayout.setMode(MediaLayout.Mode.IMAGE);
                mMediaLayout.setSurfaceTextureListener(null);
                mMediaLayout.setPlayButtonClickListener(null);
                mMediaLayout.setMuteControlClickListener(null);
                mMediaLayout.setOnClickListener(null);
                mVideoVisibleTracking.removeView(mMediaLayout);
                mMediaLayout = null;
            }
        }

        private void prepareToLeaveView() {
//            mNeedsSeek = true;
//            mNeedsPrepare = true;
//
//            // Clean up any references to this class when storing the NativeVideoController
//            // in a static location and starting a new activity
//            mNativeVideoController.setListener(null);
//            mNativeVideoController.setOnAudioFocusChangeListener(null);
//            mNativeVideoController.setProgressListener(null);
//            mNativeVideoController.clear();

            applyState(VideoState.PAUSED, true);
        }

        private void maybeChangeState() {
            VideoState newState = mVideoState;

            if (mError) {
                newState = VideoState.FAILED_LOAD;
            } else if (mEnded) {
                newState = VideoState.ENDED;
            } else {
                if (mLatestVideoControllerState == NativeVideoController.STATE_IDLE) {
                    newState = VideoState.LOADING;
                } else if (mLatestVideoControllerState == NativeVideoController.STATE_BUFFERING) {
                    newState = VideoState.BUFFERING;
                } else if (mLatestVideoControllerState == NativeVideoController.STATE_ENDED) {
                    mEnded = true;
                    newState = VideoState.ENDED;
                } else if (mLatestVideoControllerState == NativeVideoController.STATE_READY) {
                    if (mLatestVisibility) {
                        newState = mMuted ? VideoState.PLAYING_MUTED : VideoState.PLAYING;
                    } else {
                        newState = VideoState.PAUSED;
                    }
                }
            }

            applyState(newState);
        }

        @VisibleForTesting
        void applyState(@NonNull final VideoState videoState) {
            applyState(videoState, false);
        }

        @VisibleForTesting
        void applyState(@NonNull final VideoState videoState, boolean transitionToFullScreen) {
            Preconditions.checkNotNull(videoState);

//             Ignore the state change if video player is not ready to take state changes.
            if (mMediaLayout == null) {
                return;
            }

            // Check and set mVideoState so any changes we make to exo state don't
            // trigger a duplicate run of this.
            if (mVideoState == videoState) {
                return;
            }
            VideoState previousState = mVideoState;
            mVideoState = videoState;

            switch (videoState) {
                case FAILED_LOAD:
                    mNativeVideoController.setAppAudioEnabled(false);
                    mMediaLayout.setMode(MediaLayout.Mode.IMAGE);
                    // Only log the failed to play event when the video has not started
//                    if (previousState != VideoState.PLAYING && previousState != VideoState.PLAYING_MUTED) {
//                        CDAdEvents.log(Event.createEventFromDetails(
//                                BaseEvent.Name.ERROR_FAILED_TO_PLAY,
//                                BaseEvent.Category.NATIVE_VIDEO,
//                                BaseEvent.SamplingRate.NATIVE_VIDEO,
//                                null));
//                    }
                    break;
                case CREATED:
                case LOADING:
                    mMediaLayout.setMode(MediaLayout.Mode.LOADING);
                    break;
                case BUFFERING:
                    mMediaLayout.setMode(MediaLayout.Mode.BUFFERING);
                    break;
                case PAUSED:
                    mMediaLayout.setMode(MediaLayout.Mode.PAUSED);
                    break;
                case PLAYING:
                    mMediaLayout.setMode(MediaLayout.Mode.PLAYING);
                    break;
                case PLAYING_MUTED:
                    mMediaLayout.setMode(MediaLayout.Mode.PLAYING);
                    mMediaLayout.setMuteState(MediaLayout.MuteState.MUTED);
                    break;
                case ENDED:
                    mMediaLayout.setMode(MediaLayout.Mode.FINISHED);
                    mCustomEventNativeListener.onNativeAdVideoEnded(this);
                    break;
            }
        }

        private void handleResumeTrackersAndSeek(VideoState previousState) {
            if (mResumeCanBeTracked
                    && previousState != VideoState.PLAYING
                    && previousState != VideoState.PLAYING_MUTED) {
                mResumeCanBeTracked = false;
            }

            mPauseCanBeTracked = true;

            // We force a seek here to get keyframe rendering in ExtractorSampleSource.
            if (mNeedsSeek) {
                mNeedsSeek = false;
                mNativeVideoController.seekTo(mNativeVideoController.getCurrentPosition());
            }
        }


        private boolean isImageKey(@Nullable final String name) {
            return name != null && name.toLowerCase(Locale.US).endsWith("image");
        }


        @NonNull
        private List<String> getAllImageUrls() {
            final List<String> imageUrls = new ArrayList<String>();
            return imageUrls;
        }

        @Deprecated
        @VisibleForTesting
        boolean needsPrepare() {
            return mNeedsPrepare;
        }

        @Deprecated
        @VisibleForTesting
        boolean hasEnded() {
            return mEnded;
        }

        @Deprecated
        @VisibleForTesting
        boolean needsSeek() {
            return mNeedsSeek;
        }

        @Deprecated
        @VisibleForTesting
        boolean isMuted() {
            return mMuted;
        }

        @Deprecated
        @VisibleForTesting
        long getId() {
            return mId;
        }

        @Deprecated
        @VisibleForTesting
        VideoState getVideoState() {
            return mVideoState;
        }

        @Deprecated
        @VisibleForTesting
        void setLatestVisibility(boolean latestVisibility) {
            mLatestVisibility = latestVisibility;
        }

        @Deprecated
        @VisibleForTesting
        void setMuted(boolean muted) {
            mMuted = muted;
        }

        @Deprecated
        @VisibleForTesting
        MediaLayout getMediaLayout() {
            return mMediaLayout;
        }
    }

    @VisibleForTesting
    static class HeaderVisibilityStrategy implements VisibilityTrackingEvent.OnTrackedStrategy {
        @NonNull private final WeakReference<CDAdVideoNativeAd> mCDAdVideoNativeAd;

        HeaderVisibilityStrategy(@NonNull final CDAdVideoNativeAd cdAdVideoNativeAd) {
            mCDAdVideoNativeAd = new WeakReference<CDAdVideoNativeAd>(cdAdVideoNativeAd);
        }

        @Override
        public void execute() {
            final CDAdVideoNativeAd cdAdVideoNativeAd = mCDAdVideoNativeAd.get();
            if (cdAdVideoNativeAd != null) {
                cdAdVideoNativeAd.notifyAdImpressed();
            }
        }
    }

    @VisibleForTesting
    static class PayloadVisibilityStrategy implements VisibilityTrackingEvent.OnTrackedStrategy {
        @NonNull private final Context mContext;
        @NonNull private final String mUrl;

        PayloadVisibilityStrategy(@NonNull final Context context, @NonNull final String url) {
            mContext = context.getApplicationContext();
            mUrl = url;
        }

        @Override
        public void execute() {
            TrackingRequest.makeTrackingHttpRequest(mUrl, mContext);
        }
    }

    /**
     * Created purely for the purpose of mocking to ease testing.
     */
    @VisibleForTesting
    static class NativeVideoControllerFactory {
        public NativeVideoController createForId(final long id,
                @NonNull final Context context,
                @NonNull final List<VisibilityTrackingEvent> visibilityTrackingEvents,
                @Nullable final EventDetails eventDetails) {
            return NativeVideoController.createForId(id, context, visibilityTrackingEvents, eventDetails);
        }
    }

    @VisibleForTesting
    static class VideoResponseHeaders {
        private boolean mHeadersAreValid;
        private int mPlayVisiblePercent;
        private int mPauseVisiblePercent;
        private int mImpressionMinVisiblePercent;
        private int mImpressionVisibleMs;
        private int mMaxBufferMs;
        private Integer mImpressionVisiblePx;
        private JSONObject mVideoTrackers;

        VideoResponseHeaders(@NonNull final Map<String, String> serverExtras) {
            try {
                mPlayVisiblePercent = Integer.parseInt(serverExtras.get(PLAY_VISIBLE_PERCENT));
                mPauseVisiblePercent = Integer.parseInt(serverExtras.get(PAUSE_VISIBLE_PERCENT));
                mImpressionVisibleMs = Integer.parseInt(serverExtras.get(IMPRESSION_VISIBLE_MS));
                mMaxBufferMs = Integer.parseInt(serverExtras.get(MAX_BUFFER_MS));
                mHeadersAreValid = true;
            } catch (NumberFormatException e) {
                        Utils.logStackTrace(e);
                mHeadersAreValid = false;
            }

            final String impressionVisiblePxString = serverExtras.get(IMPRESSION_MIN_VISIBLE_PX);
            if (!TextUtils.isEmpty(impressionVisiblePxString)) {
                try {
                    mImpressionVisiblePx = Integer.parseInt(impressionVisiblePxString);
                } catch (NumberFormatException e) {
                        Utils.logStackTrace(e);
                    CDAdLog.d("Unable to parse impression min visible px from server extras.");
                }
            }
            try {
                mImpressionMinVisiblePercent =
                        Integer.parseInt(serverExtras.get(IMPRESSION_MIN_VISIBLE_PERCENT));
            } catch (NumberFormatException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Unable to parse impression min visible percent from server extras.");
                if (mImpressionVisiblePx == null || mImpressionVisiblePx < 0) {
                    mHeadersAreValid = false;
                }
            }


            final String videoTrackers = serverExtras.get(VIDEO_TRACKERS_KEY);
            if (TextUtils.isEmpty(videoTrackers)) {
                return;
            }

            try {
                mVideoTrackers = new JSONObject(videoTrackers);
            } catch (JSONException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Failed to parse video trackers to JSON: " + videoTrackers, e);
                mVideoTrackers = null;
            }
        }

        boolean hasValidHeaders() {
            return mHeadersAreValid;
        }

        int getPlayVisiblePercent() {
            return mPlayVisiblePercent;
        }

        int getPauseVisiblePercent() {
            return mPauseVisiblePercent;
        }

        int getImpressionMinVisiblePercent() {
            return mImpressionMinVisiblePercent;
        }

        int getImpressionVisibleMs() {
            return mImpressionVisibleMs;
        }

        int getMaxBufferMs() {
            return mMaxBufferMs;
        }

        @Nullable
        Integer getImpressionVisiblePx() {
            return mImpressionVisiblePx;
        }

        JSONObject getVideoTrackers() {
            return mVideoTrackers;
        }
    }


}
