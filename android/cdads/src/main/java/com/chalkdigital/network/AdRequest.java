package com.chalkdigital.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import androidx.preference.PreferenceManager;

import com.chalkdigital.ads.WebAdRequestBodyGenerator;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.network.response.AdResponse;
import com.chalkdigital.network.response.NetworkResponse;
import com.chalkdigital.network.retrofit.CDAdCallback;
import com.chalkdigital.network.retrofit.CDAdRetrofit;
import com.chalkdigital.network.retrofit.CDAdService;

import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import retrofit2.Call;
import retrofit2.Response;

public class AdRequest<T> extends CDAdCallback<T> {

    @NonNull private final Listener<T> mListener;
    @NonNull private final AdResponse.CDAdType mCDAdType;
    @NonNull private final Context mContext;
    @NonNull private Map<String, Object> mParams;
    @NonNull private Call<T> mCall;
    private final String mMethodName;
    private final Class<T> mReturnObjectType;
    private static boolean isEmulator;

    static {
        isEmulator = DeviceUtils.isEmulator();
    }

    public AdRequest(
            @NonNull final AdResponse.CDAdType cdAdType,
            @Nullable final Map<String, Object > params,
            @NonNull Context context,
            @NonNull final Listener listener, Object refObject, int apiTYpe, String declaredMethodName, final Class<T> returnObjectType) {
        super(refObject, apiTYpe);
        Preconditions.checkNotNull(cdAdType);
        Preconditions.checkNotNull(listener);
        mParams = params;
        mListener = listener;
        mCDAdType = cdAdType;
        mContext = context.getApplicationContext();
        mMethodName = declaredMethodName;
        mReturnObjectType = returnObjectType;
    }

    @NonNull
    public Listener getListener() {
        return mListener;
    }

    public boolean isCanceled(){
        if (mCall!=null)
            return mCall.isCanceled();
        return false;
    }

    public boolean isExecuted(){
        if (mCall!=null)
            return mCall.isExecuted();
        return false;
    }

    public void cancel(){
        if (mCall !=null)
            mCall.cancel();
    }

    public void execute(){
        HashMap<String , String > query = new HashMap<>();
        if (isEmulator)
            query.put("emulator", "1");
        String requestUrl = null;
        String path = null;
        String serverUrl = null;

        try {
            serverUrl = mContext.getResources().getText(mContext.getResources().getIdentifier("SERVER_URL", "string", mContext.getPackageName())).toString();
        } catch (Resources.NotFoundException e) {

        }
        try {
            path = mContext.getResources().getText(mContext.getResources().getIdentifier("SERVER_PATH", "string", mContext.getPackageName())).toString();
        } catch (Resources.NotFoundException e) {

        }

        if (serverUrl == null){
            CDAdLog.d("SERVER_URL not defined in strings.xml, Stopping Viewablity Event");
            return;
        }

        if (path == null){
            CDAdLog.d("SERVER_PATH not defined in strings.xml, Stopping Viewablity Event");
        }
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("cdSettings", Context.MODE_PRIVATE);
        try{
            String testUrl = sharedPreferences.getString("testUrl", "");
            if (testUrl!=null && testUrl.length()>0 && URLUtil.isValidUrl(testUrl)){
                testUrl = URLDecoder.decode(testUrl, "UTF-8");
                URI uri = new URI(testUrl);
                requestUrl = uri.getHost();
                path = uri.getPath();
                String queryString = testUrl.split("\\?")[1];
                query.putAll(getQueryMap(queryString));
                query.put("sdk", "1");
            }
            else{
                try {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    String region = preferences.getString("chalk_sdk_region", "AUTO");
                    URI uri = new URI(serverUrl);
                    String subdomain = uri.getHost().split("\\.")[0];
                    String domain = uri.getHost().replace(subdomain, "");
                    CDAdLog.d("PreferenceManagerRegion : ", region);
                    if(region != null && region.equals("JPN")){
                        subdomain = subdomain+"-jp";
                    }
                    else if((region == null || region.equals("AUTO"))){
                        if(mParams.get(WebAdRequestBodyGenerator.GEO_KEY) !=null){
                            Map<String, String> geo = (Map<String, String>) mParams.get(WebAdRequestBodyGenerator.GEO_KEY);
                            if(geo != null && geo.get(DataKeys.COUNTRY) != null && geo.get(DataKeys.COUNTRY).equals("JPN")){
                                subdomain = subdomain+"-jp";
                            }
                        }
                    }
                    requestUrl = uri.getScheme()+"://"+subdomain+domain;
                    String partnerId = mContext.getResources().getText(mContext.getResources().getIdentifier("CDADS_PARTNER_ID", "string", mContext.getPackageName())).toString();
                    query.put(DataKeys.SSPID, partnerId);
                } catch (Exception e) {
                    CDAdLog.d(e.getMessage());
                }

            }

        }
        catch (Exception e) {
            Utils.logStackTrace(e);
            e.printStackTrace();
        }
        mCall = CDAdService.performCall(CDAdRetrofit.getsharedSDKRequestAPIInstance(requestUrl+"/"), mMethodName, mReturnObjectType, new Class[] {String.class, mParams.getClass(), query.getClass()}, new Object[] {path, mParams, query}, this);
    }

    public static Map<String, String> getQueryMap(String query)
    {
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    public AdResponse parseNetworkResponse(final NetworkResponse networkResponse) {
        // NOTE: We never get status codes outside of {[200, 299], 304}. Those errors are sent to the
        // error listener.
        return (new AdResponse.Builder()).setAdType(mCDAdType).setNetworkResponse(networkResponse, mContext).build();
    }


    @Override
    public void onNetworkResponse(Call<T> call, Response<T> response, Object object, int apitype) {
        if (mListener!=null) {
            CDAdLog.v(response.toString());
            if (isCallSuccess(response))
                mListener.onAdRequestSuccess(response, object, apitype);
            else {
                mListener.onAdRequestFailure(new CDAdNetworkError(response, CDAdNetworkError.Reason.UNSPECIFIED), object, apitype);
            }
        }


    }

    @Override
    public void onNetworkFailure(Call<T> call, Throwable t, Object object, int apitype) {
        if (mListener!=null)
            mListener.onAdRequestFailure(new CDAdNetworkError(t, CDAdNetworkError.Reason.UNSPECIFIED), object, apitype);

    }

    public interface Listener<T>{
        public void onAdRequestSuccess(Response<T> response, Object object, int apitype);
        public void onAdRequestFailure(CDAdNetworkError error, Object object, int apitype);

    }

}
