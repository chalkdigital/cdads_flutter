package com.chalkdigital.ads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chalkdigital.ads.resource.CloseButtonDrawable;
import com.chalkdigital.ads.resource.DrawableConstants;
import com.chalkdigital.common.VisibleForTesting;
import com.chalkdigital.common.util.Dips;
import com.chalkdigital.common.util.Utils;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class VastVideoCloseButtonWidget extends RelativeLayout {
    @NonNull private TextView mTextView;
    @NonNull private ImageView mImageView;
//    @NonNull private final ImageLoader mImageLoader;
    @NonNull private CloseButtonDrawable mCloseButtonDrawable;

    private final int mEdgePadding;
    private final int mTextRightMargin;
    private final int mImagePadding;
    private final int mWidgetHeight;

    public VastVideoCloseButtonWidget(@NonNull final Context context) {
        super(context);

        setId((int) Utils.generateUniqueId());

        mEdgePadding = Dips.dipsToIntPixels(DrawableConstants.CloseButton.EDGE_PADDING, context);
        mImagePadding = Dips.dipsToIntPixels(DrawableConstants.CloseButton.IMAGE_PADDING_DIPS, context);
        mWidgetHeight = Dips.dipsToIntPixels(DrawableConstants.CloseButton.WIDGET_HEIGHT_DIPS, context);
        mTextRightMargin = Dips.dipsToIntPixels(DrawableConstants.CloseButton.TEXT_RIGHT_MARGIN_DIPS, context);

        mCloseButtonDrawable = new CloseButtonDrawable();
//        mImageLoader = Networking.getImageLoader(context);

        createImageView();
        createTextView();

        final LayoutParams layoutParams = new LayoutParams(
                WRAP_CONTENT,
                mWidgetHeight);

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP | RelativeLayout.ALIGN_PARENT_RIGHT);
        setLayoutParams(layoutParams);
    }

    private void createImageView() {
        mImageView = new ImageView(getContext());
        mImageView.setId((int) Utils.generateUniqueId());

        final LayoutParams iconLayoutParams = new LayoutParams(
                mWidgetHeight,
                mWidgetHeight);

        iconLayoutParams.addRule(ALIGN_PARENT_RIGHT);

        mImageView.setImageDrawable(mCloseButtonDrawable);
        mImageView.setPadding(mImagePadding, mImagePadding + mEdgePadding, mImagePadding + mEdgePadding, mImagePadding);
        addView(mImageView, iconLayoutParams);
    }

    private void createTextView() {
        mTextView = new TextView(getContext());
        mTextView.setSingleLine();
        mTextView.setEllipsize(TextUtils.TruncateAt.END);
        mTextView.setTextColor(DrawableConstants.CloseButton.TEXT_COLOR);
        mTextView.setTextSize(DrawableConstants.CloseButton.TEXT_SIZE_SP);
        mTextView.setTypeface(DrawableConstants.CloseButton.TEXT_TYPEFACE);
        mTextView.setText(DrawableConstants.CloseButton.DEFAULT_CLOSE_BUTTON_TEXT);

        final LayoutParams textLayoutParams = new LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT);

        textLayoutParams.addRule(CENTER_VERTICAL);
        textLayoutParams.addRule(LEFT_OF, mImageView.getId());

        mTextView.setPadding(0, mEdgePadding, 0, 0);
        // space between text and image
        textLayoutParams.setMargins(0, 0, mTextRightMargin, 0);

        addView(mTextView, textLayoutParams);
    }

    void updateCloseButtonText(@Nullable final String text) {
        if (mTextView != null) {
            mTextView.setText(text);
        }
    }

    void updateCloseButtonIcon(@NonNull final String imageUrl) {
//        mImageLoader.get(imageUrl, new ImageLoader.ImageListener() {
//            @Override
//            public void onResponse(final ImageLoader.ImageContainer imageContainer,
//                    final boolean isImmediate) {
//                Bitmap bitmap = imageContainer.getBitmap();
//                if (bitmap != null) {
//                    mImageView.setImageBitmap(bitmap);
//                } else {
//                    CDAdLog.d(String.format("%s returned null bitmap", imageUrl));
//                }
//            }
//
//            @Override
//            public void onErrorResponse(final VolleyError volleyError) {
//                CDAdLog.d("Failed to load image.", volleyError);
//            }
//        });
    }

    public void setOnTouchListenerToContent(@Nullable OnTouchListener onTouchListener) {
        mImageView.setOnTouchListener(onTouchListener);
        mTextView.setOnTouchListener(onTouchListener);
    }

    // for testing
    @Deprecated
    @VisibleForTesting
    ImageView getImageView() {
        return mImageView;
    }

    // for testing
    @Deprecated
    @VisibleForTesting
    void setImageView(ImageView imageView) {
        mImageView = imageView;
    }

    // for testing
    @Deprecated
    @VisibleForTesting
    TextView getTextView() {
        return mTextView;
    }
}
