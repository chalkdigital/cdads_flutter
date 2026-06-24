package com.chalkdigital.cdads

import com.chalkdigital.cdads.banner.CDABannerView

/** Shared registry so CDAdsHostApiImpl can locate a CDABannerView by adUnitId. */
internal object CDABannerPlatformViewRegistry {
    private val map = mutableMapOf<String, CDABannerPlatformView>()

    internal fun register(adUnitId: String, view: CDABannerPlatformView) {
        map[adUnitId] = view
    }

    internal fun get(adUnitId: String): CDABannerPlatformView? = map[adUnitId]

    internal fun remove(adUnitId: String) {
        map.remove(adUnitId)
    }
}
