package com.chalkdigital.cdads

import android.content.Context
import com.chalkdigital.cdads.banner.CDABannerView
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import kotlin.math.roundToInt

/**
 * Creation params from Dart (all construction-time settings — applied once,
 * here, rather than at loadBanner() time):
 *   {
 *     "adUnitId": String,
 *     "sizePreset": String,       // CDAdsSize enum name, e.g. "banner300x250" or "custom"
 *     "width": Double, "height": Double,   // density-independent px; used as the
 *                                            // custom size when sizePreset == "custom"
 *     "showCloseButton": Boolean,
 *     "isAutoRefreshEnabled": Boolean,
 *     "refreshInterval": Double,  // seconds
 *   }
 */
internal class CDABannerPlatformViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        @Suppress("UNCHECKED_CAST")
        val params = args as? Map<String, Any> ?: emptyMap()
        val adUnitId = params["adUnitId"] as? String ?: ""
        val sizePreset = params["sizePreset"] as? String ?: "custom"
        val density = context.resources.displayMetrics.density
        val widthPx = (params["width"] as? Number)?.toDouble() ?: 0.0
        val heightPx = (params["height"] as? Number)?.toDouble() ?: 0.0

        val view = CDABannerPlatformView(context, adUnitId)
        sizeFor(sizePreset)?.let { view.bannerView.adSize = it }
            ?: run { view.bannerView.customSizeDp = (widthPx / density).roundToInt() to (heightPx / density).roundToInt() }
        (params["showCloseButton"] as? Boolean)?.let { view.bannerView.showCloseButton = it }
        (params["isAutoRefreshEnabled"] as? Boolean)?.let { view.bannerView.isAutoRefreshEnabled = it }
        (params["refreshInterval"] as? Number)?.let { view.bannerView.refreshIntervalMs = (it.toDouble() * 1000).toLong() }
        return view
    }

    private fun sizeFor(preset: String): CDABannerView.Size? = when (preset) {
        "banner320x50"    -> CDABannerView.Size.BANNER_320x50
        "banner300x50"    -> CDABannerView.Size.BANNER_300x50
        "banner300x250"   -> CDABannerView.Size.BANNER_300x250
        "banner320x100"   -> CDABannerView.Size.BANNER_320x100
        "banner728x90"    -> CDABannerView.Size.BANNER_728x90
        "banner728x250"   -> CDABannerView.Size.BANNER_728x250
        "banner300x600"   -> CDABannerView.Size.BANNER_300x600
        "banner970x250"   -> CDABannerView.Size.BANNER_970x250
        "banner480x320"   -> CDABannerView.Size.BANNER_480x320
        "banner320x480"   -> CDABannerView.Size.BANNER_320x480
        "banner768x1024"  -> CDABannerView.Size.BANNER_768x1024
        "banner1086x1086" -> CDABannerView.Size.BANNER_1086x1086
        else              -> null
    }
}
