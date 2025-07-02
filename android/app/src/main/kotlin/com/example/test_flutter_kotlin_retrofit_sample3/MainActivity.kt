package com.example.test_flutter_kotlin_retrofit_sample3

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Kotlin 側の API 実装を登録
        WifiLocationApi.setUp(
            flutterEngine.dartExecutor.binaryMessenger,
            WifiLocationApiImpl(this)
        )
    }
}
