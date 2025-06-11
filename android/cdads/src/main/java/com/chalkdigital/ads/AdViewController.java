package com.chalkdigital.ads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chalkdigital.common.CDAdActions;
import com.chalkdigital.common.CDAdConnectivityChangeReceiver;
import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CDAdIPGeolocationManager;
import com.chalkdigital.common.CDAdLocationManager;
import com.chalkdigital.common.CDAdRequest;
import com.chalkdigital.common.CDAdSize;
import com.chalkdigital.common.CDAdsUtils;
import com.chalkdigital.common.AdReport;
import com.chalkdigital.common.BorderLayout;
import com.chalkdigital.common.ClientMetadata;
import com.chalkdigital.common.CloseableLayout;
import com.chalkdigital.common.LocationService;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Dips;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.mediation.CDMediationAdRequest;
import com.chalkdigital.mraid.MraidNativeCommandHandler;
import com.chalkdigital.network.CDAdNetworkError;
import com.chalkdigital.network.AdRequest;
import com.chalkdigital.network.response.AdResponse;
import com.chalkdigital.network.response.Event;
import com.chalkdigital.network.response.NetworkResponse;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import retrofit2.Response;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;

public class AdViewController {
    private static final String TAG = "AdViewController";
    private boolean isWaitingForLocationUpdate;
    private boolean isWaitingForIP;
    private boolean shouldSkipIp;
    private boolean shouldSkipLocation;
    private boolean mAtLeastOneAdLoaded;
    private long mLastAdRequestTime;
    private boolean mIsPaused;
    private Handler mLocationHandler;
    private Runnable mLocationRunnable;
    private CDMediationAdRequest mCdMediationAdRequest;
    private CDAdBroadcastReceiver mCDAdBroadcastReceiver;
    static final int MAX_REFRESH_TIME = 60; // 10 seconds
    static final int MIN_REFRESH_TIME = 10; // 10 seconds
    private static final FrameLayout.LayoutParams WRAP_AND_CENTER_LAYOUT_PARAMS =
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER);
    private static final FrameLayout.LayoutParams WRAP_AND_BOTTOM_CENTER_LAYOUT_PARAMS =
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM);
    private final static WeakHashMap<View,Boolean> sViewShouldHonorServerDimensions = new WeakHashMap<View, Boolean>();

    private final long mBroadcastIdentifier;

    @Nullable private Context mContext;
    @Nullable private CDAdView mCDAdView;
    @Nullable private WebAdRequestBodyGenerator mUrlGenerator;

    @Nullable private AdResponse mAdResponse;
    @Nullable private String mCustomEventClassName;
    private final Runnable mRefreshRunnable;
    @NonNull private final AdRequest.Listener mAdListener;
    @NonNull private CDAdRequest mCDAdRequest;

    private boolean mIsDestroyed;
    private Handler mHandler;
    private boolean mIsLoading;
    private String mUrl;

    /**
     * This is the publisher-specified auto refresh flag. AdViewController will only attempt to
     * refresh ads when this is true. Setting this to false will block refreshing.
     */
    private boolean mShouldAllowAutoRefresh = false;
    private boolean mAutoRefreshStatus = false;

    private boolean mAdWasLoaded;
    private int mTimeoutMilliseconds;
    @Nullable private AdRequest mActiveRequest;
    @Nullable private Integer mRefreshTime;

    public static void setShouldHonorServerDimensions(View view) {
        sViewShouldHonorServerDimensions.put(view, true);
    }

    private static boolean getShouldHonorServerDimensions(View view) {
        return sViewShouldHonorServerDimensions.get(view) != null;
    }

    public AdViewController(@NonNull Context context, @NonNull CDAdView view) {
        mContext = context;
        mCDAdView = view;

        // Timeout value of less than 0 means use the ad format's default timeout
        mTimeoutMilliseconds = -1;
        mBroadcastIdentifier = Utils.generateUniqueId();

        mUrlGenerator = new WebAdRequestBodyGenerator(mContext.getApplicationContext(),
                MraidNativeCommandHandler.isStorePictureSupported(mContext));

        mAdListener = new AdRequest.Listener() {

            @Override
            public void onAdRequestSuccess(Response response, Object object, int apitype) {
                try {
                    NetworkResponse networkResponse = (NetworkResponse) response.body();
//                    networkResponse = (NetworkResponse) new Gson().fromJson("{\"bidid\":\"2-80000614664-681879-42486243\",\"cur\":\"USD\",\"id\":\"8BA816E5-2E2C-470D-983D-650F67A160E0\",\"seatbid\":[{\"bid\":[{\"adid\":\"2-80000614664-681879-42486243-80000614664\",\"adm\":\"<?xml version=\\\"1.0\\\"?><VAST version=\\\"2.0\\\"><Ad id=\\\"2-80000614664-681879-42486243\\\"><InLine><AdSystem version=\\\"1.0.0\\\">2</AdSystem><AdTitle>Chalk SDK VAST Interstitial Testing</AdTitle><Description></Description><Error><![CDATA[https://n2.cmcd1.com/act?a=74&piggyback=RzCHVodDnlLC972Q7ui0SJ7zh4ptH2PeaMwxoVxEuVhNBdqP84vGvE684mrONf1C80dd9dvaLcsb6KQexNT5KL1Sr52BLAtdQlAVKSxHOfOhPUT3O6ewpjkvBrlvEAzjyH4X5o1BaNG2jEqMfpk5ue3kwlM9Ig15hR0hiLtrYpWAyECScdcrQ2mUPe2xetufClmzMxwn7lG7u5WurgLVyvVQVgo4reXj07qCVtklJq2rYG43FVz4GmGYSmCE69ZeaR3Nt9CmecvlVJf5MzU47187gdchEaKFR6KsL6llSg7GmBuqRQo64EdqWDZDRiRCLBhxIzH7ePs8il09wR4G5pSxDPJPUe6uzvJ1y4YShHuzwLkKY4F1&ecode=[ERRORCODE]]]></Error><Impression><![CDATA[https://n2.cmcd1.com/act?a=1&bid=.0090&wpalgo=0&pid=chalkboard&lid=2-80000614664-681879-42486243&piggyback=cSznTn5CNfR9NJlzRrTwBdsdfIUgThjUOS2Xtai6uTqgbvZ2KhEEiqCJJXOBFsmLvgqfZ5iLkWFNmqCNa5L0OcdxOg2WrBYgqLVjUHuXqjrgRChjz2fQtNjYWeGCJHMGTJSNalea62eYz2Ddfd9GOTc3jBd5l2yVAUjEzkfRSdIWfdyvzutRLLvWkYjFEeL3IhTDg3Ff93C9n7i4biG69ETQJ5TVYc4MiXXOGBQc5uWtfadeMSaMiquTumqP0ZxX2xNNbAqUtUw3zib9CNOJhYiIdRUMs96DrbxsqpqNc9DKI9gMmxw0QkNJaLFH4D9FoN8ZVwI2h95zWiby2jUGZNUEZCELuzefXhy3Yu7eCmBOezIBC4bSI47UdxlJ2o72SNxqtdb6t2gKNJmwxGGo4i1Tga8LKthay3fcJ006dmUrAsoEJsA7V8gxNeicZ6Keg5BRXEQsqgPCpSPvZyEQztfsOLWrKbXrb9X6lnS6h5dxylRJ9a8FTEg6ERoC0LfrWWi0goOVsz0pZo3sBT4OuPFGrV8vU3F5KmnDLOxp9x08gsmuSPAB2xhGkYKHImEW1hgHRYU79yjE24MmfwOoOvrMp7fLkTureKQE8hSawMZ2Lv08l22dOSTn9t6cZaSrDfwEn6Flzk3Z6d1pW1ZjGiVPvsmzSwK9vkr96pdcAtONjp0uls5b0jxghstw30eWvygXjn3KWKPd6vqyiYfYwvw6V7iVUxCdiyoeXKSDnV3IWLD35Wq2IOB5boO054sJUyArKgy&aid=8BA816E5-2E2C-470D-983D-650F67A160E0]]></Impression><Creatives><Creative sequence=\\\"1\\\" id=\\\"2\\\"><Linear><Duration>00:00:15</Duration><TrackingEvents><Tracking event=\\\"start\\\"><![CDATA[https://n2.cmcd1.com/act?a=41&piggyback=RzCHVodDnlLC972Q7ui0SJ7zh4ptH2PeaMwxoVxEuVhNBdqP84vGvE684mrONf1C80dd9dvaLcsb6KQexNT5KL1Sr52BLAtdQlAVKSxHOfOhPUT3O6ewpjkvBrlvEAzjyH4X5o1BaNG2jEqMfpk5ue3kwlM9Ig15hR0hiLtrYpWAyECScdcrQ2mUPe2xetufClmzMxwn7lG7u5WurgLVyvVQVgo4reXj07qCVtklJq2rYG43FVz4GmGYSmCE69ZeaR3Nt9CmecvlVJf5MzU47187gdchEaKFR6KsL6llSg7GmBuqRQo64EdqWDZDRiRCLBhxIzH7ePs8il09wR4G5pSxDPJPUe6uzvJ1y4YShHuzwLkKY4F1]]></Tracking><Tracking event=\\\"complete\\\"><![CDATA[https://n2.cmcd1.com/act?a=45&piggyback=RzCHVodDnlLC972Q7ui0SJ7zh4ptH2PeaMwxoVxEuVhNBdqP84vGvE684mrONf1C80dd9dvaLcsb6KQexNT5KL1Sr52BLAtdQlAVKSxHOfOhPUT3O6ewpjkvBrlvEAzjyH4X5o1BaNG2jEqMfpk5ue3kwlM9Ig15hR0hiLtrYpWAyECScdcrQ2mUPe2xetufClmzMxwn7lG7u5WurgLVyvVQVgo4reXj07qCVtklJq2rYG43FVz4GmGYSmCE69ZeaR3Nt9CmecvlVJf5MzU47187gdchEaKFR6KsL6llSg7GmBuqRQo64EdqWDZDRiRCLBhxIzH7ePs8il09wR4G5pSxDPJPUe6uzvJ1y4YShHuzwLkKY4F1]]></Tracking><Tracking event=\\\"close\\\"><![CDATA[https://n2.cmcd1.com/act?a=51&piggyback=RzCHVodDnlLC972Q7ui0SJ7zh4ptH2PeaMwxoVxEuVhNBdqP84vGvE684mrONf1C80dd9dvaLcsb6KQexNT5KL1Sr52BLAtdQlAVKSxHOfOhPUT3O6ewpjkvBrlvEAzjyH4X5o1BaNG2jEqMfpk5ue3kwlM9Ig15hR0hiLtrYpWAyECScdcrQ2mUPe2xetufClmzMxwn7lG7u5WurgLVyvVQVgo4reXj07qCVtklJq2rYG43FVz4GmGYSmCE69ZeaR3Nt9CmecvlVJf5MzU47187gdchEaKFR6KsL6llSg7GmBuqRQo64EdqWDZDRiRCLBhxIzH7ePs8il09wR4G5pSxDPJPUe6uzvJ1y4YShHuzwLkKY4F1]]></Tracking></TrackingEvents><VideoClicks><ClickThrough><![CDATA[https://www.chalkdigital.com]]></ClickThrough><ClickTracking><![CDATA[https://n2.cmcd1.com/act?a=2&bid=.0090&wpalgo=0&pid=chalkboard&lid=2-80000614664-681879-42486243&piggyback=cSznTn5CNfR9NJlzRrTwBdsdfIUgThjUOS2Xtai6uTqgbvZ2KhEEiqCJJXOBFsmLvgqfZ5iLkWFNmqCNa5L0OcdxOg2WrBYgqLVjUHuXqjrgRChjz2fQtNjYWeGCJHMGTJSNalea62eYz2Ddfd9GOTc3jBd5l2yVAUjEzkfRSdIWfdyvzutRLLvWkYjFEeL3IhTDg3Ff93C9n7i4biG69ETQJ5TVYc4MiXXOGBQc5uWtfadeMSaMiquTumqP0ZxX2xNNbAqUtUw3zib9CNOJhYiIdRUMs96DrbxsqpqNc9DKI9gMmxw0QkNJaLFH4D9FoN8ZVwI2h95zWiby2jUGZNUEZCELuzefXhy3Yu7eCmBOezIBC4bSI47UdxlJ2o72SNxqtdb6t2gKNJmwxGGo4i1Tga8LKthay3fcJ006dmUrAsoEJsA7V8gxNeicZ6Keg5BRXEQsqgPCpSPvZyEQztfsOLWrKbXrb9X6lnS6h5dxylRJ9a8FTEg6ERoC0LfrWWi0goOVsz0pZo3sBT4OuPFGrV8vU3F5KmnDLOxp9x08gsmuSPAB2xhGkYKHImEW1hgHRYU79yjE24MmfwOoOvrMp7fLkTureKQE8hSawMZ2Lv08l22dOSTn9t6cZaSrDfwEn6Flzk3Z6d1pW1ZjGiVPvsmzSwK9vkr96pdcAtONjp0uls5b0jxghstw30eWvygXjn3KWKPd6vqyiYfYwvw6V7iVUxCdiyoeXKSDnV3IWLD35Wq2IOB5boO054sJUyArKgy&aid=8BA816E5-2E2C-470D-983D-650F67A160E0]]></ClickTracking></VideoClicks><MediaFiles><MediaFile delivery=\\\"progressive\\\" width=\\\"320\\\" height=\\\"480\\\" type=\\\"video/mp4\\\"><![CDATA[https://cdn.cmcd1.com/videos/80000614664/80000614664-1-0_320x480.mp4]]></MediaFile></MediaFiles></Linear></Creative></Creatives></InLine></Ad></VAST>\",\"adomain\":[\"www.chalkdigital.com\"],\"attr\":[],\"bundle\":284035177,\"cat\":[\"IAB3-0\"],\"cid\":80000614664,\"crid\":4876793,\"ext\":{\"base_sdk_event_url\":\"https://n2.cmcd1.com/act?a=1&bid=.0090&wpalgo=0&pid=chalkboard&lid=2-80000614664-681879-42486243&piggyback=cSznTn5CNfR9NJlzRrTwBdsdfIUgThjUOS2Xtai6uTqgbvZ2KhEEiqCJJXOBFsmLvgqfZ5iLkWFNmqCNa5L0OcdxOg2WrBYgqLVjUHuXqjrgRChjz2fQtNjYWeGCJHMGTJSNalea62eYz2Ddfd9GOTc3jBd5l2yVAUjEzkfRSdIWfdyvzutRLLvWkYjFEeL3IhTDg3Ff93C9n7i4biG69ETQJ5TVYc4MiXXOGBQc5uWtfadeMSaMiquTumqP0ZxX2xNNbAqUtUw3zib9CNOJhYiIdRUMs96DrbxsqpqNc9DKI9gMmxw0QkNJaLFH4D9FoN8ZVwI2h95zWiby2jUGZNUEZCELuzefXhy3Yu7eCmBOezIBC4bSI47UdxlJ2o72SNxqtdb6t2gKNJmwxGGo4i1Tga8LKthay3fcJ006dmUrAsoEJsA7V8gxNeicZ6Keg5BRXEQsqgPCpSPvZyEQztfsOLWrKbXrb9X6lnS6h5dxylRJ9a8FTEg6ERoC0LfrWWi0goOVsz0pZo3sBT4OuPFGrV8vU3F5KmnDLOxp9x08gsmuSPAB2xhGkYKHImEW1hgHRYU79yjE24MmfwOoOvrMp7fLkTureKQE8hSawMZ2Lv08l22dOSTn9t6cZaSrDfwEn6Flzk3Z6d1pW1ZjGiVPvsmzSwK9vkr96pdcAtONjp0uls5b0jxghstw30eWvygXjn3KWKPd6vqyiYfYwvw6V7iVUxCdiyoeXKSDnV3IWLD35Wq2IOB5boO054sJUyArKgy&aid=8BA816E5-2E2C-470D-983D-650F67A160E0&event=EVENTID&sdk=SDKID\",\"mediationpriority\":[{\"eventdata\":{\"Android_Banner\":{\"class\":\"com.chalkdigital.mediation.ssbwrapper.SSBEventBanner\",\"placementId\":\"1526937450740\",\"publisherId\":\"c685b6f72d0b42e086a276d4b4d6b424\"},\"Android_Interstitial\":{\"class\":\"com.chalkdigital.mediation.ssbwrapper.SSBEventInterstitial\",\"host\":\"https://gw.pinable.jp\",\"key\":\"midnightrambler\",\"secretKey\":\"drawlswiftbayonet\"},\"iOS_Banner\":{\"class\":\"CDSSBBannerEvent\",\"host\":\"https://gw.pinable.jp\",\"key\":\"midnightrambler\",\"secretKey\":\"drawlswiftbayonet\"},\"iOS_Interstitial\":{\"class\":\"CDSSBInterstitialEvent\",\"host\":\"https://gw.pinable.jp\",\"key\":\"midnightrambler\",\"secretKey\":\"drawlswiftbayonet\"}},\"global\":{\"adapter\":\"\"},\"id\":2,\"timeout\":30},{\"eventdata\":{},\"global\":{\"adapter\":\"\"},\"id\":1,\"timeout\":30}]},\"h\":480,\"id\":\"53b1d6b2-269f-46bb-a4b4-f59a49a2b100\",\"impid\":1,\"iurl\":\"https://cdn.cmcd1.com/videos/80000614664/80000614664-1-0_320x480.mp4\",\"nurl\":\"https://n2.cmcd1.com/act?a=4&bid=.0090&wpalgo=0&pid=chalkboard&lid=2-80000614664-681879-42486243&piggyback=cSznTn5CNfR9NJlzRrTwBdsdfIUgThjUOS2Xtai6uTqgbvZ2KhEEiqCJJXOBFsmLvgqfZ5iLkWFNmqCNa5L0OcdxOg2WrBYgqLVjUHuXqjrgRChjz2fQtNjYWeGCJHMGTJSNalea62eYz2Ddfd9GOTc3jBd5l2yVAUjEzkfRSdIWfdyvzutRLLvWkYjFEeL3IhTDg3Ff93C9n7i4biG69ETQJ5TVYc4MiXXOGBQc5uWtfadeMSaMiquTumqP0ZxX2xNNbAqUtUw3zib9CNOJhYiIdRUMs96DrbxsqpqNc9DKI9gMmxw0QkNJaLFH4D9FoN8ZVwI2h95zWiby2jUGZNUEZCELuzefXhy3Yu7eCmBOezIBC4bSI47UdxlJ2o72SNxqtdb6t2gKNJmwxGGo4i1Tga8LKthay3fcJ006dmUrAsoEJsA7V8gxNeicZ6Keg5BRXEQsqgPCpSPvZyEQztfsOLWrKbXrb9X6lnS6h5dxylRJ9a8FTEg6ERoC0LfrWWi0goOVsz0pZo3sBT4OuPFGrV8vU3F5KmnDLOxp9x08gsmuSPAB2xhGkYKHImEW1hgHRYU79yjE24MmfwOoOvrMp7fLkTureKQE8hSawMZ2Lv08l22dOSTn9t6cZaSrDfwEn6Flzk3Z6d1pW1ZjGiVPvsmzSwK9vkr96pdcAtONjp0uls5b0jxghstw30eWvygXjn3KWKPd6vqyiYfYwvw6V7iVUxCdiyoeXKSDnV3IWLD35Wq2IOB5boO054sJUyArKgy&aid=8BA816E5-2E2C-470D-983D-650F67A160E0\",\"price\":9,\"w\":320}],\"seat\":2}]}", NetworkResponse.class);
                    networkResponse.parseResponse();
                    AdResponse adResponse = mActiveRequest.parseNetworkResponse(networkResponse);
                    onAdLoadSuccess(adResponse);
                } catch (Throwable t) {
                    Utils.logStackTrace(t);
                    CDAdLog.d(t.toString());
                    onAdLoadError(new CDAdNetworkError(response, t, CDAdNetworkError.Reason.BAD_BODY));
                }
            }

            @Override
            public void onAdRequestFailure(CDAdNetworkError error, Object object, int apitype) {
                onAdLoadError(error);
//                onAdRequestSuccess(null, object, apitype);
//                AdResponse adResponse = mActiveRequest.parseNetworkResponse(new Gson().fromJson("{\"cur\":\"USD\",\"id\":\"299453af-29bc-44a6-807d-8b586fcc7f82\",\"seatbid\":[{\"seat\":\"2\",\"bid\":[{\"crid\":\"3913\",\"h\":250,\"adm\":\"<script src=\\\"http:\\/\\/bs.serving-sys.com\\/BurstingPipe\\/adServer.bs?cn=rsb&c=28&pli=23634867&PluID=0&w=300&h=250&ord=1519892359421&ucm=true&mb=1\\\"><\\/script><noscript><a href=\\\"http:\\/\\/ec2-54-166-238-105.compute-1.amazonaws.com\\/xp\\/evt?pp=zkGviXgrlGrkYXYbRIgod5R8wy4JcEU4KE8tzSwabJR4rJWPI7cKy4GJss3exPYTRtErw5o2rm8iI35eFQgMnrqMWxCpF2kkadiMrblMjtnzfTtNJC8eNVrPXGneeh5iaRNn1My8aHqCiibjBboRRk5ATf4MkELpKN8cDxIEvDTNT9U3P8w2PI15TrNnwPHwxJHF60G79Fm2YN9Qznj7ZDZs7n1QGdIZMvw3wLPOa4iY2mjatHmtDeae7wbjmh0Ip6JrBvQJFUwqdUNp9r7TkPkPFrVvWQrVgNVTHQdYDpN1LmDcG0crgwPhypB6GQpEsbjaj5Hzwdfv1eHvQzHrZVw3nPR6vXhrohrxTQh7ouuT6E9kge98G6PXh4wrZYaWSGxtpNidOLwR6AujLZX3QSpdTdBWJAzxZaV6lRYHDlvd4S5lwW7ZcmVX9Fufmuvh4qi8iZSXzy2EONfagbORn0n3C6v3yNYTddhtLBJvPj3V4z4KELSWiBxU0OH3Z47lmaWII4OqnWG2uGVvLptbXMq06QrZvT4LLgUWu7RlY0VVfPlVBgYxrcdSmoNNhOrmx5O8ROoqit7f820Jg4z2nBVdewYC59Lkitbm0YFtzZZCqQz8ZUEAyrnuIGfJwm6xMIqoQirh4YEekd8tANHdB41NS4OFM4222GPynGHy0gM7xazEGTrdMLh0YE7BwxCYctbvONEZhjNFv2HPPFEQQsnajVrWFw73Fly6k0ovQFD5AlaLrGxAxt7lUWIrofEhussVV4v3ot1jKghPxTLH0Xraz5Ljjc13JU3UqKupOEzPYqUwBFgebkQzdZZGSOrIuMa1J__rd=HS5qP2fQCbbrUcdl9ZciP8CFpXX2U1pTd8VdpjnX9qcpKBWUESvNCCsd7vERQIyoJPxNhLhCu0XkVePhJzcusVPMXiocuHvtitLUEeJwNGpgQ42WiphSEeBJiw9mK1VM5E2lpFwG4LaTOQS4fwHkKiC7RMczxmllhqHtMRAWCvxMg3ZthcOfMWimM2bWLKlr__\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/bs.serving-sys.com\\/BurstingPipe\\/adServer.bs?cn=bsr&FlightID=23634867&Page=&PluID=0&Pos=1929237889&mb=1\\\" border=0 width=300 height=250><\\/a><\\/noscript><img src=\\\"http:\\/\\/ec2-54-166-238-105.compute-1.amazonaws.com\\/xp\\/evt?pp=4rirIQILvKyFGxfokEfAmHqvlnSMq0oKRPIcQtvpfTnTyADNQSDdypFqxGLbh6sDJUfYlyE41oVChqeBsURC45x5NI4aJVqtqjP4xWRgO8tVc7W0TBEf6MLp5sNxrTyspplI7g75aqhk4EaCNRsMbCCtq2cI53lNZ9veNEeSQFpiYyQpCx8yyz7vHz0T4QNlgZG2M7Gsp93hcRl38DPDxDGJshTyQ9AN4844pRKyIaslnZBTstuiWdNUTNqU6a0l0kORfYt5NwTNFKyN6SqDVeVehdznf2elxA1qSuVWOU3tlbjceYIiS3jMzDx35VamsdCRqlaHyNkjwoufGqZ6etucopB4bjskLhkBH3zqdfaSWeNsZeNzr3FMAk5gisitUaUFDNdjZ5j4YbBHgpI2HN033YudHnMplFHjDENDVTs0INaBWY1wxCQDBXgAPufUPFtr7uIs16S1nlgyHl8piqXrDaTb1ptcsnkRIX1VoUM8OWseAc6hOjg2lXsQulZ32bIMbrkO9CxP18vrzaMTObCheAtTBjfLqT10E8nqeAVcPzieABFffrqKJ38t3eVT7Z7TGbjHQrCnozrh4HQbY0cHZOu8mi5SQOdhqKsI92ag7Rz7zUazX7PWKWQgIcYhivag97FR7fQIYFNy0h1nKNuRFJVdMB9a2BvIa7qrmo8WVFVTZswJIWVyx9NSlUpMTkwlEAApjYsNzQAelHgByJFglV9FM8s2MFzNTwSkoTt9P27zLWndCKdjlJ3jnr3J8xCRAN6DVAsDPF6jOf2uNP2fYo3wNkAdTcfMxxYq1xkksuWCvTVjLBJdF7axeumX4FF2KK8YnEw5oMW5VjJ7uaO7X6G__\\\" width=\\\"1\\\" height=\\\"1\\\" \\/>\",\"adid\":\"1222_3913\",\"adomain\":[\"redfin.com\"],\"price\":0.1,\"w\":300,\"iurl\":\"https:\\/\\/daf37cpxaja7f.cloudfront.net\\/c1222\\/creative_url_15181802673800_aaa.png\",\"cat\":[\"IAB10\"],\"id\":\"cefd1a0b-3413-4c9d-8a6f-9ddebefae62a_1\",\"attr\":[],\"impid\":\"1\",\"cid\":\"1222\"}]}],\"bidid\":\"cefd1a0b-3413-4c9d-8a6f-9ddebefae62a\"}", NetworkResponse.class));
//                onAdLoadSuccess(adResponse);
            }
        };

        mRefreshRunnable = new Runnable() {
            public void run() {
                if (!mIsPaused)
                    internalLoadRequest();
            }
        };
        mRefreshTime = mCDAdView.getRefreshInterval();
        mRefreshTime = Math.max(MIN_REFRESH_TIME, mRefreshTime);
        mHandler = new Handler();
    }

    @VisibleForTesting
    void onAdLoadSuccess(@NonNull final AdResponse adResponse) {
        mAdResponse = adResponse;
        mCustomEventClassName = adResponse.getCustomEventClassName();
        // Do other ad loading setup. See AdFetcher & AdLoadTask.
        mTimeoutMilliseconds = mAdResponse.getAdTimeoutMillis() == null
                ? mTimeoutMilliseconds
                : mAdResponse.getAdTimeoutMillis();
//        mRefreshTime = mAdResponse.getRefreshTime();
        setNotLoading();
        mAtLeastOneAdLoaded = true;
        loadCustomEvent(mCDAdView, adResponse.getCustomEventClassName(),
        adResponse.getServerExtras(), adResponse.getEvents());
        setLastAdRequestTime();
        scheduleRefreshTimerIfEnabled();
    }

    @VisibleForTesting
    void onAdLoadError(final CDAdNetworkError cdAdNetworkError) {
        CDAdErrorCode errorCode = getErrorCodeFromNetworkError(cdAdNetworkError, mContext);
        onAdLoadError(errorCode);

    }

    void onAdLoadError(CDAdErrorCode errorCode){
        setNotLoading();
        adDidFail(errorCode);
    }

    @VisibleForTesting
    void loadCustomEvent(@Nullable final CDAdView cdAdView,
            @Nullable final String customEventClassName,
            @NonNull final Map<String, String> serverExtras, @NonNull final Event[] events) {
        Preconditions.checkNotNull(serverExtras);

        if (cdAdView == null) {
            CDAdLog.d("Can't load an ad in this ad view because it was destroyed.");
            return;
        }

        mCdMediationAdRequest = new CDMediationAdRequest() {
            @Override
            public String getAge() {
                if (mCDAdRequest!=null)
                    return mCDAdRequest.targetingAge;
                return "";
            }

            @Override
            public String getGender() {
                if (mCDAdRequest!=null)
                    return mCDAdRequest.targetingGender;
                return "";
            }

            @Override
            public String getEducation() {
                if (mCDAdRequest!=null)
                    return mCDAdRequest.targetingEducation;
                return "";
            }

            @Override
            public String getLanguage() {
                if (mCDAdRequest!=null)
                    return mCDAdRequest.targetingLanguage;
                return "";
            }

            @Override
            public int income() {
                if (mCDAdRequest!=null)
                    return mCDAdRequest.targetingIncome;
                return 0;
            }

            @Override
            public String getKeywords() {
                if (mCDAdRequest!=null)
                    return mCDAdRequest.keyword;
                return null;
            }

            @Override
            public Location getLocation() {
                Location lastLocation = CDAdLocationManager.getLastLocation(mContext);
                if (lastLocation != null)
                    return lastLocation;
                else return LocationService.getLastKnownLocation(mContext);
            }


            @Override
            public boolean isTesting() {
                if (mCDAdRequest!=null)
                    return mCDAdRequest.testing;
                return false;
            }

        };


        cdAdView.loadCustomEvent(customEventClassName, serverExtras, mCdMediationAdRequest, events);
    }

    @VisibleForTesting
    @NonNull
    static CDAdErrorCode getErrorCodeFromNetworkError(@NonNull final CDAdNetworkError error,
            @Nullable final Context context) {
        final Response networkResponse = error.networkResponse;

        // For CDAdNetworkErrors, networkResponse is null.
        if (networkResponse!=null) {
            switch (error.getReason()) {
                case WARMING_UP:
                    return CDAdErrorCode.WARMUP;
                case NO_FILL:
                    return CDAdErrorCode.NO_FILL;
                default:
                    return CDAdErrorCode.UNSPECIFIED;
            }
        }

        if (networkResponse == null) {
            if (!DeviceUtils.isNetworkAvailable(context)) {
                return CDAdErrorCode.NO_CONNECTION;
            }
            return CDAdErrorCode.UNSPECIFIED;
        }

        if (error.networkResponse.code() >= 400) {
            return CDAdErrorCode.SERVER_ERROR;
        }

        return CDAdErrorCode.UNSPECIFIED;
    }

    @Nullable
    public CDAdView getCDAdView() {
        return mCDAdView;
    }

    public void loadRequest(@NonNull CDAdRequest cdAdRequest){
        mLastAdRequestTime = 0;
        mCDAdRequest = cdAdRequest;
        mAtLeastOneAdLoaded = false;
        internalLoadRequest();
    }

    private void internalLoadRequest() {

        if (mCDAdBroadcastReceiver == null)
            mCDAdBroadcastReceiver = new CDAdBroadcastReceiver();

        if (!mCDAdBroadcastReceiver.isBroadcastReceiverRegistered){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CDAdActions.LOCATION_CHANGED);
            intentFilter.addAction(CDAdActions.LOCATION_ERROR);
            intentFilter.addAction(CDAdActions.IP_RECEIVED);
            intentFilter.addAction(CDAdActions.IP_ERROR);
            intentFilter.addAction(CDAdActions.CDAdNotifyNetworkReachabilityChanged);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mCDAdBroadcastReceiver, intentFilter);
            mCDAdBroadcastReceiver.isBroadcastReceiverRegistered = true;
        }

        mAdWasLoaded = true;
//        mIsPaused = false;
        if (mCDAdRequest == null || isDestroyed())
            return;

        if (System.currentTimeMillis() - mLastAdRequestTime <30000) {
            return;
        }

        if (!isNetworkAvailable()) {
            CDAdLog.d("Can't load an ad because there is no network connectivity. Waiting for network conection");
            onAdLoadError(CDAdErrorCode.NO_CONNECTION);
            return;
        }

        if (mCDAdView.isLocationAutoUpdateEnabled() && !shouldSkipLocation && CDAdLocationManager.shouldWaitToProceedForAdRequest(mContext.getApplicationContext())){
            CDAdLog.d("Can't load an ad because location is not available. Waiting for location");
            isWaitingForLocationUpdate = true;

            initializeLocationHandler(mContext);
            return;
        }


        if (CDAdsUtils.isIsGeoIpLocationEnabled() && CDAdIPGeolocationManager.shouldWaitForIP(mContext.getApplicationContext()) && !shouldSkipIp){
            isWaitingForIP = true;
            return;
        }
        shouldSkipIp = false;
        shouldSkipLocation = false;

        if (mCDAdBroadcastReceiver!=null && mCDAdBroadcastReceiver.isBroadcastReceiverRegistered){
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mCDAdBroadcastReceiver);
            mCDAdBroadcastReceiver.isBroadcastReceiverRegistered = false;
        }

//        String adUrl = generateAdUrl();
        loadNonJavascript();
    }

    void loadNonJavascript() {
//        if (url == null) {
//            return;
//        }
//
//        if (!url.startsWith("javascript:")) {
//            CDAdLog.d("Loading url: " + url);
//        }
//
        if (mIsLoading) {
            CDAdLog.i("Already loading an ad, wait to finish.");
            return;
        }
//
//        mUrl = url;
        mIsLoading = true;

        fetchAd();
    }

    public void reload() {
        CDAdLog.d("Reload ad: " + mUrl);
        loadNonJavascript();
    }

    /**
     * Returns true if continuing to load the failover url, false if the ad actually did not fill.
     */
    boolean loadFailUrl(CDAdErrorCode errorCode) {
        mIsLoading = false;

        Log.v("CDAd", "CDAdErrorCode: " + (errorCode == null ? "" : errorCode.toString()));

        final String failUrl = mAdResponse == null ? "" : mAdResponse.getFailoverUrl();
        if (!TextUtils.isEmpty(failUrl)) {
            CDAdLog.d("Loading failover url: " + failUrl);
            loadNonJavascript();
            return true;
        } else {
            // No other URLs to try, so signal a failure.
            adDidFail(CDAdErrorCode.NO_FILL);
            return false;
        }
    }

    void setNotLoading() {
        this.mIsLoading = false;
        if (mActiveRequest != null) {
            if (!mActiveRequest.isCanceled()) {
                mActiveRequest.cancel();
            }
            mActiveRequest = null;
        }

    }


    @Nullable
    public String getCustomEventClassName() {
        return mCustomEventClassName;
    }

    public long getBroadcastIdentifier() {
        return mBroadcastIdentifier;
    }


    void pauseRefresh() {
        CDAdLog.d("pauseRefresh");
        setAutoRefreshStatus(false);
    }

    void resumeRefresh() {
        CDAdLog.d("resumeRefresh");
        if (mShouldAllowAutoRefresh) {
            setAutoRefreshStatus(mShouldAllowAutoRefresh);
        }
    }

    public boolean isAutoRefreshEnabled() {
        return mShouldAllowAutoRefresh;
    }

    void setAdAutoRefreshEnabled(final boolean adAutoRefreshEnabled) {
        mShouldAllowAutoRefresh = adAutoRefreshEnabled;
        setAutoRefreshStatus(adAutoRefreshEnabled);
    }

    private void setAutoRefreshStatus(final boolean newAutoRefreshStatus) {
        final boolean autoRefreshStatusChanged = mAdWasLoaded &&
                (mAutoRefreshStatus != newAutoRefreshStatus);
        if (autoRefreshStatusChanged) {
            mIsPaused = !newAutoRefreshStatus;
            final String enabledString = (newAutoRefreshStatus) ? "enabled" : "disabled";
            CDAdLog.d("Refresh " + enabledString);
            if (mAdWasLoaded && newAutoRefreshStatus) {
                scheduleRefreshTimerIfEnabled();
            } else if (!newAutoRefreshStatus) {
                cancelRefreshTimer();
            }
        }
        mAutoRefreshStatus = newAutoRefreshStatus;
    }

    @Nullable
    public AdReport getAdReport() {
        if (mAdResponse != null) {
            return new AdReport(ClientMetadata.getInstance(mContext), mAdResponse);
        }
        return null;
    }

    public Event[] getEvents(){
        return mAdResponse.getEvents();
    }

    boolean isDestroyed() {
        return mIsDestroyed;
    }

    /*
     * Clean up the internal state of the AdViewController.
     */
    void cleanup() {
        if (mIsDestroyed) {
            return;
        }

        if (mCDAdBroadcastReceiver!=null && mCDAdBroadcastReceiver.isBroadcastReceiverRegistered){
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mCDAdBroadcastReceiver);
            mCDAdBroadcastReceiver.isBroadcastReceiverRegistered = false;
        }

        if (mActiveRequest != null) {
            mActiveRequest.cancel();
            mActiveRequest = null;
        }

        setAutoRefreshStatus(false);
        cancelRefreshTimer();

        // WebView subclasses are not garbage-collected in a timely fashion on Froyo and below,
        // thanks to some persistent references in WebViewCore. We manually release some resources
        // to compensate for this "leak".
        mCDAdView = null;
        mContext = null;
        mUrlGenerator = null;

        // Flag as destroyed. LoadUrlTask checks this before proceeding in its onPostExecute().
        mIsDestroyed = true;
    }

    Integer getAdTimeoutDelay() {
        return mTimeoutMilliseconds;
    }

    public void trackImpression() {
        if (mAdResponse != null) {
//            TrackingRequest.makeTrackingHttpRequest(mAdResponse.getImpressionTrackingUrl(),
//                    mContext, BaseEvent.Name.IMPRESSION_REQUEST);
        }
    }

    public void registerClick() {
        if (mAdResponse != null) {
            // Click tracker fired from Banners and Interstitials
//            TrackingRequest.makeTrackingHttpRequest(mAdResponse.getClickTrackingUrl(),
//                    mContext, BaseEvent.Name.CLICK_REQUEST);
        }
    }

    void fetchAd() {
        CDAdView cdAdView = getCDAdView();
        if (!cdAdView.canReload()){
            return;
        }
        if (cdAdView == null || mContext == null) {
            CDAdLog.d("Can't load an ad in this ad view because it was destroyed.");
            setNotLoading();
            return;
        }
        cancelRefreshTimer();
        setLastAdRequestTime();
        mActiveRequest = new AdRequest(mCDAdView.getCDADType(), new WebAdRequestBodyGenerator(mContext, false).bodyWithParams(mCDAdView.getParams(mCDAdRequest), mCDAdRequest.testing, mContext), mContext, mAdListener, null, 0, "adRequest", NetworkResponse.class);
        mActiveRequest.execute();
    }

    private void setLastAdRequestTime(){
        mLastAdRequestTime = System.currentTimeMillis();
    }

    void forceRefresh() {
        setNotLoading();
        loadRequest(mCDAdRequest);
    }


    void adDidFail(CDAdErrorCode errorCode) {
        CDAdLog.i("Ad failed to load.");
        setNotLoading();

        CDAdView cdAdView = getCDAdView();
        if (cdAdView == null) {
            return;
        }
        setLastAdRequestTime();
        scheduleRefreshTimerIfEnabled();
        cdAdView.adFailed(errorCode);
    }

    void scheduleRefreshTimerIfEnabled() {
        cancelRefreshTimer();
        if ((!mAtLeastOneAdLoaded || mShouldAllowAutoRefresh) && mRefreshTime != null && mRefreshTime > 0) {
            int nextAdInterval = (int) ((System.currentTimeMillis() - mLastAdRequestTime)/1000);
            if (nextAdInterval > mRefreshTime )
                nextAdInterval = 0;
            else if (nextAdInterval < mRefreshTime){
                nextAdInterval = mRefreshTime - nextAdInterval;
            }
            mHandler.postDelayed(mRefreshRunnable,
                    Math.min(MAX_REFRESH_TIME,
                            nextAdInterval)*1000);
        }
    }

    private void cancelRefreshTimer() {
        mHandler.removeCallbacks(mRefreshRunnable);
    }

    private boolean isNetworkAvailable() {
        if (mContext == null) {
            return false;
        }
        // If we don't have network state access, just assume the network is up.
        if (!DeviceUtils.isPermissionGranted(mContext, ACCESS_NETWORK_STATE)) {
            return true;
        }

        // Otherwise, perform the connectivity check.
        ConnectivityManager cm
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    void setAdContentView(final View view) {
        // XXX: This method is called from the WebViewClient's callbacks, which has caused an error on a small portion of devices
        // We suspect that the code below may somehow be running on the wrong UI Thread in the rare case.
        // see: https://stackoverflow.com/questions/10426120/android-got-calledfromwrongthreadexception-in-onpostexecute-how-could-it-be
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                CDAdView cdAdView = getCDAdView();
                if (cdAdView == null) {
                    return;
                }
                cdAdView.removeAllViews();
                View layout = view;
                if (mAdResponse.isShowBorder()){
                    BorderLayout borderLayout = new BorderLayout(mContext);
                    borderLayout.setContentView(view);
                    borderLayout.setBorderWidth(mAdResponse.getBorderWidth());
                    try {
                        borderLayout.setBorderColor(Color.parseColor("#"+mAdResponse.getBorderColor()));
                    } catch (Exception e) {
                        Utils.logStackTrace(e);
                        borderLayout.setBorderColor(Color.BLACK);
                    }
                    layout = borderLayout;
                }
                if (mAdResponse.isCloseable()){
                    CloseableLayout closeableLayout = new CloseableLayout(mContext);
                    closeableLayout.setClosePosition(CloseableLayout.ClosePosition.TOP_RIGHT);
                    closeableLayout.setOnCloseListener(new CloseableLayout.OnCloseListener() {
                        @Override
                        public void onClose() {
                            mCDAdView.setVisibility(View.INVISIBLE);
                        }
                    });
                    closeableLayout.addView(layout, WRAP_AND_BOTTOM_CENTER_LAYOUT_PARAMS);
                    layout = closeableLayout;
                    ViewGroup.LayoutParams layoutParams = cdAdView.getLayoutParams();
                    layoutParams.height = WRAP_AND_BOTTOM_CENTER_LAYOUT_PARAMS.height;
                    layoutParams.width = WRAP_AND_BOTTOM_CENTER_LAYOUT_PARAMS.width;
                    cdAdView.setLayoutParams(layoutParams);
                }

                cdAdView.addView(layout, getAdLayoutParams(view, mAdResponse.isCloseable()));
            }
        });
    }

    private FrameLayout.LayoutParams getAdLayoutParams(View view, boolean isCloseable) {
        Integer width = null;
        Integer height = null;
        if (mAdResponse != null) {
            int margin = isCloseable?25:0;
            if (mAdResponse.getWidth()!=null){
                width = mAdResponse.getWidth()+margin*2;
                height = mAdResponse.getHeight()+margin;
            }else{
                CDAdSize cdAdSize = CDAdSize.getSizeFromCDSizeConstant(mCDAdView.getCDAdSize());
                width = cdAdSize.getWidth()+margin*2;
                height = cdAdSize.getHeight()+margin;
            }
            WRAP_AND_BOTTOM_CENTER_LAYOUT_PARAMS.setMargins(Dips.asIntPixels(margin, mContext) ,0,Dips.asIntPixels(margin, mContext),0);
        }

        if (width != null && height != null && getShouldHonorServerDimensions(view) && width > 0 && height > 0) {
            int scaledWidth = Dips.asIntPixels(width, mContext);
            int scaledHeight = Dips.asIntPixels(height, mContext);

            return new FrameLayout.LayoutParams(scaledWidth, scaledHeight, Gravity.CENTER);
        } else {
            return WRAP_AND_CENTER_LAYOUT_PARAMS;
        }
    }

    @Deprecated // for testing
    @VisibleForTesting
    Integer getRefreshTime() {
        return mRefreshTime;
    }

    @Deprecated // for testing
    @VisibleForTesting
    void setRefreshTime(@Nullable final Integer refreshTime) {
        mRefreshTime = refreshTime;
    }

    private class CDAdBroadcastReceiver extends BroadcastReceiver {
        boolean isBroadcastReceiverRegistered;
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case CDAdActions.LOCATION_CHANGED:
                    if (isWaitingForLocationUpdate){
                        synchronized (AdViewController.this){
                            isWaitingForLocationUpdate = false;
                            shouldSkipLocation = true;
                            reloadAdIfStillWaiting();
                            clearLocationHandler();
                        }
                    }
                    break;
                case CDAdActions.LOCATION_ERROR:
                    if (isWaitingForLocationUpdate) {
                        synchronized (AdViewController.this) {
                            isWaitingForLocationUpdate = false;
                            shouldSkipLocation = true;
                            reloadAdIfStillWaiting();
                            clearLocationHandler();
                        }
                    }
                    break;
                case CDAdActions.CDAdNotifyNetworkReachabilityChanged:
                    if (intent.getBooleanExtra(CDAdConnectivityChangeReceiver.IS_NETWORK_CONNECTED, false)) {
                        synchronized (AdViewController.this){
                            reloadAdIfStillWaiting();
                        }
                    }
//                    else if(mShouldAllowAutoRefresh){
//                        CDAdLog.i("CDAdView", "Ad Request stopped, Network not reachable");
//                        cancelRefreshTimer();
//                    }
                    break;
                case CDAdActions.IP_RECEIVED:
                    synchronized (AdViewController.this){
                        isWaitingForIP = false;
                        reloadAdIfStillWaiting();
                    }
                    break;
                case CDAdActions.IP_ERROR:
                    synchronized (AdViewController.this){
                        isWaitingForIP = false;
                        shouldSkipIp = true;
                        reloadAdIfStillWaiting();
                    }
                    break;

            }
        }
    }

    private void reloadAdIfStillWaiting(){
        if (!mIsPaused && mAdWasLoaded){
            if (!mAtLeastOneAdLoaded || mShouldAllowAutoRefresh)
                scheduleRefreshTimerIfEnabled();
        }
    }

    private synchronized void initializeLocationHandler(final Context context){
        CDAdLog.d(TAG, "initializeLocationHandler");
        if (mLocationHandler == null){
            CDAdLog.d(TAG, "initializedLocationHandler");
            mLocationHandler = new Handler();
            mLocationRunnable = new Runnable() {
                @Override
                public void run() {
                    CDAdLog.d(TAG, "locationHandlerTimeout");
                    onLocationServiceTimeout(context);
//                stopUpdatingLocation(true);
                }
            };
        }
        mLocationHandler.postDelayed(mLocationRunnable, CDAdConstants.CDAdLocationServiceTimeoutInterval*1000);
    }

    private synchronized void clearLocationHandler(){
        CDAdLog.d(TAG, "clearLocationHandler");
        if (mLocationHandler !=null){
            CDAdLog.d(TAG, "clearedLocationHandler");
            if (mLocationRunnable !=null) {
                mLocationHandler.removeCallbacks(mLocationRunnable);
                mLocationRunnable = null;
            }
            mLocationHandler = null;
        }
    }

    private void onLocationServiceTimeout(Context context){
        isWaitingForLocationUpdate = false;
        shouldSkipLocation = true;
        reloadAdIfStillWaiting();
    }
}
