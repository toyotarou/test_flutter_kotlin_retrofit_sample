import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:test_flutter_kotlin_hello_world/pigeon/wifi_location.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Wi-Fi 位置情報収集',
      theme: ThemeData(colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue), useMaterial3: true),
      home: const MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key});

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  bool _isRunning = false;
  bool _isLoading = false;
  List<WifiLocation> _locations = [];

  Future<void> _requestPermissions() async {
    final locationStatus = await Permission.location.request();
    final fgServiceStatus = await Permission.ignoreBatteryOptimizations.request();

    if (!locationStatus.isGranted) {
      throw Exception('位置情報の権限が拒否されました');
    }
    if (!fgServiceStatus.isGranted) {
      debugPrint('バッテリー最適化除外の許可が拒否されました（続行可能）');
    }
  }

  Future<void> _startService() async {
    setState(() {
      _isLoading = true;
    });

    try {
      await _requestPermissions(); // ✅ パーミッション取得

      final api = WifiLocationApi();
      await api.startLocationCollection();

      // 🕒 サービス起動を少し待つ（例: 1秒）
      await Future.delayed(const Duration(seconds: 1));
      await _checkStatus();
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('❌ エラー: ${e.toString()}')));
    }

    setState(() {
      _isLoading = false;
    });
  }

  Future<void> _checkStatus() async {
    final api = WifiLocationApi();
    final result = await api.isCollecting();
    setState(() {
      _isRunning = result;
    });
  }

  Future<void> _fetchData() async {
    final api = WifiLocationApi();
    final result = await api.getWifiLocations();
    setState(() {
      _locations = result.whereType<WifiLocation>().toList();
      _locations.sort((a, b) => '${b.date} ${b.time}'.compareTo('${a.date} ${a.time}'));
    });
  }

  @override
  void initState() {
    super.initState();
    _checkStatus();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Wi-Fi位置情報収集サービス')),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ElevatedButton(onPressed: _isLoading ? null : _startService, child: const Text('取得開始（Kotlin）')),
            const SizedBox(height: 12),
            ElevatedButton(onPressed: _checkStatus, child: const Text('現在の稼働状態を確認')),
            const SizedBox(height: 12),
            Text(_isRunning ? '✅ サービス稼働中' : '❌ サービス停止中', style: const TextStyle(fontSize: 16)),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () async {
                final api = WifiLocationApi();
                await api.deleteAllWifiLocations();

                ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('全件削除しました')));
              },
              child: const Text('全削除'),
            ),
            const SizedBox(height: 24),
            ElevatedButton(onPressed: _fetchData, child: const Text('Roomから取得（Flutter表示）')),
            const SizedBox(height: 12),
            Expanded(
              child: _locations.isEmpty
                  ? const Text('📭 データがまだありません')
                  : ListView.builder(
                      itemCount: _locations.length,
                      itemBuilder: (context, index) {
                        final loc = _locations[index];
                        return Card(
                          margin: const EdgeInsets.symmetric(vertical: 6),
                          child: ListTile(
                            title: Text('📡 ${loc.ssid}'),
                            subtitle: Text('🕒 ${loc.date} ${loc.time}\n📍 緯度: ${loc.latitude}, 経度: ${loc.longitude}'),
                          ),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }
}
