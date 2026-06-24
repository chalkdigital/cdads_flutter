// Public API — import this single file in your Flutter app.
export 'src/cdads.dart';
export 'src/cdads_banner_view.dart';
export 'src/cdads_banner_widget.dart';
export 'src/cdads_interstitial_ad.dart';
export 'src/cdads_rewarded_video_ad.dart';
export 'src/cdads_native_ad_manager.dart';
export 'src/cdads_event_handler.dart' show CDAdsEventHandler;
export 'src/cdads_events.dart';
export 'src/cdads_debug_trigger.dart';

// Re-export Pigeon data types so consumers don't need to import generated files.
export 'src/generated/cdads_api.g.dart'
    show
        CDAdsConfig,
        CDAdsRequest,
        CDAdsGeoInfo,
        CDAdsNativeAdData,
        CDAdsReward,
        CDAdsError,
        CDAdsLogLevel,
        CDAdsErrorCode,
        CDAdsSize;
