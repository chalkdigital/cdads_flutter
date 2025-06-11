class CDGeoInfo {
  final double lat;
  final double lon;
  final String countryCode;
  final String region;
  final String city;
  final String zip;
  final String streetAddress;
  final String subThoroughFare;
  final String haccuracy;

  // Fixed type
  final int type = 3;

  CDGeoInfo({
    required this.lat,
    required this.lon,
    required this.countryCode,
    required this.region,
    required this.city,
    required this.zip,
    this.streetAddress = '',
    this.subThoroughFare = '',
    this.haccuracy = "5",
  });

  Map<String, dynamic> toMap() {
    return {
      'lat': lat,
      'lon': lon,
      'type': type,
      'countryCode': countryCode,
      'region': region,
      'city': city,
      'zip': zip,
      'streetAddress': streetAddress,
      'subThoroughFare': subThoroughFare,
      'haccuracy': haccuracy,
    };
  }
}
