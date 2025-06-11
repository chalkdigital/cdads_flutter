package com.chalkdigital.network.response;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.chalkdigital.common.CDAdConstants;
import com.chalkdigital.common.CreativeOrientation;
import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.event.EventDetails;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.DateAndTime;
import com.chalkdigital.common.util.DeviceUtils;
import com.chalkdigital.common.util.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class AdResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private CDAdType mAdType;

    @Nullable
    private String mAdUnitId;

    @Nullable
    private String mFullAdType;
    @Nullable
    private String mNetworkType;

    @Nullable
    private String mRewardedVideoCurrencyName;
    @Nullable
    private String mRewardedVideoCurrencyAmount;
    @Nullable
    private String mRewardedCurrencies;
    @Nullable
    private String mRewardedVideoCompletionUrl;
    @Nullable
    private Integer mRewardedDuration;
    private boolean mShouldRewardOnClick;

    @Nullable
    private String mRedirectUrl;
    @Nullable
    private String mClickTrackingUrl;
    @Nullable
    private String mImpressionTrackingUrl;
    @Nullable
    private String mFailoverUrl;
    @Nullable
    private String mRequestId;

    @Nullable
    private Integer mWidth;
    @Nullable
    private Integer mHeight;
    @Nullable
    private Integer mAdTimeoutDelayMillis;
    @Nullable
    private Integer mRefreshTime;
    @Nullable
    private String mDspCreativeId;

    private boolean mScrollable;

    @Nullable
    private String mResponseBody;
    @Nullable
    private JSONObject mJsonBody;

    @Nullable
    private EventDetails mEventDetails;

    @Nullable
    private String mCustomEventClassName;
    @Nullable
    private Integer mBrowserAgent;

    private boolean closeable;
    private boolean takeScreenshot;
    private boolean skipable;
    private String skipOffset;
    private boolean showBorder;
    private float borderWidth;
    private String borderColor;
    private String vpaidSourceUrl;
    private int stretch;
    private Event[] mEvents;
    private int mOrientation;

    public void setResponseBody(@Nullable String responseBody) {
        mResponseBody = responseBody;
    }

    @NonNull
    private final Map<String, String> mServerExtras;

    private final long mTimestamp;

    private AdResponse(@NonNull Builder builder, Context context) {

        mServerExtras = new HashMap<String, String >();

        ConfigurationParameters globalConfigurationParameters = null;
        ConfigurationParameters bidConfigurationParameters = null;
        mAdType = builder.getAdType();
        if (builder.networkResponse == null || !builder.networkResponse.isNoFillCase) {

            if (builder.networkResponse != null && builder.networkResponse.getSeatbid() != null && builder.networkResponse.getSeatbid().size() > 0) {

                Bid bid = builder.networkResponse.getSeatbid().get(0).getBid().get(0);
                if (bid.getExt() != null) {
                    globalConfigurationParameters = bid.getExt().getGlobal();
                    bidConfigurationParameters = bid.getExt().getBid();
                }
                if (globalConfigurationParameters == null)
                    globalConfigurationParameters = new ConfigurationParameters();
                if (bidConfigurationParameters != null) {
                    if (bidConfigurationParameters.getBorder() != null)
                        globalConfigurationParameters.setBorder(bidConfigurationParameters.getBorder());
                    if (bidConfigurationParameters.getBorderWidth() != null)
                        globalConfigurationParameters.setBorderWidth(bidConfigurationParameters.getBorderWidth());
                    if (bidConfigurationParameters.getBorderColor() != null)
                        globalConfigurationParameters.setBorderColor(bidConfigurationParameters.getBorderColor());
                    if (bidConfigurationParameters.getClosable() != null)
                        globalConfigurationParameters.setClosable(bidConfigurationParameters.getClosable());
                    if (bidConfigurationParameters.getClickaction() != null)
                        globalConfigurationParameters.setClickaction(bidConfigurationParameters.getClickaction());
                    if (bidConfigurationParameters.getStretch() != null)
                        globalConfigurationParameters.setStretch(bidConfigurationParameters.getStretch());
                    if (bidConfigurationParameters.getEvents() != null)
                        globalConfigurationParameters.setMediationpriority(bidConfigurationParameters.getEvents());
                    if (bidConfigurationParameters.getBase_sdk_event_url() != null)
                        globalConfigurationParameters.setBase_sdk_event_url(bidConfigurationParameters.getBase_sdk_event_url());
                    if (bidConfigurationParameters.getSkipable() != null)
                        globalConfigurationParameters.setSkipable(bidConfigurationParameters.getSkipable());
                    if (bidConfigurationParameters.getSkipOffset() != null)
                        globalConfigurationParameters.setSkipOffset(bidConfigurationParameters.getSkipOffset());
                    if (bidConfigurationParameters.getvpaidSourceUrl() != null)
                        globalConfigurationParameters.setvpaidSourceUrl(bidConfigurationParameters.getvpaidSourceUrl());
                    if (bidConfigurationParameters.getOrientation() != null)
                        globalConfigurationParameters.setOrientation(bidConfigurationParameters.getOrientation());
                    if (bidConfigurationParameters.getAdPrefix() != null)
                        globalConfigurationParameters.setAdPrefix(bidConfigurationParameters.getAdPrefix());
                    if (bidConfigurationParameters.getAdSuffix() != null)
                        globalConfigurationParameters.setAdSuffix(bidConfigurationParameters.getAdSuffix());
                }
                if (globalConfigurationParameters.getBorderColor() == null)
                    globalConfigurationParameters.setBorderColor("000000");
                if (globalConfigurationParameters.getBorder() == null)
                    globalConfigurationParameters.setBorder(false);
                if (globalConfigurationParameters.getBorderWidth() == null)
                    globalConfigurationParameters.setBorderWidth(1.0f);
                if (globalConfigurationParameters.getClosable() == null)
                    globalConfigurationParameters.setClosable(false);
                if (globalConfigurationParameters.getStretch() == null)
                    globalConfigurationParameters.setStretch(0);
                if (globalConfigurationParameters.getClickaction() == null)
                    globalConfigurationParameters.setClickaction(0);
                if (globalConfigurationParameters.getEvents() == null)
                    globalConfigurationParameters.setEvents(new HashMap<String, String[]>());
                if (globalConfigurationParameters.getSkipable() == null)
                    globalConfigurationParameters.setSkipable(true);
                if (globalConfigurationParameters.getSkipOffset() == null)
                    globalConfigurationParameters.setSkipOffset("00:00:00.000");
                if (globalConfigurationParameters.getvpaidSourceUrl() == null)
                    globalConfigurationParameters.setvpaidSourceUrl("");
                if (globalConfigurationParameters.getOrientation() == null)
                    globalConfigurationParameters.setOrientation("p");
                if (globalConfigurationParameters.getBase_sdk_event_url()== null)
                    globalConfigurationParameters.setBase_sdk_event_url("");
//            bid.setApi(2);
                mResponseBody = bid.getAdm();
//            mResponseBody = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><VAST version=\"2.0\">  <Ad id=\"317402160\">  <Wrapper>    <AdSystem>ddm</AdSystem>   <VASTAdTagURI><![CDATA[http://bs.serving-sys.com/Serving?cn=display&c=23&pl=VAST&pli=24850875&PluID=0&pos=8818&ord=1551899620141&cim=1]]></VASTAdTagURI>    <Creatives><Creative AdID=\"317402160\"><Linear><TrackingEvents><Tracking event=\"start\"><![CDATA[https://analyse.statiqmedia.com/impl?client_id=maddict&campaign_id=Moulinex-Young-Cosmo-Android&device_id=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&latitude=28.6&longitude=77.2&cb=1551899620141]]></Tracking>                <Tracking event=\"start\"><![CDATA[https://nep.advangelists.com/xp/evt?srid=03979169-55ce-4bbb-bef9-417e140155ca&osrid=16e1eb1b-8c4c-4ac2-a238-649a23d65f42&t=1551899620&pubid=7d95de69cfc7cc03c3a05b4fde9662b8&plmt=0&tagid=&impid=1&cip=106.223.192.48&adv=233&pkg=1&cid=3616&crid=10750&caid=233&b=com.pandora.android&c=IND&wh=320x480&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ZUK+Z2132+Build%2FMMB29M%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F72.0.3626.105+Mobile+Safari%2F537.36&carrier=airtel&ct=New+Delhi&st=DL&z=110014&dma=&bek=8468ee83bd56d5d128a147a1fc403017_20190306_19&le=0&av=0&lae=0&userid=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&bidid=03979169-55ce-4bbb-bef9-417e140155ca&ifa=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&ccr=1.0&gps=0&lat=28.600000381469727&lng=77.19999694824219&algid=&m=1&act=6]]></Tracking><Tracking event=\"start\"><![CDATA[https://nep.advangelists.com/xp/evt?pp=2YdYOiizRvAtSx8oN9KIrTSKYNHb4OK9MuXsMgTjo3TK12B0mLmruDaD7UqkLnADOB3DGmdPkJEr8RXU4i9LgImjCWMFmut0J0dJs9GlMqSmZiC8UUQxwUw0T2Ja3YujkPU819amQsHbmgjy1U5Mro27oUoWyHwUBUhYkzncdQpjhOkWWDMHbfOST2P2mwILI6tEIouuts2mt4mFhDwOf1fjQi8fWXOp0wcyz1V2F7ItdavizLqPpu3zV1ElidWpQayEhT0MQ9dUi7I6R8A0lxR0kxQjbzCQi91KIL7Y5sVWyk9pvP9fObKdS8qPPeT2oFYHRL3QBXAT9m5eNXtqIEw6abXMroxNgrTkgeTLjSGPrEXWgbTljSF23jJjZENWhVXePBf7Ycpldt9lH9ICJ1nNHOVvMyslagkEMJ26UhzHYBK2ogXzfjNS9jCcGnVg06XrvoBBTFrjAUSihcmxNpqphMbTrGNMDgN3XuI0othJ8RTnKXHNznUQ8Iopw3yy7og2OWorSqCDj0eIkdjC6VfaTpOkuPA389D4ifwwFoMM3XzKBVTZ37LkhCv3hhP7LkwFbZOqKrxA6wQflCr7ZHKlZNFgjhWcZn9qyOkU8XhpEnFZSVwQxcp1SJYuGNRm8zIv7B4XRKy8jZgLst2jZjXs3uhE8mD4D6o68mlzpmbutGZLnsJGq5Cn51nAT6cnFspUbo9fQvl2yEBZkJoZuQhV2Q3lWQxOup6P72NAafOPehbbW48G1becv0gmSLXQjQBAyV7xznvrOIVxaQ3xwquxiF1LpGjOSn8XWwZuu6jgSbHbD0xdJ00siRqZlbPrzblTOWhhPT2sFXezEZuXu45kl9eh9QniH9OJnrl6tKZHMRZBg4SAayEh4b5xKaEsIwDpjV0O13QXEXTfBTzdd6hIY19Ip1ZRKipyvjwPELG5uq1KyiwuFsYWz1CscVB7LTgzu5TESAARt5vJqe1BrcJQ07KwLbj17BIPxgm2hwOVevlhFtRmnJmnHmthLnv14KKxKY5jZ3keDSdzPLWEqe0OK75ufmNGftFzuljUa0fK7lg88tQKml5W6FKw4sjUHqO34HktPrk3pCwHQSAhHWrOrzVsgRNu35GjeTOMGmPWRsH3sEKWkEehsXLkFr3Q__]]></Tracking><Tracking event=\"firstQuartile\"><![CDATA[https://nep.advangelists.com/xp/evt?srid=03979169-55ce-4bbb-bef9-417e140155ca&osrid=16e1eb1b-8c4c-4ac2-a238-649a23d65f42&t=1551899620&pubid=7d95de69cfc7cc03c3a05b4fde9662b8&plmt=0&tagid=&impid=1&cip=106.223.192.48&adv=233&pkg=1&cid=3616&crid=10750&caid=233&b=com.pandora.android&c=IND&wh=320x480&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ZUK+Z2132+Build%2FMMB29M%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F72.0.3626.105+Mobile+Safari%2F537.36&carrier=airtel&ct=New+Delhi&st=DL&z=110014&dma=&bek=8468ee83bd56d5d128a147a1fc403017_20190306_19&le=0&av=0&lae=0&userid=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&bidid=03979169-55ce-4bbb-bef9-417e140155ca&ifa=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&ccr=1.0&gps=0&lat=28.600000381469727&lng=77.19999694824219&algid=&m=1&act=7]]></Tracking><Tracking event=\"midpoint\"><![CDATA[https://nep.advangelists.com/xp/evt?srid=03979169-55ce-4bbb-bef9-417e140155ca&osrid=16e1eb1b-8c4c-4ac2-a238-649a23d65f42&t=1551899620&pubid=7d95de69cfc7cc03c3a05b4fde9662b8&plmt=0&tagid=&impid=1&cip=106.223.192.48&adv=233&pkg=1&cid=3616&crid=10750&caid=233&b=com.pandora.android&c=IND&wh=320x480&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ZUK+Z2132+Build%2FMMB29M%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F72.0.3626.105+Mobile+Safari%2F537.36&carrier=airtel&ct=New+Delhi&st=DL&z=110014&dma=&bek=8468ee83bd56d5d128a147a1fc403017_20190306_19&le=0&av=0&lae=0&userid=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&bidid=03979169-55ce-4bbb-bef9-417e140155ca&ifa=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&ccr=1.0&gps=0&lat=28.600000381469727&lng=77.19999694824219&algid=&m=1&act=8]]></Tracking><Tracking event=\"thirdQuartile\"><![CDATA[https://nep.advangelists.com/xp/evt?srid=03979169-55ce-4bbb-bef9-417e140155ca&osrid=16e1eb1b-8c4c-4ac2-a238-649a23d65f42&t=1551899620&pubid=7d95de69cfc7cc03c3a05b4fde9662b8&plmt=0&tagid=&impid=1&cip=106.223.192.48&adv=233&pkg=1&cid=3616&crid=10750&caid=233&b=com.pandora.android&c=IND&wh=320x480&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ZUK+Z2132+Build%2FMMB29M%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F72.0.3626.105+Mobile+Safari%2F537.36&carrier=airtel&ct=New+Delhi&st=DL&z=110014&dma=&bek=8468ee83bd56d5d128a147a1fc403017_20190306_19&le=0&av=0&lae=0&userid=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&bidid=03979169-55ce-4bbb-bef9-417e140155ca&ifa=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&ccr=1.0&gps=0&lat=28.600000381469727&lng=77.19999694824219&algid=&m=1&act=9]]></Tracking><Tracking event=\"complete\"><![CDATA[https://nep.advangelists.com/xp/evt?srid=03979169-55ce-4bbb-bef9-417e140155ca&osrid=16e1eb1b-8c4c-4ac2-a238-649a23d65f42&t=1551899620&pubid=7d95de69cfc7cc03c3a05b4fde9662b8&plmt=0&tagid=&impid=1&cip=106.223.192.48&adv=233&pkg=1&cid=3616&crid=10750&caid=233&b=com.pandora.android&c=IND&wh=320x480&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ZUK+Z2132+Build%2FMMB29M%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F72.0.3626.105+Mobile+Safari%2F537.36&carrier=airtel&ct=New+Delhi&st=DL&z=110014&dma=&bek=8468ee83bd56d5d128a147a1fc403017_20190306_19&le=0&av=0&lae=0&userid=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&bidid=03979169-55ce-4bbb-bef9-417e140155ca&ifa=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&ccr=1.0&gps=0&lat=28.600000381469727&lng=77.19999694824219&algid=&m=1&act=10]]></Tracking><Tracking event=\"close\"><![CDATA[https://nep.advangelists.com/xp/evt?srid=03979169-55ce-4bbb-bef9-417e140155ca&osrid=16e1eb1b-8c4c-4ac2-a238-649a23d65f42&t=1551899620&pubid=7d95de69cfc7cc03c3a05b4fde9662b8&plmt=0&tagid=&impid=1&cip=106.223.192.48&adv=233&pkg=1&cid=3616&crid=10750&caid=233&b=com.pandora.android&c=IND&wh=320x480&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ZUK+Z2132+Build%2FMMB29M%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F72.0.3626.105+Mobile+Safari%2F537.36&carrier=airtel&ct=New+Delhi&st=DL&z=110014&dma=&bek=8468ee83bd56d5d128a147a1fc403017_20190306_19&le=0&av=0&lae=0&userid=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&bidid=03979169-55ce-4bbb-bef9-417e140155ca&ifa=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&ccr=1.0&gps=0&lat=28.600000381469727&lng=77.19999694824219&algid=&m=1&act=11]]></Tracking></TrackingEvents><VideoClicks><ClickTracking><![CDATA[https://nep.advangelists.com/xp/evt?pp=6v7k43PfjFO2LQ6KWf1jeV3w9TGy0VyyNM8hewOtjOXfvHSvQngKFiGgHVShYxvBdpOwKnaNWEUu9BXn0weAB2bX0vmM37NyBtO5OnQrQvKUs39LVbspO0H0EIqZeAVMS13c68iePdBWea71qQYQiMGJkT5QgGMogWke0bjhLLhWKfKVn4aqKmC3OaWQN4SMGnqIMS1MZRzNAxkrCIzYitp3Cjcuo0n2MB96rCdPKPYOLMexOqBHZjtPTv7mYRo3gZXqK5kQqBiEZPafoy2VuhnllAe21hY31F6KvtUEAGPpBspsyVye6RSqGSlBOS6W5Xg7dLN5Ce7ymKG4JmUPi5rFI15sKksRNVvh7UBO85fLDGnHVg0hBVq1EhKh66beWtdW4s6DvLnWEPmufX69GYQwjmS89HYMpEXIPMp2AcI4bPPXw3maZme2re3PkvwpdQij8pF2cftERg4XeK5cI1LI7eENEFBeEfpvh9EPJWeeqo3uqbBUAkUFdhiP1289svYYkFadeqEuxZy4F4DTxDtjsNlTZ0C1VIBCnKTvSD1ShPeVpLf9gtug7oc1cBF0aXPA0PdJ1snrrm5SoqNZqDE4eQCdlaBAWUQ6lZviY71npckKb1br7Ooqg9m3Vx1nyvq8ufOtPQNALRVR6HjsQRpJZHYJnhGYySxUwSZDaHBRtPhXpuwfyNsaB27dXIkyHG93AEp5dMSS729nPZ6JI2MDWEWt80GkZkrRXNDigoaGC7qXFJuV8EcBQGXFQFWioyrLaRDxeMQC3659Fo6XHMgblJj92ryQseDN7YnVhLKO5syivLi7N9Q5slzy9qB8CvCPteknqRQumlZHDrN9s8xhEjSevIqWfVOKxNJDLEseDL2KW77xhtQkgxkjtTnou04hSTwnHK7scvjUFHOeJwyZUAORmJ8jZvidCwRxFdGd8fyWXOl9YQ4qSI4wslxM7fNQRia51l6B6iSMsG5XmU7BEY4uVdEER2OQkt59bY5gKi7TWqfl2EZ05knHwCQeuKosdxkRz4RGPTC57ErV8RAsBin26HpiP4c2A5qGrSYx1ZIxLmVnbGUuXah7N3KgbzfBD6OeH5iDBcTXc9clzsEMrjnH4qbeCVI5jDCF5kpOz__]]></ClickTracking></VideoClicks></Linear></Creative></Creatives>  <Impression><![CDATA[https://nep.advangelists.com/xp/evt?srid=03979169-55ce-4bbb-bef9-417e140155ca&osrid=16e1eb1b-8c4c-4ac2-a238-649a23d65f42&t=1551899620&pubid=7d95de69cfc7cc03c3a05b4fde9662b8&plmt=0&tagid=&impid=1&cip=106.223.192.48&adv=233&pkg=1&cid=3616&crid=10750&caid=233&b=com.pandora.android&c=IND&wh=320x480&zoneid=&dealid=&dspid=3&imptype=video&ua=Mozilla%2F5.0+%28Linux%3B+Android+6.0.1%3B+ZUK+Z2132+Build%2FMMB29M%3B+wv%29+AppleWebKit%2F537.36+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Chrome%2F72.0.3626.105+Mobile+Safari%2F537.36&carrier=airtel&ct=New+Delhi&st=DL&z=110014&dma=&bek=8468ee83bd56d5d128a147a1fc403017_20190306_19&le=0&av=0&lae=0&userid=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&bidid=03979169-55ce-4bbb-bef9-417e140155ca&ifa=0b9176c4-975b-4c80-8abf-c0f7e2bd2b85&ccr=1.0&gps=0&lat=28.600000381469727&lng=77.19999694824219&algid=&m=1&act=5]]></Impression></Wrapper>  </Ad></VAST>";
                if (mAdType.value >= 2) {
                    mServerExtras.put(DataKeys.VAST, mResponseBody);
                    try {
                        mResponseBody = URLDecoder.decode(mResponseBody, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Utils.logStackTrace(e);
                        mResponseBody = "";
                    }
                }
//            if (mResponseBody!=null && mResponseBody.length()>0 && (bid.getApi()==null || bid.getApi()<=0 || bid.getApi()>2) && mAdType.value<2){
//                mResponseBody = CDAdConstants.responseBodyPrefix+mResponseBody+CDAdConstants.responseBodySuffix;
//            }
                if (mResponseBody != null && mResponseBody.length() > 0 && (bid.getApi() == null || bid.getApi() <= 0 || bid.getApi() > 2) && mAdType.value < 2) {
                    if (globalConfigurationParameters.getAdSuffix() == null || globalConfigurationParameters.getAdSuffix().length() == 0 || globalConfigurationParameters.getAdPrefix() == null || globalConfigurationParameters.getAdPrefix().length() == 0) {
                        if (mAdType.value == 0 || ((stretch + "").equals(CDAdConstants.StretchType.STRETCH_TYPE_ASPECT_FILL.toString()))) {
                            globalConfigurationParameters.setAdPrefix(CDAdConstants.responseBodyPrefix);
                            globalConfigurationParameters.setAdSuffix(CDAdConstants.responseBodySuffix);
                        } else {
                            globalConfigurationParameters.setAdPrefix(CDAdConstants.interstitialResponseBodyPrefix);
                            globalConfigurationParameters.setAdSuffix(CDAdConstants.interstitialResponseBodySuffix);
                        }
                    }

                    mResponseBody = globalConfigurationParameters.getAdPrefix() + CDAdConstants.mraidJSPrefix + mResponseBody + globalConfigurationParameters.getAdSuffix();
                }
                if (mAdType.value < 2) {
//                if (OmViewabilitySession.isEnabled()){
//                    mResponseBody = OmViewabilitySession.injectOmidJsInHTMLString(mResponseBody);
//                }
                }

                mWidth = (int) bid.getW();
                mHeight = (int) bid.getH();
                mBrowserAgent = globalConfigurationParameters.getClickaction();
                mCustomEventClassName = setUpCustomEventClassFromApi(bid.getApi());
                mOrientation = DeviceUtils.requestedOrientation(context, CreativeOrientation.fromString(globalConfigurationParameters.getOrientation()));
                closeable = globalConfigurationParameters.getClosable();
                skipable = globalConfigurationParameters.getSkipable();
                skipOffset = globalConfigurationParameters.getSkipOffset();
                showBorder = globalConfigurationParameters.getBorder();
                borderWidth = globalConfigurationParameters.getBorderWidth();
                stretch = globalConfigurationParameters.getStretch();
                borderColor = globalConfigurationParameters.getBorderColor();
                vpaidSourceUrl = globalConfigurationParameters.getvpaidSourceUrl();
            }
        }else{
            globalConfigurationParameters = new ConfigurationParameters();
            if (builder.networkResponse.getBase_sdk_event_url()!=null)
                globalConfigurationParameters.setBaseSdkEventUrl(builder.networkResponse.getBase_sdk_event_url());
            if (builder.networkResponse.getSdks()!=null)
                globalConfigurationParameters.setEvents(builder.networkResponse.getSdks());
            stretch = 1;
        }
        mEvents = globalConfigurationParameters.getEvents().toArray(new Event[globalConfigurationParameters.getEvents().size()]);
        mCustomEventClassName = setUpCustomEventClassFromApi(null);
        mNetworkType = "";
        mClickTrackingUrl = "";
        mImpressionTrackingUrl = "";
        mFailoverUrl = "";
        mAdTimeoutDelayMillis = CDAdConstants.INTERSTITIAL_TIMEOUT_INTERVAL;
        mRefreshTime = (int) CDAdConstants.CDAdfetchInterval * 1000;
        mScrollable = false;
        mJsonBody = null;
        mEventDetails = null;
        mDspCreativeId = null;
        mRequestId = "";
        mRedirectUrl = "";
        mAdUnitId = "";
        mFullAdType = "";
        mRewardedVideoCurrencyName = "";
        mRewardedVideoCurrencyAmount = "";
        mRewardedCurrencies = "";
        mRewardedDuration = 0;
        mRewardedVideoCompletionUrl = "";
        mShouldRewardOnClick = false;
        mServerExtras.put(DataKeys.HTML_RESPONSE_BODY_KEY, mResponseBody);
        if (stretch > 0) {
            mServerExtras.put(DataKeys.HEIGHT, getHeight() + "");
            mServerExtras.put(DataKeys.WIDTH, getWidth() + "");
            mServerExtras.put(DataKeys.STRETCH, stretch + "");
        }
        if (mAdType == CDAdType.CDAdTypeInterstitial)
            mServerExtras.put(DataKeys.FORCE_CLOSE_BUTTON, closeable ? "true" : "false");
        mServerExtras.put(DataKeys.SKIP_ENABLE, skipable ? "true" : "false");
        mServerExtras.put(DataKeys.SKIP_OFFSET, skipOffset);
        mServerExtras.put(DataKeys.VPAID_SOURCE_URL, vpaidSourceUrl);
        mServerExtras.put(DataKeys.CREATIVE_ORIENTATION_KEY, mOrientation + "");
        mServerExtras.put(DataKeys.CLICK_ACTION, mBrowserAgent + "");
        mServerExtras.put(DataKeys.BASE_SDK_EVENT_URL, globalConfigurationParameters.getBase_sdk_event_url());
        mTimestamp = DateAndTime.now().getTime();
//        mEvents = new Event[2];
//        HashMap<String, Object> eventDataObject1 = new HashMap<String, Object>();
//        HashMap<String, Object> bannerObject1 = new HashMap<String, Object>();
//        HashMap<String, Object> interstitialObject1 = new HashMap<String, Object>();
//        bannerObject1.put("class", "com.chalkdigital.mediation.ssbwrapper.SSBEventBanner");
//        interstitialObject1.put("class", "com.chalkdigital.mediation.ssbwrapper.SSBEventInterstitial");
//        eventDataObject1.put("Android_Interstitial", interstitialObject1);
//        eventDataObject1.put("Android_Banner", bannerObject1);
//        Event event1 = new Event(null);
//        event1.setId(2);
//        event1.setTimeout(30);
//        event1.setEventData(eventDataObject1);
//        mEvents[0] = event1;
//        Event event2 = new Event(null);
//        event2.setId(1);
//        event2.setTimeout(30);
//        event2.setEventData(new HashMap<String, Object>());
//        mEvents[1] = event2;

    }

    public boolean hasJson() {
        return mJsonBody != null;
    }

    @Nullable
    public JSONObject getJsonBody() {
        return mJsonBody;
    }

    @Nullable
    public EventDetails getEventDetails() {
        return mEventDetails;
    }

    @Nullable
    public String getStringBody() {
        return mResponseBody;
    }

    @Nullable
    public CDAdType getAdType() {
        return mAdType;
    }

    @Nullable
    public String getFullAdType() {
        return mFullAdType;
    }

    @Nullable
    public String getAdUnitId() {
        return mAdUnitId;
    }

    @Nullable
    public String getNetworkType() {
        return mNetworkType;
    }

    @Nullable
    public String getRewardedVideoCurrencyName() {
        return mRewardedVideoCurrencyName;
    }

    @Nullable
    public String getRewardedVideoCurrencyAmount() {
        return mRewardedVideoCurrencyAmount;
    }

    @Nullable
    public String getRewardedCurrencies() {
        return mRewardedCurrencies;
    }

    @Nullable
    public String getRewardedVideoCompletionUrl() {
        return mRewardedVideoCompletionUrl;
    }

    @Nullable
    public Integer getRewardedDuration() {
        return mRewardedDuration;
    }

    public boolean shouldRewardOnClick() {
        return mShouldRewardOnClick;
    }

    @Nullable
    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    @Nullable
    public String getClickTrackingUrl() {
        return mClickTrackingUrl;
    }

    @Nullable
    public String getImpressionTrackingUrl() {
        return mImpressionTrackingUrl;
    }

    @Nullable
    public String getFailoverUrl() {
        return mFailoverUrl;
    }

    @Nullable
    public String getRequestId() {
        return mRequestId;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Nullable
    public Integer getWidth() {
        return mWidth;
    }

    @Nullable
    public Integer getHeight() {
        return mHeight;
    }

    @Nullable
    public Integer getAdTimeoutMillis() {
        return mAdTimeoutDelayMillis;
    }

    @Nullable
    public Integer getRefreshTime() {
        return mRefreshTime;
    }

    @Nullable
    public String getDspCreativeId() {
        return mDspCreativeId;
    }

    @Nullable
    public String getCustomEventClassName() {
        return mCustomEventClassName;
    }

    @Nullable
    public Integer getBrowserAgent() { return mBrowserAgent; }

    public long getTimestamp() {
        return mTimestamp;
    }

    public boolean isCloseable() {
        if(getAdType()==CDAdType.CDAdTypeVideo)
            return false;
        return closeable;
    }

    public boolean isTakeScreenshot() {
        return takeScreenshot;
    }

    public boolean isShowBorder() {
        return showBorder;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public int shouldStretch() {
        return stretch;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public Event[] getEvents(){
        return mEvents;
    }

    public boolean isSkipable() {
        return skipable;
    }

    public String getSkipOffset() {
        return skipOffset;
    }

    @NonNull
    public Map<String, String> getServerExtras() {
        // Strings are immutable, so this works as a "deep" copy.
        TreeMap<String, String > serverExtras = new TreeMap<String, String>(mServerExtras);
        return serverExtras;
    }

    private String setUpCustomEventClassFromApi(Integer api){
        api = null;
        String customEventClassName = "";
        HashMap<Object, String> convertedCustomEvents = new HashMap<>();
        if (mAdType == CDAdType.CDAdTypeBanner){
            convertedCustomEvents.put("html", "com.chalkdigital.banner.ads.HtmlBanner");
            convertedCustomEvents.put(new Integer(5), "com.chalkdigital.banner.mraid.MraidBanner");
        }else if (mAdType == CDAdType.CDAdTypeInterstitial){
            convertedCustomEvents.put("html", "com.chalkdigital.interstitial.ads.HtmlInterstitial");
            convertedCustomEvents.put(new Integer(5), "com.chalkdigital.interstitial.mraid.MraidInterstitial");
        }else if (mAdType == CDAdType.CDAdTypeVideo){
            convertedCustomEvents.put("html", "com.chalkdigital.nativeads.CDAdCustomEventVideoNative");
            convertedCustomEvents.put(new Integer(1), "com.chalkdigital.nativeads.CDAdCustomEventVideoNative");
            convertedCustomEvents.put(new Integer(2), "com.chalkdigital.nativeads.CDAdCustomEventVideoNative");
        }else if (mAdType == CDAdType.CDAdTypeInterstitialVideo){
            convertedCustomEvents.put("html", "com.chalkdigital.spark.SparkInterstitial");
            convertedCustomEvents.put(new Integer(1), "com.chalkdigital.spark.SparkInterstitial");
            convertedCustomEvents.put(new Integer(2), "com.chalkdigital.spark.SparkInterstitial");
        }
        if (api == null) {
            if ( mResponseBody!=null && mResponseBody.contains("mraid"))
                customEventClassName = convertedCustomEvents.get(5);
            else
                customEventClassName = convertedCustomEvents.get("html");
        }
        else{
            if (convertedCustomEvents.keySet().contains(api))
                customEventClassName = convertedCustomEvents.get(api);
            else customEventClassName = convertedCustomEvents.get("html");
        }

        if (customEventClassName==null || customEventClassName.equals(""))
            CDAdLog.d("Could not find custom event class named "+ customEventClassName);

        return customEventClassName;
    }

    public Builder toBuilder() {
        return new Builder()
                .setAdType(mAdType);
    }

    public static class Builder {
        private NetworkResponse networkResponse;
        private Context context;
        private CDAdType adType;

        public NetworkResponse getNetworkResponse() {
            return networkResponse;
        }

        public Builder setNetworkResponse(NetworkResponse networkResponse, Context context) {
            this.networkResponse = networkResponse;
            this.context = context;
            return this;
        }

        public CDAdType getAdType() {
            return adType;
        }

        public Builder setAdType(CDAdType adType) {
            this.adType = adType;
            return this;
        }

        public AdResponse build() {
            return new AdResponse(this, context);
        }
    }

    public enum CDAdType{
        CDAdTypeUnknown(-1),
        CDAdTypeBanner(0),
        CDAdTypeInterstitial(1),
        CDAdTypeVideo(2),
        CDAdTypeInterstitialVideo(3),
        CDAdTypeText(4),
        CDAdTypePopup(5);

        private  Integer value;
        private CDAdType(Integer value){
            this.value = value;
        }

        public static Integer getValue(CDAdType cdAdType){
            return cdAdType.value;
        }
    }

}
