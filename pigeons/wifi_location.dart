import 'package:pigeon/pigeon.dart';

class WifiLocation {
  String date;
  String time;
  String ssid;
  String latitude;
  String longitude;

  WifiLocation({
    required this.date,
    required this.time,
    required this.ssid,
    required this.latitude,
    required this.longitude,
  });
}

@HostApi()
abstract class WifiLocationApi {
  List<WifiLocation> getWifiLocations();

  void deleteAllWifiLocations();

  void startLocationCollection();

  bool isCollecting();
}
