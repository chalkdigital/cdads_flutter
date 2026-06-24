import CDAds
import Flutter
import UIKit

/// Entry point registered by Flutter's plugin system.
/// Responsibility: wire Pigeon channels, register the banner PlatformView,
/// and arm CDAds' background task before the host app finishes launching.
/// All other business logic lives in CDAdsHostApiImpl.
public class CdadsFlutterPlugin: NSObject, FlutterPlugin {

    // Plugin registration always happens on the main thread in practice —
    // assumeIsolated lets us construct the @MainActor CDAdsHostApiImpl here
    // without making this nonisolated protocol requirement itself @MainActor.
    public static func register(with registrar: FlutterPluginRegistrar) {
        MainActor.assumeIsolated {
            // CDAds.initialize() registers a BGTaskScheduler launch handler,
            // which iOS requires to happen before
            // application(_:didFinishLaunchingWithOptions:) returns — too
            // early for the real config, which only arrives later via the
            // Dart-side CDAds.initialize() Pigeon call in CDAdsHostApiImpl.
            // Plugin registration happens synchronously within that same
            // launch window, so calling it here with bare defaults satisfies
            // the timing requirement without requiring any host-app
            // AppDelegate changes. CDAds.initialize(with:) applies the real,
            // later config to this already-running instance rather than
            // ignoring it (see CDAds.swift).
            CDAds.initialize(with: CDAdsConfiguration())

            let messenger = registrar.messenger()

            // 1. Wire Pigeon host API (Dart → Native)
            let hostApi = CDAdsHostApiImpl(messenger: messenger)
            CDAdsHostApiSetup.setUp(binaryMessenger: messenger, api: hostApi)

            // 2. Register banner PlatformView factory
            registrar.register(
                CDABannerPlatformViewFactory(messenger: messenger),
                withId: "cdads_flutter/banner"
            )
        }
    }
}
