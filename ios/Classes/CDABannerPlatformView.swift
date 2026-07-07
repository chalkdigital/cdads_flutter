import Flutter
import UIKit
import CDAds

/// FlutterPlatformView wrapping CDABannerView from the CDAds Swift SDK.
///
/// Creation params from Dart (all construction-time settings — CDABannerView's
/// size is init-only, so these can't be applied later via loadBanner()):
///   {
///     "adUnitId": String,
///     "sizePreset": String,       // CDAdsSize enum name, e.g. "banner300x250" or "custom"
///     "width": Double, "height": Double,   // used as the custom size when sizePreset == "custom"
///     "showCloseButton": Bool,
///     "isAutoRefreshEnabled": Bool,
///     "refreshInterval": Double,  // seconds
///   }
///
/// The view registers itself in the shared registry so that CDAdsHostApiImpl
/// can find it by adUnitId when loadBanner() is called from Dart.
@MainActor
final class CDABannerPlatformView: NSObject, FlutterPlatformView {

    // Shared registry: adUnitId → view
    // Populated on creation, removed on destroy().
    static var registry: [String: CDABannerView] = [:]

    // Native UIKit doesn't clip a view's subviews to its bounds by default, so
    // CDABannerView's close button (which straddles its top-left corner — half
    // inside, half outside its own frame) is visible as-is in a plain UIKit host.
    // Flutter's platform-view embedding wraps the returned view in its own
    // container that *does* clip to exactly this widget's allocated rect, which
    // silently cuts off the overhanging half of the button. Fix: reserve an
    // extra margin around bannerView and return a wrapper containing it inset by
    // that margin, so the button's absolute position ends up at the wrapper's
    // (0,0) corner — fully inside the wrapper's bounds instead of overhanging it.
    // The Dart widget grows the SizedBox/UiKitView by the same margin so Flutter
    // actually allocates the extra space (see cdads_banner_widget.dart).
    private static let closeButtonMargin: CGFloat = 25

    private let bannerView: CDABannerView
    private let containerView: UIView
    private let adUnitId: String

    init(frame: CGRect, viewId: Int64, args: Any?, messenger: FlutterBinaryMessenger) {
        // Parse creation params
        let params     = args as? [String: Any]
        let adUnitId   = params?["adUnitId"] as? String ?? ""
        let sizePreset = params?["sizePreset"] as? String ?? "custom"
        let width      = params?["width"]    as? Double ?? Double(frame.width)
        let height     = params?["height"]   as? Double ?? Double(frame.height)
        let showCloseButton = params?["showCloseButton"] as? Bool ?? false

        self.adUnitId  = adUnitId
        let banner = CDABannerView(size: Self.size(forPreset: sizePreset, width: width, height: height))
        self.bannerView = banner

        let margin = showCloseButton ? Self.closeButtonMargin : 0
        let container = UIView(frame: CGRect(x: 0, y: 0, width: width + margin, height: height + margin))
        container.backgroundColor = .clear
        banner.frame = CGRect(x: margin, y: margin, width: width, height: height)
        container.addSubview(banner)
        self.containerView = container

        super.init()

        bannerView.showCloseButton = showCloseButton
        if let isAutoRefreshEnabled = params?["isAutoRefreshEnabled"] as? Bool {
            bannerView.isAutoRefreshEnabled = isAutoRefreshEnabled
        }
        if let refreshInterval = params?["refreshInterval"] as? Double {
            bannerView.refreshInterval = refreshInterval
        }
        if let behaviourStr = params?["landingPageBehaviour"] as? String {
            bannerView.landingPageBehaviour = behaviourStr == "deviceBrowser" ? .deviceBrowser : .inAppBrowser
        }

        // Register so the bridge can find this view when loadBanner() arrives
        Self.registry[adUnitId] = bannerView
    }

    func view() -> UIView { containerView }

    deinit {
        // deinit runs nonisolated even though this class is @MainActor —
        // PlatformViews are always torn down on the main thread in practice.
        MainActor.assumeIsolated {
            Self.registry.removeValue(forKey: self.adUnitId)
        }
    }

    private static func size(forPreset preset: String, width: Double, height: Double) -> CDABannerView.CDABannerSize {
        switch preset {
        case "banner320x50":    return .banner320x50
        case "banner300x50":    return .banner300x50
        case "banner300x250":   return .banner300x250
        case "banner320x100":   return .banner320x100
        case "banner728x90":    return .banner728x90
        case "banner728x250":   return .banner728x250
        case "banner300x600":   return .banner300x600
        case "banner970x250":   return .banner970x250
        case "banner480x320":   return .banner480x320
        case "banner320x480":   return .banner320x480
        case "banner768x1024":  return .banner768x1024
        case "banner1086x1086": return .banner1086x1086
        default:                return .custom(width: width, height: height)
        }
    }
}
