/// Represents the environments (production or test).
enum CDEnvironment {
  production,
  test,
}

/// Represents various Ad Providers.
enum CDADProvider {
  chalk,
  google,
  adMarvel,
  adColonyAurora,
  amazon,
  chartboost,
  facebook,
  inMobi,
  heyzap,
  millennial,
  vungle,
  vurve,
  unityAds,
  yuMe,
}

/// Represents log levels.
enum CDLogLevel {
  all(0),
  trace(10),
  debug(20),
  info(30),
  warn(40),
  error(50),
  fatal(60),
  off(70);

  final int value;
  const CDLogLevel(this.value);
}

/// Represents types of native ad data.
enum CDNativeAdDataType {
  sponsored(1),
  description(2),
  rating(3),
  likes(4),
  downloads(5),
  price(6),
  salePrice(7),
  phone(8),
  address(9),
  description2(10),
  displayUrl(11),
  ctaText(12),
  other(501);

  final int value;
  const CDNativeAdDataType(this.value);
}
