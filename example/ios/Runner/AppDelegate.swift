// import cdads
import Flutter
import Pods_Runner
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
    // CdadsFlutterPlugin.launchOptions = launchOptions
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
