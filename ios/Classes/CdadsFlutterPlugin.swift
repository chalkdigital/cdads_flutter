import Chalkdigital_Mobile_Ads_SDK
import Flutter
import UIKit

public class CdadsFlutterPlugin: NSObject, FlutterPlugin, CDadsDelegate {

  public static var launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
  public static var cdInitialisationParams: CDInitialisationParams =
    CDInitialisationParams()

  public static func register(with registrar: FlutterPluginRegistrar) {
    if let options = launchOptions {
      // Use options here, safely
      print("Launch options: \(options)")
      // e.g., MySDK.start(with: options)
    }
    NotificationCenter.default.addObserver(
      self,
      selector: #selector(applicationDidFinishLaunching),
      name: UIApplication.didFinishLaunchingNotification,
      object: nil
    )
    let channel = FlutterMethodChannel(
      name: "cdads_flutter", binaryMessenger: registrar.messenger())
    let instance = CdadsFlutterPlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
    let factory = CDAdPlatformViewFactory(messenger: registrar.messenger())
    registrar.register(factory, withId: "cdads_ad_view")
  }

  @objc static func applicationDidFinishLaunching(_ notification: Notification) {
    // let launchOptions = notification.object as? [UIApplication.LaunchOptionsKey: Any];
    // CDAds.initialise(
    //   with: cdInitialisationParams, launchOptions: launchOptions, enableTracking: true)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    switch call.method {
    case "getPlatformVersion":
      result("iOS " + UIDevice.current.systemVersion)
    case "initialize":
      var cdInitialisationParams: CDInitialisationParams =
        CDInitialisationParams()
      if let args = call.arguments as? [String: Any] {
        cdInitialisationParams.appName = args["appName"] as? String ?? ""
        cdInitialisationParams.appKey = args["appKey"] as? String ?? ""
        cdInitialisationParams.secretKey = args["secretKey"] as? String ?? ""
        cdInitialisationParams.host = args["host"] as? String ?? ""
        cdInitialisationParams.partnerKey = args["partnerKey"] as? String ?? ""
        cdInitialisationParams.applicationIABCategory =
          args["applicationIABCategory"] as? String ?? ""
        cdInitialisationParams.distanceFilter = args["distanceFilter"] as? Double ?? 100
        cdInitialisationParams.locationUpdateInterval =
          args["locationUpdateInterval"] as? Double ?? 30
        cdInitialisationParams.adLocationExpiryInterval =
          args["adLocationExpiryInterval"] as? Double ?? 300
        let logLevelValue = args["logLevel"] as? UInt32 ?? 0
        switch logLevelValue {
        case CDLogLevelDebug.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelDebug
        case CDLogLevelInfo.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelInfo
        case CDLogLevelWarn.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelWarn
        case CDLogLevelError.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelError
        case CDLogLevelAll.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelAll
        case CDLogLevelOff.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelOff
        case CDLogLevelFatal.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelFatal
        case CDLogLevelTrace.rawValue:
          cdInitialisationParams.logLevel = CDLogLevelTrace
        default:
          cdInitialisationParams.logLevel = CDLogLevelInfo
        }
        let providerStringValue = args["provider"] as? UInt32 ?? 0
        switch providerStringValue {
        case CDADProviderChalk.rawValue:
          cdInitialisationParams.provider = CDADProviderChalk
        case CDADProviderGoogle.rawValue:
          cdInitialisationParams.provider = CDADProviderGoogle
        case CDADProviderAdMarvel.rawValue:
          cdInitialisationParams.provider = CDADProviderAdMarvel
        case CDADProviderAdColonyAurora.rawValue:
          cdInitialisationParams.provider = CDADProviderAdColonyAurora
        case CDADProviderAmazon.rawValue:
          cdInitialisationParams.provider = CDADProviderAmazon
        case CDADProviderChartboost.rawValue:
          cdInitialisationParams.provider = CDADProviderChartboost
        case CDADProviderFacebook.rawValue:
          cdInitialisationParams.provider = CDADProviderFacebook
        case CDADProviderInMobi.rawValue:
          cdInitialisationParams.provider = CDADProviderInMobi
        case CDADProviderHeyzap.rawValue:
          cdInitialisationParams.provider = CDADProviderHeyzap
        case CDADProviderMillenial.rawValue:
          cdInitialisationParams.provider = CDADProviderMillenial
        case CDADProviderVungle.rawValue:
          cdInitialisationParams.provider = CDADProviderVungle
        case CDADProviderVurve.rawValue:
          cdInitialisationParams.provider = CDADProviderVurve
        case CDADProviderUnityAds.rawValue:
          cdInitialisationParams.provider = CDADProviderUnityAds
        case CDADProviderYuMe.rawValue:
          cdInitialisationParams.provider = CDADProviderYuMe
        default:
          cdInitialisationParams.provider = CDADProviderChalk
        }
        let environmentStringValue = args["environment"] as? UInt32 ?? 0
        switch environmentStringValue {
        case CDEnvironmentProduction.rawValue:
          cdInitialisationParams.environment = CDEnvironmentProduction
        case CDEnvironmentTest.rawValue:
          cdInitialisationParams.environment = CDEnvironmentTest
        case CDEnvironmentTest.rawValue:
          cdInitialisationParams.environment = CDEnvironmentTest
        default:
          cdInitialisationParams.environment = CDEnvironmentTest
        }
        cdInitialisationParams.showTrackingTerms = args["showTrackingTerms"] as? Bool ?? false
        cdInitialisationParams.gdpr = args["gdpr"] as? Bool ?? false
        cdInitialisationParams.consent = args["consent"] as? Bool ?? false
        // cdInitialisationParams.geoIpLocationEnable = args["geoIpLocationEnable"] as? Bool ?? false
        cdInitialisationParams.clientHasUserTrackingPermission =
          args["clientHasUserTrackingPermission"] as? Bool ?? false
        CdadsFlutterPlugin.cdInitialisationParams = cdInitialisationParams
        DispatchQueue.main.async {
          CDAds.initialise(
            with: CdadsFlutterPlugin.cdInitialisationParams,
            launchOptions: CdadsFlutterPlugin.launchOptions, enableTracking: true)
        }
      }
      result(nil)
    default:
      result(FlutterMethodNotImplemented)
    }
  }
}
