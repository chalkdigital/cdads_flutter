import 'generated/cdads_api.g.dart';
import 'cdads_event_handler.dart';

/// Main entry point for the CDAds Flutter plugin.
///
/// ```dart
/// await CDAds.initialize(CDAdsConfig(
///   partnerKey: 'YOUR_PARTNER_KEY',
///   host: 'https://ads.example.com',
///   appName: 'MyApp',
///   applicationIabCategory: 'IAB1',
/// ));
/// ```
class CDAds {
  CDAds._();

  static final _hostApi = CDAdsHostApi();

  /// Initialise the SDK. Call once before using any ad types.
  static Future<void> initialize(CDAdsConfig config) async {
    CDAdsEventHandler.setUp();
    await _hostApi.initialize(config);
  }

  /// Update GDPR consent state at runtime (e.g. after a consent dialog).
  static Future<void> updateConsent({
    required bool gdprApplies,
    required bool hasConsent,
  }) =>
      _hostApi.updateConsent(
          gdprApplies: gdprApplies, hasConsent: hasConsent);

  /// Toggle location tracking without re-initialising the SDK.
  static Future<void> setLocationEnabled(bool enabled) =>
      _hostApi.setLocationEnabled(enabled);

  // ── In-app debug log ──────────────────────────────────────────────────────
  // Modern equivalent of the legacy Tempo "*##*" debug console. See
  // CDAdsDebugTrigger for the magic-code matcher used to reveal it.

  /// Whether SDK log lines are being written to the in-app debug log. Defaults to false.
  /// Completely independent of [CDAdsConfig.logLevel], which only controls
  /// native-side log verbosity for development.
  static Future<bool> isDebugFileLoggingEnabled() =>
      _hostApi.isDebugFileLoggingEnabled();

  /// Toggles in-app debug log capture.
  static Future<void> setDebugFileLoggingEnabled(bool enabled) =>
      _hostApi.setDebugFileLoggingEnabled(enabled);

  /// The SDK's captured debug log text (oldest first).
  static Future<String> debugLogs() => _hostApi.debugLogs();

  /// Clears the in-app debug log buffer and its backing file.
  static Future<void> clearDebugLogs() => _hostApi.clearDebugLogs();

  /// Presents the native in-app debug log viewer on top of the current screen.
  static Future<void> showDebugLogViewer() => _hostApi.showDebugLogViewer();

  // Internal accessor used by ad classes
  static CDAdsHostApi get hostApi => _hostApi;
}
