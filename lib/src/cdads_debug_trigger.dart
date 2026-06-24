/// Matches the legacy Tempo "secret code" used to reveal the in-app debug log
/// viewer — historically, typing `*##*` into the app's search bar would show
/// a hidden console. Wire this into whatever text-entry callback the host app
/// already has and call `CDAds.showDebugLogViewer()` on a match.
///
/// ```dart
/// TextField(
///   onChanged: (text) {
///     if (CDAdsDebugTrigger.matches(text)) {
///       CDAds.showDebugLogViewer();
///     }
///   },
/// )
/// ```
abstract final class CDAdsDebugTrigger {
  /// The legacy reveal code.
  static const String magicCode = '*##*';

  /// Returns `true` once [text] starts with the magic code.
  static bool matches(String text) => text.startsWith(magicCode);
}
