import Chalkdigital_Mobile_Ads_SDK
import Flutter
import Foundation

class CDAdPlatformView: NSObject, FlutterPlatformView, CDAdViewDelegate {
    private var containerView: UIView
    private var adView: CDAdView
    private var channel: FlutterMethodChannel
    private var placementId: String?
    private var partnerId: String?
    private var isLocationAutoUpdateEnabled: Bool = true
    private var isAdAutoRefreshEnabled: Bool = true
    private var isTesting: Bool = false
    private var isCloseable: Bool = true
    private var useInAppBrowser: Bool = true
    private var cdGeoInfo: CDGeoInfo?

    init(
        frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?,
        messenger: FlutterBinaryMessenger
    ) {

        self.containerView = UIView(
            frame: CGRect(x: 0, y: 0, width: frame.width + 32, height: frame.height + 16))
        self.containerView.backgroundColor = .clear
        self.adView = CDAdView(frame: frame)
        self.containerView.addSubview(adView)
        self.adView.translatesAutoresizingMaskIntoConstraints = false

        NSLayoutConstraint.activate([
            self.adView.topAnchor.constraint(equalTo: containerView.topAnchor, constant: 16),
            self.adView.leadingAnchor.constraint(
                equalTo: containerView.leadingAnchor, constant: 16),
            self.adView.trailingAnchor.constraint(
                equalTo: containerView.trailingAnchor, constant: -16),
        ])
        self.channel = FlutterMethodChannel(
            name: "cdads_ad_view_\(viewId)", binaryMessenger: messenger)
        super.init()

        channel.setMethodCallHandler { [weak self] call, result in
            guard let self = self else { return }

            switch call.method {
            case "loadAd":
                if let args = call.arguments as? [String: Any] {
                    if let placementId = args["placementId"] as? String {
                        self.placementId = placementId
                    }

                    if let partnerId = args["partnerId"] as? String {
                        self.partnerId = partnerId
                    }

                    if let locationEnabled = args["isLocationAutoUpdateEnabled"] as? Bool {
                        self.isLocationAutoUpdateEnabled = locationEnabled
                    }

                    if let refreshEnabled = args["isAdAutoRefreshEnabled"] as? Bool {
                        self.isAdAutoRefreshEnabled = refreshEnabled
                    }

                    if let testing = args["isTesting"] as? Bool {
                        self.isTesting = testing
                    }
                    if let closeable = args["isCloseable"] as? Bool {
                        self.isCloseable = closeable
                    }
                    if let useInAppBrowser = args["useInAppBrowser"] as? Bool {
                        self.useInAppBrowser = useInAppBrowser
                    }

                    if let cdGeoMap = args["geoInfo"] as? [String: Any] {
                        let cdGeoInfo = CDGeoInfo()

                        cdGeoInfo.lat = (cdGeoMap["lat"] as? NSNumber)?.floatValue ?? 0.0
                        cdGeoInfo.lon = (cdGeoMap["lon"] as? NSNumber)?.floatValue ?? 0.0
                        cdGeoInfo.type = (cdGeoMap["lon"] as? NSNumber)?.integerValue ?? 0.0
                        cdGeoInfo.countryCode = cdGeoMap["countryCode"] as? String ?? ""
                        cdGeoInfo.region = cdGeoMap["region"] as? String ?? ""
                        cdGeoInfo.city = cdGeoMap["city"] as? String ?? ""
                        cdGeoInfo.zip = cdGeoMap["zip"] as? String ?? ""
                        cdGeoInfo.streetAddress = cdGeoMap["streetAddress"] as? String ?? ""
                        cdGeoInfo.subThoroughFare = cdGeoMap["subThoroughFare"] as? String ?? ""
                        cdGeoInfo.haccuracy = cdGeoMap["haccuracy"] as? String ?? ""
                    }
                }

                self.adView.getAdWithNotification()
                result(nil)

            default:
                result(FlutterMethodNotImplemented)
            }
        }

        self.adView.delegate = self
        self.adView.getAdWithNotification()  // Start ad load
    }

    func view() -> UIView {
        return adView
    }

    // MARK: - CDAdViewDelegate required methods
    func placementId(_ cdAdView: CDAdView!) -> String! {
        return self.placementId ?? "0"
    }

    func partnerId(_ cdAdView: CDAdView!) -> String! {
        return self.partnerId ?? ""
    }

    func locationServicesEnabled(_ cdAdView: CDAdView!) -> Bool {
        return self.isLocationAutoUpdateEnabled
    }
    func allowAutomaticRefreshAds(_ cdAdView: CDAdView!) -> Bool {
        return self.isAdAutoRefreshEnabled
    }

    func closeable(_ cdAdView: CDAdView!) -> Bool {
        return self.isCloseable
    }

    func use(inAppBrowser cdAdView: CDAdView!) -> Bool {
        return self.useInAppBrowser
    }

    func cdAdViewFrame(_ cdAdView: CDAdView!) -> CGRect {
        print(adView.frame.debugDescription)
        return adView.frame
    }

    func cdAdViewSize(_ cdAdView: CDAdView!) -> CDAdSize {
        return CDAdSize(size: adView.frame.size)
    }

    func locationServicesEnabled(_ cdAdView: CDAdView!) -> Bool {
        return true
    }

    func use(inAppBrowser cdAdView: CDAdView!) -> Bool {
        return true
    }

    func applicationUIViewController(_ cdAdView: CDAdView!) -> UIViewController! {
        // Use root view controller
        return UIApplication.shared.delegate?.window??.rootViewController
    }

    // MARK: - Optional delegate callbacks sent to Flutter
    func getAdSucceeded(_ cdAdView: CDAdView!) {
        channel.invokeMethod("onAdLoaded", arguments: nil)
    }

    func getAdFailed(_ cdAdView: CDAdView!) {
        channel.invokeMethod("onAdFailed", arguments: nil)
    }

    func adViewDidReceiveTapEvent(_ cdAdView: CDAdView!) {
        channel.invokeMethod("onAdTapped", arguments: nil)
    }

    func interstitialClosed(_ cdAdView: CDAdView!) {
        channel.invokeMethod("onInterstitialClosed", arguments: nil)
    }

    // Add other callbacks as needed...
}
