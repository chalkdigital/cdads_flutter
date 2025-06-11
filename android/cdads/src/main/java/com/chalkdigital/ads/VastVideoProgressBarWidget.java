package com.chalkdigital.ads;

import android.content.Context;
import android.support.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.View;
import android.widget.RelativeLayout;

import com.chalkdigital.ads.resource.DrawableConstants;
import com.chalkdigital.ads.resource.ProgressBarDrawable;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.util.Dips;
import com.chalkdigital.common.util.Utils;

public class VastVideoProgressBarWidget extends AppCompatImageView {
    @NonNull private ProgressBarDrawable mProgressBarDrawable;
    private final int mProgressBarHeight;

    public VastVideoProgressBarWidget(@NonNull final Context context) {
        super(context);

        setId((int) Utils.generateUniqueId());

        mProgressBarDrawable = new ProgressBarDrawable(context);
        setImageDrawable(mProgressBarDrawable);

        mProgressBarHeight =
                Dips.dipsToIntPixels(DrawableConstants.ProgressBar.HEIGHT_DIPS, context);
    }

    public void setAnchorId(final int anchorId) {
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                mProgressBarHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, anchorId);
        setLayoutParams(layoutParams);

    }

    public void calibrateAndMakeVisible(final Integer duration, final int skipOffset) {
        mProgressBarDrawable.setDurationAndSkipOffset(duration, skipOffset);
        setVisibility(View.VISIBLE);
    }

    public void updateProgress(final int progress) {
        mProgressBarDrawable.setProgress(progress);
    }

    public void reset() {
        mProgressBarDrawable.reset();
        mProgressBarDrawable.setProgress(0);
    }

    // for testing
    @Deprecated
    @VisibleForTesting
    ProgressBarDrawable getImageViewDrawable() {
        return mProgressBarDrawable;
    }

    // for testing
    @Deprecated
    @VisibleForTesting
    void setImageViewDrawable(@NonNull ProgressBarDrawable drawable) {
        mProgressBarDrawable = drawable;
    }
}
