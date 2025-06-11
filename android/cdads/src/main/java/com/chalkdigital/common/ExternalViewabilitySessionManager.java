package com.chalkdigital.common;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;

import com.chalkdigital.common.logging.CDAdLog;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates all third-party viewability session measurements.
 */
public class ExternalViewabilitySessionManager {

    @NonNull private final Set<ExternalViewabilitySession> mViewabilitySessions;

    public enum ViewabilityVendor {
        AVID, MOAT, CDAD, OMSDK, ALL;

        public void disable() {
            switch (this) {
                case AVID:
                    AvidViewabilitySession.disable();
                    break;
                case MOAT:
                    MoatViewabilitySession.disable();
                    break;
                case OMSDK:
//                    OmViewabilitySession.disable();
                    break;
                case CDAD:
                    CDAdViewabilitySession.disable();
                    break;
                case ALL:
                    AvidViewabilitySession.disable();
                    MoatViewabilitySession.disable();
                    CDAdViewabilitySession.disable();
                    break;
                default:
                    CDAdLog.d("Attempted to disable an invalid viewability vendor: " + this);
                    return;
            }
            CDAdLog.d("Disabled viewability for " + this);
        }

        /**
         * @link { AdUrlGenerator#VIEWABILITY_KEY }
         */
        @NonNull
        public static String getEnabledVendorKey() {
            final boolean avidEnabled = AvidViewabilitySession.isEnabled();
            final boolean moatEnabled = MoatViewabilitySession.isEnabled();
            final boolean cdAdViewabilityEnabled = CDAdViewabilitySession.isEnabled();

            String vendorKey = "0";
            if (avidEnabled && moatEnabled && cdAdViewabilityEnabled) {
                vendorKey = "7";
            }
            else if (avidEnabled && moatEnabled) {
                vendorKey = "6";
            }
            else if (moatEnabled && cdAdViewabilityEnabled) {
                vendorKey = "5";
            }
            else if (avidEnabled && cdAdViewabilityEnabled) {
                vendorKey = "4";
            }
            else if (cdAdViewabilityEnabled) {
                vendorKey = "3";
            }
            else if (avidEnabled) {
                vendorKey = "1";
            } else if (moatEnabled) {
                vendorKey = "2";
            }

            return vendorKey;
        }

        @Nullable
        public static ViewabilityVendor fromKey(@NonNull final String key) {
            Preconditions.checkNotNull(key);

            switch (key) {
                case "1":
                    return AVID;
                case "2":
                    return MOAT;
                case "3":
                    return CDAD;
                case "4":
                    return ALL;
                default:
                    return null;
            }
        }
    }

    public ExternalViewabilitySessionManager(@NonNull final Context context) {
        Preconditions.checkNotNull(context);

        mViewabilitySessions = new HashSet<ExternalViewabilitySession>();
        mViewabilitySessions.add(new AvidViewabilitySession());
        mViewabilitySessions.add(new MoatViewabilitySession());
        mViewabilitySessions.add(new CDAdViewabilitySession());
//        mViewabilitySessions.add(new OmViewabilitySession());

        initialize(context);
    }

    /**
     * Allow the viewability session to perform any necessary initialization. Each session
     * must handle any relevant caching or lazy loading independently.
     *
     * @param context Preferably Activity Context. Currently only used to obtain a reference to the
     *                Application required by some viewability vendors.
     */
    private void initialize(@NonNull final Context context) {
        Preconditions.checkNotNull(context);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.initialize(context);
            logEvent(session, "initialize", successful, false);
        }
    }

    /**
     * Perform any necessary clean-up and release of resources.
     */
    public void invalidate() {
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.invalidate();
            logEvent(session, "invalidate", successful, false);
        }
    }

    /**
     * Registers and starts viewability tracking for the given WebView.
     * @param context Preferably an Activity Context.
     * @param webView The WebView to be tracked.
     * @param isDeferred True for cached ads (i.e. interstitials)
     */
    public void createDisplaySession(@NonNull final Context context,
            @NonNull final WebView webView, boolean isDeferred) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(webView);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.createDisplaySession(context, webView, isDeferred);
            logEvent(session, "start display session", successful, true);
        }
    }

    public void createDisplaySession(@NonNull final Context context,
            @NonNull final WebView webview) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(webview);

        createDisplaySession(context, webview, false);
    }

    /**
     * Begins deferred impression tracking. For cached ads (i.e. interstitials) this should be
     * called separately from {@link ExternalViewabilitySessionManager#createDisplaySession(Context, WebView)}.
     * @param activity
     */
    public void startDeferredDisplaySession(@NonNull final Activity activity) {
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.startDeferredDisplaySession(activity);
            logEvent(session, "record deferred session", successful, true);
        }
    }

    /**
     * Unregisters and disables all viewability tracking for the given WebView.
     */
    public void endDisplaySession() {
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.endDisplaySession();
            logEvent(session, "end display session", successful, true);
        }
    }

//    /**
//     * Registers and starts video viewability tracking for the given View.
//     *
//     * @param activity An Activity Context.
//     * @param view The player View.
//     * @param vastVideoConfig Configuration file used to store video viewability tracking tags.
//     */
    public void createVideoSession(@NonNull final Activity activity, @NonNull final View view,
            @NonNull final Map<String, String > externalTrackers) {
        Preconditions.checkNotNull(activity);
        Preconditions.checkNotNull(view);
        Preconditions.checkNotNull(externalTrackers);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Set<String> buyerResources = new HashSet<String>();
            if (session instanceof AvidViewabilitySession) {
//                buyerResources.addAll(vastVideoConfig.getAvidJavascriptResources());
            } else if (session instanceof MoatViewabilitySession) {
//                buyerResources.addAll(vastVideoConfig.getMoatImpressionPixels());
            } else{

            }

            final Boolean successful = session.createVideoSession(activity, view, buyerResources,
                    externalTrackers);
            logEvent(session, "start video session", successful, true);
        }
    }

    /**
     * Prevents friendly obstructions from affecting viewability scores.
     *
     * @param views Views in the same Window and a higher z-index as the video playing.
     */
    public void registerVideoObstructions(@NonNull final List<View> views) {
        Preconditions.checkNotNull(views);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.registerVideoObstructions(views);
            logEvent(session, "register friendly obstruction", successful, true);
        }
    }

    public void onVideoPrepared(@NonNull final View playerView, final Integer duration, final Integer volume) {
        Preconditions.checkNotNull(playerView);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.onVideoPrepared(playerView, duration, volume);
            logEvent(session, "on video prepared", successful, true);
        }
    }

    /**
     * Notify pertinent video lifecycle events (e.g. MediaPlayer onPrepared, first quartile fired).
     *
     * @param event Corresponding {@link ExternalViewabilitySession.VideoEvent}.
     * @param playHeadMillis Current video playhead, in milliseconds.
     */
    public void recordVideoEvent(@NonNull final ExternalViewabilitySession.VideoEvent event,
                                 final Integer playHeadMillis) {
        Preconditions.checkNotNull(event);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.recordVideoEvent(event,null, playHeadMillis, null);
            logEvent(session, "record video event (" + event.name() + ")", successful, true);
        }
    }

    /**
     * Notify pertinent video lifecycle events (e.g. MediaPlayer onPrepared, first quartile fired).
     *
     * @param event Corresponding {@link ExternalViewabilitySession.VideoEvent}.
     * @param duration Current video duration, in milliseconds.
     * @param playHeadMillis Current video playhead, in milliseconds.
     * @param volume Current video volume.
     */
    public void recordVideoEvent(@NonNull final ExternalViewabilitySession.VideoEvent event, final Integer duration,
            final Integer playHeadMillis, final Integer volume) {
        Preconditions.checkNotNull(event);

        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.recordVideoEvent(event,duration, playHeadMillis, volume);
            logEvent(session, "record video event (" + event.name() + ")", successful, true);
        }
    }

    /**
     * Unregisters and disables all viewability tracking for the given View.
     */
    public void endVideoSession() {
        for (final ExternalViewabilitySession session : mViewabilitySessions) {
            final Boolean successful = session.endVideoSession();
            logEvent(session, "end video session", successful, true);
        }
    }

    private void logEvent(@NonNull final ExternalViewabilitySession session,
            @NonNull final String event,
            @Nullable final Boolean successful,
            final boolean isVerbose) {
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(event);

        if (successful == null) {
            // Method return values are only null when the corresponding viewability vendor has been
            // disabled. Do not log in those cases.
            return;
        }

        final String failureString = successful ? "" : "failed to ";
        final String message = String.format(Locale.US, "%s viewability event: %s%s.",
                session.getName(), failureString, event);
        if (isVerbose) {
            CDAdLog.v(message);
        } else {
            CDAdLog.d(message);
        }
    }
}
