package com.chalkdigital.common;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chalkdigital.common.logging.CDAdLog;
import com.chalkdigital.common.util.Intents;
import com.chalkdigital.common.util.Utils;
import com.chalkdigital.exceptions.IntentNotResolvableException;

import java.util.EnumSet;

import static com.chalkdigital.common.UrlResolutionTask.UrlResolutionListener;
//import static com.chalkdigital.network.TrackingRequest.makeTrackingHttpRequest;

/**
 * {@code UrlHandler} facilitates handling user clicks on different URLs, allowing configuration
 * for which kinds of URLs to handle and then responding accordingly for a given URL.
 *
 * This class is designed to be instantiated for a single use by immediately calling its {@link
 * #handleUrl(Context, String, String)} method upon constructing it.
 */
public class UrlHandler {

    /**
     * {@code ClickListener} defines the methods that {@link UrlHandler} calls when handling a
     * certain click succeeds or fails.
     */
    public interface ResultActions {
        /**
         * Called if the URL matched a supported {@link UrlAction} and was resolvable. Will be
         * called at most 1 times and is mutually exclusive with
         * {@link ResultActions#urlHandlingFailed(String, UrlAction)}.
         */
        void urlHandlingSucceeded(@NonNull final String url, @NonNull final UrlAction urlAction);

        /**
         * Called with {@link UrlAction#NOOP} if the URL did not match any supported
         * {@link UrlAction}s; or, called with the last matching {@link UrlAction} if URL was
         * unresolvable. Will be called at most 1 times and is mutually exclusive with
         * {@link ResultActions#urlHandlingSucceeded(String, UrlAction)}.
         */
        void urlHandlingFailed(@NonNull final String url,
                               @NonNull final UrlAction lastFailedUrlAction);
    }

    /**
     * {@code CDAdSchemeListener} defines the methods that {@link UrlHandler} calls when handling
     * {@code HANDLE_CDAD_SCHEME} URLs.
     */
    public interface CDAdSchemeListener {
        void onFinishLoad();
        void onClose();
        void onFailLoad();
    }

    /**
     * {@code Builder} provides an API to configure an immutable {@link UrlHandler} and create it.
     */
    public static class Builder {
        @NonNull
        private EnumSet<UrlAction> supportedUrlActions = EnumSet.of(UrlAction.NOOP);
        @NonNull
        private ResultActions resultActions = EMPTY_CLICK_LISTENER;
        @NonNull
        private CDAdSchemeListener cdAdsSchemeListener = EMPTY_CDAD_SCHEME_LISTENER;
        private boolean skipShowCDAdBrowser = false;
        @Nullable private String creativeId;

        /**
         * Sets the {@link UrlAction}s to support in the {@code UrlHandler} to build.
         *
         * @param first A {@code UrlAction} for the {@code UrlHandler} to support.
         * @param others An arbitrary number of {@code UrlAction}s for the {@code UrlHandler} to
         * support.
         * @return A {@link Builder} with the desired supported {@code UrlAction}s added.
         */
        public Builder withSupportedUrlActions(@NonNull final UrlAction first,
                @Nullable final UrlAction... others) {
            this.supportedUrlActions = EnumSet.of(first, others);
            return this;
        }

        /**
         * Sets the {@link UrlAction}s to support in the {@code UrlHandler} to build.
         *
         * @param supportedUrlActions An {@code EnumSet} of {@code UrlAction}s for the
         * {@code UrlHandler} to support.
         * @return A {@link Builder} with the desired supported {@code UrlAction}s added.
         */
        public Builder withSupportedUrlActions(
                @NonNull final EnumSet<UrlAction> supportedUrlActions) {
            this.supportedUrlActions = EnumSet.copyOf(supportedUrlActions);
            return this;
        }
        
        /**
         * Sets the {@link ResultActions} for the {@code UrlHandler} to
         * build.
         *
         * @param resultActions A {@code ClickListener} for the {@code UrlHandler}.
         * @return A {@link Builder} with the desired {@code ClickListener} added.
         */
        public Builder withResultActions(@NonNull final ResultActions resultActions) {
            this.resultActions = resultActions;
            return this;
        }

        /**
         * Sets the {@link CDAdSchemeListener} for the {@code UrlHandler} to build.
         *
         * @param cdAdsSchemeListener A {@code CDAdSchemeListener} for the {@code UrlHandler}.
         * @return A {@link Builder} with the desired {@code CDAdSchemeListener} added.
         */
        public Builder withCDAdSchemeListener(
                @NonNull final CDAdSchemeListener cdAdsSchemeListener) {
            this.cdAdsSchemeListener = cdAdsSchemeListener;
            return this;
        }

        /**
         * If called, will avoid starting a {@link CDAdBrowser} activity where applicable.
         * (see {@link Intents#showCDAdBrowserForUrl(Context, Uri, String)})
         *
         * @return A {@link Builder} that will skip starting a {@code CDAdBrowser}.
         */
        public Builder withoutCDAdBrowser() {
            this.skipShowCDAdBrowser = true;
            return this;
        }

        /**
         * Sets the creativeId for the ad associated with this URL
         *
         * @return A {@link Builder} that knows the creativeID for the ad.
         */
        public Builder withDspCreativeId(@Nullable final String creativeId) {
            this.creativeId = creativeId;
            return this;
        }

        /**
         * Creates an immutable {@link UrlHandler} with the desired configuration, according to the
         * other {@link Builder} methods called before.
         *
         * @return An immutable {@code UrlHandler} with the desired configuration.
         */
        public UrlHandler build() {
            return new UrlHandler(supportedUrlActions, resultActions, cdAdsSchemeListener,
                    skipShowCDAdBrowser, creativeId);
        }
    }

    private static final ResultActions EMPTY_CLICK_LISTENER = new ResultActions() {
        @Override
        public void urlHandlingSucceeded(@NonNull String url, @NonNull UrlAction urlAction) { }
        @Override
        public void urlHandlingFailed(@NonNull String url, @NonNull UrlAction lastFailedUrlAction) { }
    };

    private static final CDAdSchemeListener EMPTY_CDAD_SCHEME_LISTENER =
            new CDAdSchemeListener() {
        @Override public void onFinishLoad() { }

        @Override public void onClose() { }

        @Override public void onFailLoad() { }
    };

    @NonNull
    private EnumSet<UrlAction> mSupportedUrlActions;
    @NonNull
    private ResultActions mResultActions;
    @NonNull
    private CDAdSchemeListener mCDAdSchemeListener;
    @Nullable private String mDspCreativeId;
    private boolean mSkipShowCDAdBrowser;
    private boolean mAlreadySucceeded;
    private boolean mTaskPending;

    /**
     * Do not instantiate UrlHandler directly; use {@link Builder} instead.
     */
    private UrlHandler(
            @NonNull final EnumSet<UrlAction> supportedUrlActions,
            @NonNull final ResultActions resultActions,
            @NonNull final CDAdSchemeListener cdAdsSchemeListener,
            final boolean skipShowCDAdBrowser,
            @Nullable final String dspCreativeId) {
        mSupportedUrlActions = EnumSet.copyOf(supportedUrlActions);
        mResultActions = resultActions;
        mCDAdSchemeListener = cdAdsSchemeListener;
        mSkipShowCDAdBrowser = skipShowCDAdBrowser;
        mDspCreativeId = dspCreativeId;
        mAlreadySucceeded = false;
        mTaskPending = false;
    }

    @NonNull
    EnumSet<UrlAction> getSupportedUrlActions() {
        return EnumSet.copyOf(mSupportedUrlActions);
    }

    @NonNull
    ResultActions getResultActions() {
        return mResultActions;
    }

    @NonNull
    CDAdSchemeListener getCDAdSchemeListener() {
        return mCDAdSchemeListener;
    }

    boolean shouldSkipShowCDAdBrowser() {
        return mSkipShowCDAdBrowser;
    }

    /**
     * Performs the actual click handling by verifying that the {@code destinationUrl} is one of
     * the configured supported {@link UrlAction}s and then handling it accordingly.
     *
     * @param context The activity context.
     * @param destinationUrl The URL to handle.
     */
    public void handleUrl(@NonNull final Context context, @NonNull final String destinationUrl, final String clickAction) {
        Preconditions.checkNotNull(context);

        handleUrl(context, destinationUrl, true, clickAction);
    }

    /**
     * Performs the actual click handling by verifying that the {@code destinationUrl} is one of
     * the configured supported {@link UrlAction}s and then handling it accordingly.
     *
     * @param context The activity context.
     * @param destinationUrl The URL to handle.
     * @param fromUserInteraction Whether this handling was triggered from a user interaction.
     */
    public void handleUrl(@NonNull final Context context, @NonNull final String destinationUrl,
            final boolean fromUserInteraction, final String clickAction) {
        Preconditions.checkNotNull(context);

        handleUrl(context, destinationUrl, fromUserInteraction, null, clickAction);
    }

    /**
     * Follows any redirects from {@code destinationUrl} and then handles the URL accordingly.
     *
     * @param context The activity context.
     * @param destinationUrl The URL to handle.
     * @param fromUserInteraction Whether this handling was triggered from a user interaction.
     * @param trackingUrls Optional tracking URLs to trigger on success
     */
    public void handleUrl(@NonNull final Context context, @NonNull final String destinationUrl,
            final boolean fromUserInteraction, @Nullable final Iterable<String> trackingUrls, final String clickAction) {
        Preconditions.checkNotNull(context);

        if (TextUtils.isEmpty(destinationUrl)) {
            failUrlHandling(destinationUrl, null, "Attempted to handle empty url.", null);
            return;
        }

        final UrlResolutionListener urlResolutionListener = new UrlResolutionListener() {
            @Override
            public void onSuccess(@NonNull final String resolvedUrl) {
                mTaskPending = false;
                handleResolvedUrl(context, resolvedUrl, fromUserInteraction, trackingUrls, clickAction);
            }

            @Override
            public void onFailure(@NonNull final String message,
                    @Nullable final Throwable throwable) {
                mTaskPending = false;
                failUrlHandling(destinationUrl, null, message, throwable);

            }

        };

        UrlResolutionTask.getResolvedUrl(destinationUrl, urlResolutionListener, clickAction);
        mTaskPending = true;
    }

    /**
     * Performs the actual url handling by verifying that the {@code destinationUrl} is one of
     * the configured supported {@link UrlAction}s and then handling it accordingly.
     *
     * @param context The activity context.
     * @param url The URL to handle.
     * @param fromUserInteraction Whether this handling was triggered from a user interaction.
     * @param trackingUrls Optional tracking URLs to trigger on success
     * @return true if the given URL was successfully handled; false otherwise
     */
    public boolean handleResolvedUrl(@NonNull final Context context,
            @NonNull final String url, final boolean fromUserInteraction,
            @Nullable Iterable<String> trackingUrls, final String clickAction) {
        if (TextUtils.isEmpty(url)) {
            failUrlHandling(url, null, "Attempted to handle empty url.", null);
            return false;
        }

        UrlAction lastFailedUrlAction = UrlAction.NOOP;
        final Uri destinationUri = Uri.parse(url);

        for (final UrlAction urlAction : mSupportedUrlActions) {
            if (urlAction.shouldTryHandlingUrl(destinationUri, clickAction)) {
                try {
                    urlAction.handleUrl(UrlHandler.this, context, destinationUri,
                            fromUserInteraction, mDspCreativeId, clickAction);
                    if (!mAlreadySucceeded && !mTaskPending
                            && !UrlAction.IGNORE_ABOUT_SCHEME.equals(urlAction)
                            && !UrlAction.HANDLE_CDAD_SCHEME.equals(urlAction)) {
//                        makeTrackingHttpRequest(trackingUrls, context,
//                                BaseEvent.Name.CLICK_REQUEST);
                        mResultActions.urlHandlingSucceeded(destinationUri.toString(),
                                urlAction);
                        mAlreadySucceeded = true;
                    }
                    return true;
                } catch (IntentNotResolvableException e) {
                        Utils.logStackTrace(e);
                    CDAdLog.d(e.getMessage(), e);
                    lastFailedUrlAction = urlAction;
                    // continue trying to match...
                }
            }
        }
        failUrlHandling(url, lastFailedUrlAction, "Link ignored. Unable to handle url: " + url, null);
        return false;
    }

    private void failUrlHandling(@Nullable final String url, @Nullable UrlAction urlAction,
            @NonNull final String message, @Nullable final Throwable throwable) {
        Preconditions.checkNotNull(message);

        if (urlAction == null) {
            urlAction = UrlAction.NOOP;
        }

        CDAdLog.d(message, throwable);
        mResultActions.urlHandlingFailed(url, urlAction);
    }

}
