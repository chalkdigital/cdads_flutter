import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.chalkdigital.ads.CDAdErrorCode
import com.chalkdigital.ads.CDAdView
import com.chalkdigital.ads.CDAdView.CDAdViewListener
import com.chalkdigital.common.CDAdGeoInfo
import com.chalkdigital.common.CDAdSize
import com.chalkdigital.common.CDAdsUtils
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView


class CDAdPlatformView(
    private val context: Context,
    viewId: Int,
    creationParams: Map<String, Any>,
    messenger: io.flutter.plugin.common.BinaryMessenger
) : PlatformView, CDAdViewListener {

    private val cdAdView: CDAdView = CDAdView(context)
    private val containerView: LinearLayout = LinearLayout(context);

    private val channel: MethodChannel = MethodChannel(messenger, "cdads_ad_view_$viewId")

    private var placementId: String = "0"
    private var partnerId: String = ""
    private var isLocationAutoUpdateEnabled: Boolean = true
    private var isAdAutoRefreshEnabled: Boolean = true
    private var isTesting: Boolean = false
    private var isCloseable: Boolean = true
    private var useInAppBrowser: Boolean = true

    init {
        containerView.setOrientation(LinearLayout.VERTICAL);
        // Set layout parameters or size if needed
        containerView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        cdAdView.layoutParams = layoutParams
        containerView.addView(cdAdView)
        cdAdView.cdAdViewListener = this
        @Suppress("UNCHECKED_CAST") val adSize : Map<String, Any> = creationParams["adSize"] as Map<String, Any>
        cdAdView.cdAdSize = CDAdSize.getCDAdSizeConstantFromSize(adSize["width"] as Int, adSize["height"] as Int);
        CDAdsUtils.setBrowserAgent(
            if (useInAppBrowser) CDAdsUtils.BrowserAgent.IN_APP else CDAdsUtils.BrowserAgent.NATIVE
        )

        channel.setMethodCallHandler { call, result ->
            when (call.method) {
                "loadAd" -> {
                    val args = call.arguments as? Map<*, *>
                    args?.let { map ->
                        placementId = map["placementId"] as? String ?: placementId
                        partnerId = map["partnerId"] as? String ?: partnerId
                        isLocationAutoUpdateEnabled = map["isLocationAutoUpdateEnabled"] as? Boolean ?: isLocationAutoUpdateEnabled
                        isAdAutoRefreshEnabled = map["isAdAutoRefreshEnabled"] as? Boolean ?: isAdAutoRefreshEnabled
                        isTesting = map["isTesting"] as? Boolean ?: isTesting
                        isCloseable = map["isCloseable"] as? Boolean ?: isCloseable
                        useInAppBrowser = map["useInAppBrowser"] as? Boolean ?: useInAppBrowser
                        val cdGeoInfo = CDAdGeoInfo()
                        val cdGeoMap = call.argument("geoInfo") as Map<String, Any>?
                        if(cdGeoMap != null && cdGeoMap.size>0){
                            cdGeoInfo.lat = (cdGeoMap["lat"] as Double).toFloat()
                            cdGeoInfo.lon = (cdGeoMap["lon"] as Double).toFloat()
                            cdGeoInfo.type = (cdGeoMap["type"] as Int).toInt()
                            cdGeoInfo.countryCode = cdGeoMap["countryCode"] as String?
                            cdGeoInfo.region = cdGeoMap["region"] as String?
                            cdGeoInfo.city = cdGeoMap["city"] as String?
                            cdGeoInfo.zip = cdGeoMap["zip"] as String?
                            cdGeoInfo.accuracy = cdGeoMap["haccuracy"] as String?
                            cdGeoInfo.time = System.currentTimeMillis()
                            cdAdView.setCDAdGeoInfo(cdGeoInfo)
                        }
                        cdAdView.isLocationAutoUpdateEnabled = isLocationAutoUpdateEnabled
                        cdAdView.setAdAutoRefreshEnabled(isAdAutoRefreshEnabled)
                        cdAdView.setTesting(isTesting)

                    }
                    // Trigger ad load
                    cdAdView.requestNewAd(HashMap(), partnerId, placementId)
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
//        cdAdView.requestNewAd(HashMap(),"chalkboard", "" )

    }

    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    override fun getView(): View = containerView

    override fun dispose() {
        cdAdView.destroy() // If your SDK has a destroy method
    }

    override fun onBannerAdRequest(banner: CDAdView?) {

    }

    override fun onBannerLoaded(banner: CDAdView?) {
        channel.invokeMethod("onAdLoaded", null)
    }

    override fun onBannerFailed(banner: CDAdView?, errorCode: CDAdErrorCode?) {
//        val errorMap = mapOf("errorCode" to (errorCode?.name ?: "UNKNOWN"))
        channel.invokeMethod("onAdFailed", null)
    }

    override fun onBannerClicked(banner: CDAdView?) {
        channel.invokeMethod("onAdTapped", null)
    }

    override fun onBannerExpanded(banner: CDAdView?) {

    }

    override fun onBannerCollapsed(banner: CDAdView?) {

    }
}
