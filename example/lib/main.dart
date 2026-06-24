import 'package:flutter/material.dart';
import 'package:cdads_flutter/cdads_flutter.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await CDAds.initialize(CDAdsConfig(
    partnerKey: 'YOUR_PARTNER_KEY',
    host: 'https://ads.example.com',
    appName: 'CDAdsExample',
    applicationIabCategory: 'IAB1',
    isTestEnvironment: true,
    logLevel: CDAdsLogLevel.debug,
    gdprApplies: false,
    hasConsent: true,
    enableTracking: true,
    enableLocationTracking: false,
    locationDistanceFilter: 100,
    locationUpdateInterval: 30,
    locationExpiryInterval: 300,
    clientHasUserTrackingPermission: false,
  ));

  runApp(const CDAdsExampleApp());
}

class CDAdsExampleApp extends StatelessWidget {
  const CDAdsExampleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CDAds Example',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const HomeScreen(),
    );
  }
}

// ── Home screen ───────────────────────────────────────────────────────────────

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _supportCodeController = TextEditingController();

  @override
  void dispose() {
    _supportCodeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('CDAds Example')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _NavTile('Banner Ads',        Icons.image,       const BannerScreen()),
          _NavTile('Interstitial Ad',   Icons.fullscreen,  const InterstitialScreen()),
          _NavTile('Rewarded Video',    Icons.videocam,    const RewardedScreen()),
          _NavTile('Native Ad',         Icons.article,     const NativeAdScreen()),
          _NavTile('Location Tracking', Icons.location_on, const LocationScreen()),
          const SizedBox(height: 32),
          // Type "*##*" here to reveal the SDK's in-app debug log viewer —
          // same reveal code Tempo used for its old console.
          TextField(
            controller: _supportCodeController,
            decoration: const InputDecoration(
              labelText: 'Support code',
              border: OutlineInputBorder(),
            ),
            onChanged: (text) {
              if (CDAdsDebugTrigger.matches(text)) {
                _supportCodeController.clear();
                CDAds.showDebugLogViewer();
              }
            },
          ),
        ],
      ),
    );
  }
}

class _NavTile extends StatelessWidget {
  const _NavTile(this.title, this.icon, this.destination);
  final String title;
  final IconData icon;
  final Widget destination;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: Icon(icon),
        title: Text(title),
        trailing: const Icon(Icons.chevron_right),
        onTap: () => Navigator.push(
          context, MaterialPageRoute(builder: (_) => destination)),
      ),
    );
  }
}

// ── Banner screen ─────────────────────────────────────────────────────────────
//
// Ads are only requested while the app is in the foreground. The SDK's
// PlatformView triggers a load as soon as it's created (and reloads on its own
// auto-refresh timer) — so the app-level way to enforce "foreground only" is to
// not have the banner PlatformViews in the tree at all while backgrounded, and
// let them re-create (and re-request) when the app resumes.

class BannerScreen extends StatefulWidget {
  const BannerScreen({super.key});

  @override
  State<BannerScreen> createState() => _BannerScreenState();
}

class _BannerScreenState extends State<BannerScreen> with WidgetsBindingObserver {
  bool _isForeground = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    final isForeground = state == AppLifecycleState.resumed;
    if (isForeground != _isForeground) {
      setState(() => _isForeground = isForeground);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Banner Ads')),
      body: ListView(
        children: [
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text('320×50 Banner — close button + 15s auto-refresh',
                style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          _foregroundOnly(
            child: CDAdsBannerWidget(
              adUnitId: 'banner-320x50',
              size: CDAdsSize.banner320x50,
              showCloseButton: true,
              isAutoRefreshEnabled: true,
              refreshInterval: 15,
              onLoaded: () => debugPrint('[CDAds] Banner 320x50 loaded'),
              onFailedToLoad: (e) => debugPrint('[CDAds] Banner failed: $e'),
              onImpression: () => debugPrint('[CDAds] Banner impression'),
              onClicked: () => debugPrint('[CDAds] Banner clicked'),
              onWillLeaveApp: () => debugPrint('[CDAds] Banner will leave app'),
              onExpanded: () => debugPrint('[CDAds] Banner expanded'),
              onCollapsed: () => debugPrint('[CDAds] Banner collapsed'),
              onDidClose: () => debugPrint('[CDAds] Banner closed by user'),
            ),
            width: 320,
            height: 50,
          ),
          const Divider(height: 32),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text('300×250 MREC', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          _foregroundOnly(
            child: CDAdsBannerWidget(
              adUnitId: 'banner-300x250',
              size: CDAdsSize.banner300x250,
              onLoaded: () => debugPrint('[CDAds] MREC loaded'),
            ),
            width: 300,
            height: 250,
          ),
          const Divider(height: 32),
          const Padding(
            padding: EdgeInsets.all(16),
            child: Text('Custom 250×250', style: TextStyle(fontWeight: FontWeight.bold)),
          ),
          _foregroundOnly(
            child: CDAdsBannerWidget(
              adUnitId: 'banner-custom',
              size: CDAdsSize.custom,
              customWidth: 250,
              customHeight: 250,
              onLoaded: () => debugPrint('[CDAds] Custom banner loaded'),
            ),
            width: 250,
            height: 250,
          ),
        ],
      ),
    );
  }

  // Tearing the banner widget down (rather than just visually hiding it)
  // removes the PlatformView, so no ad request can be made while backgrounded.
  // It's recreated — and requests a fresh ad — once the app resumes.
  Widget _foregroundOnly({required Widget child, required double width, required double height}) {
    // ListView forces tight cross-axis (width) constraints on direct children, which
    // would stretch the banner's fixed-width SizedBox to the full screen width. Align
    // gives it loose constraints instead so it renders at its actual requested size.
    if (!_isForeground) return SizedBox(width: width, height: height);
    return Align(alignment: Alignment.centerLeft, child: child);
  }
}

// ── Interstitial screen ───────────────────────────────────────────────────────

class InterstitialScreen extends StatefulWidget {
  const InterstitialScreen({super.key});

  @override
  State<InterstitialScreen> createState() => _InterstitialScreenState();
}

class _InterstitialScreenState extends State<InterstitialScreen> {
  late final CDAdsInterstitialAd _ad;
  String _status = 'Idle';

  @override
  void initState() {
    super.initState();
    _ad = CDAdsInterstitialAd(adUnitId: 'interstitial-unit');
    _ad.onLoaded       = () => _setStatus('Loaded — ready to show');
    _ad.onFailedToLoad = (e) => _setStatus('Failed: ${e.message}');
    _ad.onWillAppear    = () => _setStatus('Appearing…');
    _ad.onDismissed     = () => _setStatus('Dismissed');
    _ad.onExpired       = () => _setStatus('Expired');
  }

  @override
  void dispose() {
    _ad.dispose();
    super.dispose();
  }

  void _setStatus(String s) => setState(() => _status = s);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Interstitial')),
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Status: $_status', style: const TextStyle(fontSize: 16)),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () { _ad.load(); _setStatus('Loading…'); },
              child: const Text('Load'),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: _ad.isReady ? _ad.show : null,
              child: const Text('Show'),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Rewarded screen ───────────────────────────────────────────────────────────

class RewardedScreen extends StatefulWidget {
  const RewardedScreen({super.key});

  @override
  State<RewardedScreen> createState() => _RewardedScreenState();
}

class _RewardedScreenState extends State<RewardedScreen> {
  late final CDAdsRewardedVideoAd _ad;
  String _status = 'Idle';
  CDAdsReward? _lastReward;

  @override
  void initState() {
    super.initState();
    _ad = CDAdsRewardedVideoAd(adUnitId: 'rewarded-unit');
    _ad.onLoaded       = () => _setStatus('Loaded — ready to show');
    _ad.onFailedToLoad = (e) => _setStatus('Failed: ${e.message}');
    _ad.onDismissed     = () => _setStatus('Dismissed');
    _ad.onExpired       = () => _setStatus('Expired');
    _ad.onEarnedReward  = (r) {
      setState(() { _lastReward = r; _status = 'Earned reward!'; });
      debugPrint('[CDAds] Reward: ${r.amount} ${r.currencyType}');
    };
  }

  @override
  void dispose() {
    _ad.dispose();
    super.dispose();
  }

  void _setStatus(String s) => setState(() => _status = s);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Rewarded Video')),
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Status: $_status', style: const TextStyle(fontSize: 16)),
            if (_lastReward != null)
              Padding(
                padding: const EdgeInsets.only(top: 8),
                child: Text(
                  'Last reward: ${_lastReward!.amount} ${_lastReward!.currencyType}',
                  style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold),
                ),
              ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () { _ad.load(); _setStatus('Loading…'); },
              child: const Text('Load'),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: _ad.isReady ? _ad.show : null,
              child: const Text('Show'),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Native Ad screen ──────────────────────────────────────────────────────────

class NativeAdScreen extends StatefulWidget {
  const NativeAdScreen({super.key});

  @override
  State<NativeAdScreen> createState() => _NativeAdScreenState();
}

class _NativeAdScreenState extends State<NativeAdScreen> {
  late final CDAdsNativeAdManager _manager;
  CDAdsNativeAdData? _adData;
  String? _error;

  @override
  void initState() {
    super.initState();
    _manager = CDAdsNativeAdManager(adUnitId: 'native-unit');
    _manager.onLoaded       = (data) => setState(() => _adData = data);
    _manager.onFailedToLoad = (e) => setState(() => _error = e.message);
    _manager.onExpired      = () => setState(() => _adData = null);
    _manager.load();
  }

  @override
  void dispose() {
    _manager.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Native Ad')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: _buildBody(),
      ),
    );
  }

  Widget _buildBody() {
    if (_error != null) {
      return Center(child: Text('Error: $_error', style: const TextStyle(color: Colors.red)));
    }
    if (_adData == null) {
      return const Center(child: CircularProgressIndicator());
    }
    final ad = _adData!;
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            if (ad.iconImageUrl != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: Image.network(ad.iconImageUrl!, width: 48, height: 48,
                    errorBuilder: (_, __, ___) => const Icon(Icons.image, size: 48)),
              ),
            Text(ad.title ?? '', style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Text(ad.body ?? '', style: const TextStyle(fontSize: 14)),
            const SizedBox(height: 8),
            if (ad.sponsoredLabel != null)
              Text('Sponsored: ${ad.sponsoredLabel}',
                  style: const TextStyle(fontSize: 12, color: Colors.grey)),
            const SizedBox(height: 16),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                ElevatedButton(
                  onPressed: () {
                    _manager.trackImpression(ad.adId);
                    debugPrint('[CDAds] Native impression tracked');
                  },
                  child: const Text('Track Impression'),
                ),
                ElevatedButton(
                  onPressed: () {
                    _manager.trackClick(ad.adId);
                    debugPrint('[CDAds] Native click tracked');
                  },
                  child: Text(ad.callToAction ?? 'Learn More'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

// ── Location tracking screen ───────────────────────────────────────────────────

class LocationScreen extends StatefulWidget {
  const LocationScreen({super.key});

  @override
  State<LocationScreen> createState() => _LocationScreenState();
}

class _LocationScreenState extends State<LocationScreen> {
  bool _enabled = false;
  CDAdsGeoInfo? _lastLocation;

  @override
  void initState() {
    super.initState();
    CDAdsEventHandler.onLocationUpdate = (geo) => setState(() => _lastLocation = geo);
  }

  @override
  void dispose() {
    CDAdsEventHandler.onLocationUpdate = null;
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Location Tracking')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'The host app must request location permission before enabling '
              'tracking — the SDK does not request it on your behalf.',
              style: TextStyle(color: Colors.grey),
            ),
            SwitchListTile(
              title: const Text('Enable background location tracking'),
              value: _enabled,
              onChanged: (value) {
                setState(() => _enabled = value);
                CDAds.setLocationEnabled(value);
              },
            ),
            const SizedBox(height: 16),
            Text(
              _lastLocation == null
                  ? 'No location update received yet.'
                  : 'Last location: ${_lastLocation!.lat}, ${_lastLocation!.lon}'
                      '${_lastLocation!.city != null ? ' (${_lastLocation!.city})' : ''}',
            ),
          ],
        ),
      ),
    );
  }
}
