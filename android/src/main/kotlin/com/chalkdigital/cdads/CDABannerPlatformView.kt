package com.chalkdigital.cdads

import android.content.Context
import android.view.View
import com.chalkdigital.cdads.banner.CDABannerView
import io.flutter.plugin.platform.PlatformView

/**
 * Wraps a [CDABannerView] for Flutter PlatformView embedding.
 * Creation params must contain `adUnitId: String`.
 */
internal class CDABannerPlatformView(
    context: Context,
    private val adUnitId: String,
) : PlatformView {

    internal val bannerView: CDABannerView = CDABannerView(context)

    init {
        CDABannerPlatformViewRegistry.register(adUnitId, this)
    }

    override fun getView(): View = bannerView

    override fun dispose() {
        bannerView.destroy()
        CDABannerPlatformViewRegistry.remove(adUnitId)
    }
}
