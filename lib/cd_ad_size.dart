class CDAdSize {
  final String name;
  final double width;
  final double height;

  const CDAdSize._(this.name, this.width, this.height);

  Map<String, dynamic> toMap() => {
        'name': name,
        'width': width.toInt(),
        'height': height.toInt(),
      };

  static const CDAdSize banner320x50 = CDAdSize._('banner320x50', 320, 50);
  static const CDAdSize banner300x50 = CDAdSize._('banner300x50', 300, 50);
  static const CDAdSize banner300x250 = CDAdSize._('banner300x250', 300, 250);
  static const CDAdSize banner320x100 = CDAdSize._('banner320x100', 320, 100);
  static const CDAdSize banner728x90 = CDAdSize._('banner728x90', 728, 90);
  static const CDAdSize banner728x250 = CDAdSize._('banner728x250', 728, 250);
  static const CDAdSize banner320x480 = CDAdSize._('banner320x480', 320, 480);
  static const CDAdSize banner768x1024 =
      CDAdSize._('banner768x1024', 768, 1024);
  static const CDAdSize banner300x600 = CDAdSize._('banner300x600', 300, 600);
  static const CDAdSize banner1086x1086 =
      CDAdSize._('banner1086x1086', 1086, 1086);
  static const CDAdSize banner970x250 = CDAdSize._('banner970x250', 970, 250);
  static const CDAdSize banner480x320 = CDAdSize._('banner480x320', 480, 320);
}
