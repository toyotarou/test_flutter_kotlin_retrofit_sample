package com.example.test_flutter_kotlin_hello_world

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class WifiForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("WifiForegroundService", "🔧 onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("WifiForegroundService", "🚀 ForegroundService 起動")

        // 通知チャネルの準備（Android 8 以降必須）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "wifi_service_channel",
                "Wi-Fi Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, "wifi_service_channel")
            .setContentTitle("Wi-Fi位置情報取得中")
            .setContentText("サービスが実行されています")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)

        // まだ何もせず、継続実行
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // バインドしない
    }
}
