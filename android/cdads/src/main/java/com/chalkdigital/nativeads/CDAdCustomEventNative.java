package com.chalkdigital.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.chalkdigital.common.DataKeys;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.nativeads.NativeImageHelper.ImageListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.chalkdigital.common.DataKeys.CLICK_ACTION;
import static com.chalkdigital.common.DataKeys.JSON_BODY_KEY;
import static com.chalkdigital.common.util.Numbers.parseDouble;
import static com.chalkdigital.nativeads.NativeImageHelper.preCacheImages;

public class CDAdCustomEventNative extends CustomEventNative {

    @Override
    protected void loadNativeAd(@NonNull final Context context,
            @NonNull final CustomEventNativeListener customEventNativeListener,
            @NonNull final Map<String, Object> localExtras,
            @NonNull final Map<String, String> serverExtras) {

        Object json = localExtras.get(JSON_BODY_KEY);
        // null or non-JSONObjects should not be passed in localExtras as JSON_BODY_KEY
        if (!(json instanceof JSONObject)) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.INVALID_RESPONSE);
            return;
        }

        final CDAdStaticNativeAd cdAdStaticNativeAd =
                new CDAdStaticNativeAd(context,
                        (JSONObject) json,
                        new ImpressionTracker(context),
                        new NativeClickHandler(context, serverExtras.get(CLICK_ACTION)),
                        customEventNativeListener);

        if (serverExtras.containsKey(DataKeys.IMPRESSION_MIN_VISIBLE_PERCENT)) {
            try {
                cdAdStaticNativeAd.setImpressionMinPercentageViewed(Integer.parseInt(
                        serverExtras.get(DataKeys.IMPRESSION_MIN_VISIBLE_PERCENT)));
            } catch (final NumberFormatException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Unable to format min visible percent: " +
                        serverExtras.get(DataKeys.IMPRESSION_MIN_VISIBLE_PERCENT));
            }
        }

        if (serverExtras.containsKey(DataKeys.IMPRESSION_VISIBLE_MS)) {
            try {
                cdAdStaticNativeAd.setImpressionMinTimeViewed(
                        Integer.parseInt(serverExtras.get(DataKeys.IMPRESSION_VISIBLE_MS)));
            } catch (final NumberFormatException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Unable to format min time: " +
                        serverExtras.get(DataKeys.IMPRESSION_VISIBLE_MS));
            }
        }

        if (serverExtras.containsKey(DataKeys.IMPRESSION_MIN_VISIBLE_PX)) {
            try {
                cdAdStaticNativeAd.setImpressionMinVisiblePx(Integer.parseInt(
                        serverExtras.get(DataKeys.IMPRESSION_MIN_VISIBLE_PX)));
            } catch (final NumberFormatException e) {
                        Utils.logStackTrace(e);
                CDAdLog.d("Unable to format min visible px: " +
                        serverExtras.get(DataKeys.IMPRESSION_MIN_VISIBLE_PX));
            }
        }

        try {
            cdAdStaticNativeAd.loadAd();
        } catch (IllegalArgumentException e) {
                        Utils.logStackTrace(e);
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.UNSPECIFIED);
        }
    }

    static class CDAdStaticNativeAd extends StaticNativeAd {
        enum Parameter {
            IMPRESSION_TRACKER("imptracker", true),
            CLICK_TRACKER("clktracker", true),

            TITLE("title", false),
            TEXT("text", false),
            MAIN_IMAGE("mainimage", false),
            ICON_IMAGE("iconimage", false),

            CLICK_DESTINATION("clk", false),
            FALLBACK("fallback", false),
            CALL_TO_ACTION("ctatext", false),
            STAR_RATING("starrating", false);

            @NonNull final String name;
            final boolean required;

            Parameter(@NonNull final String name, final boolean required) {
                this.name = name;
                this.required = required;
            }

            @Nullable
            static Parameter from(@NonNull final String name) {
                for (final Parameter parameter : values()) {
                    if (parameter.name.equals(name)) {
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
                    if (parameter.required) {
                        requiredKeys.add(parameter.name);
                    }
                }
            }
        }

        @VisibleForTesting
        static final String PRIVACY_INFORMATION_CLICKTHROUGH_URL = "https://www.chalkdigital.com/optout";

        @NonNull private final Context mContext;
        @NonNull private final CustomEventNativeListener mCustomEventNativeListener;
        @NonNull private final JSONObject mJsonObject;
        @NonNull private final ImpressionTracker mImpressionTracker;
        @NonNull private final NativeClickHandler mNativeClickHandler;

        CDAdStaticNativeAd(@NonNull final Context context,
                @NonNull final JSONObject jsonBody,
                @NonNull final ImpressionTracker impressionTracker,
                @NonNull final NativeClickHandler nativeClickHandler,
                @NonNull final CustomEventNativeListener customEventNativeListener) {
            mJsonObject = jsonBody;
            mContext = context.getApplicationContext();
            mImpressionTracker = impressionTracker;
            mNativeClickHandler = nativeClickHandler;
            mCustomEventNativeListener = customEventNativeListener;
        }

        void loadAd() throws IllegalArgumentException {
            if (!containsRequiredKeys(mJsonObject)) {
                throw new IllegalArgumentException("JSONObject did not contain required keys.");
            }

            final Iterator<String> keys = mJsonObject.keys();
            while (keys.hasNext()) {
                final String key = keys.next();
                final Parameter parameter = Parameter.from(key);

                if (parameter != null) {
                    try {
                        addInstanceVariable(parameter, mJsonObject.opt(key));
                    } catch (ClassCastException e) {
                        Utils.logStackTrace(e);
                        throw new IllegalArgumentException("JSONObject key (" + key + ") contained unexpected value.");
                    }
                } else {
                    addExtra(key, mJsonObject.opt(key));
                }
            }
            setPrivacyInformationIconClickThroughUrl(PRIVACY_INFORMATION_CLICKTHROUGH_URL);

            preCacheImages(mContext, getAllImageUrls(), new ImageListener() {
                @Override
                public void onImagesCached() {
                    mCustomEventNativeListener.onNativeAdLoaded(CDAdStaticNativeAd.this);
                }

                @Override
                public void onImagesFailedToCache(final NativeErrorCode errorCode) {
                    mCustomEventNativeListener.onNativeAdFailed(errorCode);
                }
            });
        }

        private boolean containsRequiredKeys(@NonNull final JSONObject jsonObject) {
            final Set<String> keys = new HashSet<String>();
            final Iterator<String> jsonKeys = jsonObject.keys();
            while (jsonKeys.hasNext()) {
                keys.add(jsonKeys.next());
            }

            return keys.containsAll(Parameter.requiredKeys);
        }

        private void addInstanceVariable(@NonNull final Parameter key,
                @Nullable final Object value) throws ClassCastException {
            try {
                switch (key) {
                    case MAIN_IMAGE:
                        setMainImageUrl((String) value);
                        break;
                    case ICON_IMAGE:
                        setIconImageUrl((String) value);
                        break;
                    case IMPRESSION_TRACKER:
                        addImpressionTrackers(value);
                        break;
                    case CLICK_DESTINATION:
                        setClickDestinationUrl((String) value);
                        break;
                    case CLICK_TRACKER:
                        parseClickTrackers(value);
                        break;
                    case CALL_TO_ACTION:
                        setCallToAction((String) value);
                        break;
                    case TITLE:
                        setTitle((String) value);
                        break;
                    case TEXT:
                        setText((String) value);
                        break;
                    case STAR_RATING:
                        setStarRating(parseDouble(value));
                        break;
                    default:
                        CDAdLog.d("Unable to add JSON key to internal mapping: " + key.name);
                        break;
                }
            } catch (ClassCastException e) {
                        Utils.logStackTrace(e);
                if (!key.required) {
                    CDAdLog.d("Ignoring class cast exception for optional key: " + key.name);
                } else {
                    throw e;
                }
            }
        }

        private void parseClickTrackers(@NonNull final Object clickTrackers) {
            if (clickTrackers instanceof JSONArray) {
                addClickTrackers(clickTrackers);
            } else {
                addClickTracker((String) clickTrackers);
            }
        }

        private boolean isImageKey(@Nullable final String name) {
            return name != null && name.toLowerCase(Locale.US).endsWith("image");
        }

        @NonNull
        List<String> getExtrasImageUrls() {
            final List<String> extrasBitmapUrls = new ArrayList<String>(getExtras().size());
            for (final Map.Entry<String, Object> entry : getExtras().entrySet()) {
                if (isImageKey(entry.getKey()) && entry.getValue() instanceof String) {
                    extrasBitmapUrls.add((String) entry.getValue());
                }
            }

            return extrasBitmapUrls;
        }

        @NonNull
        List<String> getAllImageUrls() {
            final List<String> imageUrls = new ArrayList<String>();
            if (getMainImageUrl() != null) {
                imageUrls.add(getMainImageUrl());
            }
            if (getIconImageUrl() != null) {
                imageUrls.add(getIconImageUrl());
            }

            imageUrls.addAll(getExtrasImageUrls());
            return imageUrls;
        }

        // Lifecycle Handlers
        @Override
        public void prepare(@NonNull final View view) {
            mImpressionTracker.addView(view, this);
            mNativeClickHandler.setOnClickListener(view, this);
        }

        @Override
        public void clear(@NonNull final View view) {
            mImpressionTracker.removeView(view);
            mNativeClickHandler.clearOnClickListener(view);
        }

        @Override
        public void destroy() {
            mImpressionTracker.destroy();
        }

        // Event Handlers
        @Override
        public void recordImpression(@NonNull final View view) {
            notifyAdImpressed();
        }

        @Override
        public void handleClick(@Nullable final View view, final String clickAction) {
            notifyAdClicked();
            mNativeClickHandler.openClickDestinationUrl(getClickDestinationUrl(), view, clickAction);
        }
    }
}
