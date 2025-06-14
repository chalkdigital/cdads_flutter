package com.chalkdigital.nativeads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chalkdigital.common.Preconditions;
import com.chalkdigital.common.VisibleForTesting;

import java.util.WeakHashMap;

import static android.view.View.VISIBLE;

/**
 * An implementation of {@link CDAdAdRenderer} for rendering native ads.
 */
public class CDAdStaticNativeAdRenderer implements CDAdAdRenderer<StaticNativeAd> {
    @NonNull private final ViewBinder mViewBinder;

    // This is used instead of View.setTag, which causes a memory leak in 2.3
    // and earlier: https://code.google.com/p/android/issues/detail?id=18273
    @VisibleForTesting @NonNull final WeakHashMap<View, StaticNativeViewHolder> mViewHolderMap;

    /**
     * Constructs a native ad renderer with a view binder.
     *
     * @param viewBinder The view binder to use when inflating and rendering an ad.
     */
    public CDAdStaticNativeAdRenderer(@NonNull final ViewBinder viewBinder) {
        mViewBinder = viewBinder;
        mViewHolderMap = new WeakHashMap<View, StaticNativeViewHolder>();
    }

    @Override
    @NonNull
    public View createAdView(@NonNull final Context context, @Nullable final ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(mViewBinder.layoutId, parent, false);
    }

    @Override
    public void renderAdView(@NonNull final View view,
            @NonNull final StaticNativeAd staticNativeAd) {
        StaticNativeViewHolder staticNativeViewHolder = mViewHolderMap.get(view);
        if (staticNativeViewHolder == null) {
            staticNativeViewHolder = StaticNativeViewHolder.fromViewBinder(view, mViewBinder);
            mViewHolderMap.put(view, staticNativeViewHolder);
        }

        update(staticNativeViewHolder, staticNativeAd);
        NativeRendererHelper.updateExtras(staticNativeViewHolder.mainView,
                mViewBinder.extras,
                staticNativeAd.getExtras());
        setViewVisibility(staticNativeViewHolder, VISIBLE);
    }

    @Override
    public void renderAdView(@NonNull final ViewGroup view, @NonNull final StaticNativeAd ad) {

    }

    @Override
    public boolean supports(@NonNull final BaseNativeAd nativeAd) {
        Preconditions.checkNotNull(nativeAd);
        return nativeAd instanceof StaticNativeAd;
    }

    private void update(@NonNull final StaticNativeViewHolder staticNativeViewHolder,
            @NonNull final StaticNativeAd staticNativeAd) {
        NativeRendererHelper.addTextView(staticNativeViewHolder.titleView,
                staticNativeAd.getTitle());
        NativeRendererHelper.addTextView(staticNativeViewHolder.textView, staticNativeAd.getText());
        NativeRendererHelper.addTextView(staticNativeViewHolder.callToActionView,
                staticNativeAd.getCallToAction());
        NativeImageHelper.loadImageView(staticNativeAd.getMainImageUrl(),
                staticNativeViewHolder.mainImageView);
        NativeImageHelper.loadImageView(staticNativeAd.getIconImageUrl(),
                staticNativeViewHolder.iconImageView);
        NativeRendererHelper.addPrivacyInformationIcon(
                staticNativeViewHolder.privacyInformationIconImageView,
                staticNativeAd.getPrivacyInformationIconImageUrl(),
                staticNativeAd.getPrivacyInformationIconClickThroughUrl());
    }

    private void setViewVisibility(@NonNull final StaticNativeViewHolder staticNativeViewHolder,
            final int visibility) {
        if (staticNativeViewHolder.mainView != null) {
            staticNativeViewHolder.mainView.setVisibility(visibility);
        }
    }
}
