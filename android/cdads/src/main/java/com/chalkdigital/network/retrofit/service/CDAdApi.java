package com.chalkdigital.network.retrofit.service;


import com.chalkdigital.network.response.GetAdvertisementResponse;
import com.chalkdigital.network.response.GetBeaconOffers;
import com.chalkdigital.network.response.GetLocalesResponse;
import com.chalkdigital.network.response.GetUUidsResponse;
import com.chalkdigital.network.response.IPGeoLocationRequestResponse;
import com.chalkdigital.network.response.NetworkResponse;
import com.chalkdigital.network.response.ReverseGeocodeResponse;
import com.chalkdigital.network.response.SSBBaseResponse;
import com.chalkdigital.network.response.TrackingRequestResponse;
import com.chalkdigital.network.response.UpdateDeviceInfoResponse;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by arungupta on 20/06/16.
 */
public interface CDAdApi {

    @POST
    Call<NetworkResponse> adRequest(@Url String url, @Body HashMap<String, Object> options, @QueryMap HashMap<String, String > query);

    @GET("maps/api/geocode/json")
    Call<ReverseGeocodeResponse> reverseGeocode(@QueryMap HashMap<String, Object> body);

    @GET("geocode/ip2geo/v1")
    Call<IPGeoLocationRequestResponse> geolocateIp();

    @POST("track")
    Call<TrackingRequestResponse> tracking(@Body HashMap<String, Object> params);

    @POST("v1/ssbp/getUuids")
    Call<GetUUidsResponse> ssbFetchUuids(@Body HashMap<String, Object> params);

    @POST("v1/ssbp/getLocales")
    Call<GetLocalesResponse> ssbGetLocales(@Body HashMap<String, Object> params);

    @POST("v1/ssbp/getBeaconOffers")
    Call<GetBeaconOffers> ssbGetBeaconOffers(@Body HashMap<String, Object> params);

    @POST("v1/ssbp/getAdvertisement")
    Call<GetAdvertisementResponse> ssbGetAdvertisements(@Body HashMap<String, Object> params);

    @POST("v1/ssbp/updateDeviceInfo")
    Call<UpdateDeviceInfoResponse> ssbUpdateDeviceInfo(@Body HashMap<String, Object> params);

    @GET
    Call<Object> logEvent(@Url String requestUrl);

    @GET
    @Streaming
    Call<ResponseBody> fetchVpaidHTML(@Url String requestUrl);

    @POST("sdk-debug")
    @Headers({"Content-Type: application/json"})
    Call<Object> logException(@Body HashMap<String, Object> body);
}
