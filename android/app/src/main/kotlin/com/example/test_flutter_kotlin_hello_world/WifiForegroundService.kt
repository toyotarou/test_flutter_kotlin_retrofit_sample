package com.example.test_flutter_kotlin_hello_world

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.test_flutter_kotlin_hello_world.room.AppDatabase
import com.example.test_flutter_kotlin_hello_world.room.WifiLocationEntity
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import java.util.*

class WifiForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timer: Timer? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("WifiForegroundService", "🔧 onCreate")

        // 通知チャンネルを作成（API 26+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "位置情報取得チャンネル",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Wi-Fi位置情報取得通知"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("WifiForegroundService", "🚀 ForegroundService 起動")

        // 通知を作成してstartForeground()を必ず呼ぶ
        val notification: Notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("Wi-Fi位置情報取得中")
            .setContentText("1分ごとにWi-Fiと位置を保存しています")
            .setSmallIcon(R.drawable.ao_toyo) // ← 必ず存在するアイコン
            .build()

        startForeground(1, notification) // 🔑 5秒以内に必ず呼ぶ

        startRepeatingTask()

        return START_STICKY
    }

    private fun startRepeatingTask() {
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                collectAndSaveLocation()
            }
        }, 0, 60_000) // 1分ごと
    }

    private fun collectAndSaveLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val db = AppDatabase.getDatabase(applicationContext)

        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    var ssid = wifiManager.connectionInfo?.ssid ?: "取得失敗"

                    if (ssid == "<unknown ssid>" || ssid == "0x") {
                        val success = wifiManager.startScan()
                        if (success) {
                            val scanResults = wifiManager.scanResults
                            if (scanResults.isNotEmpty()) {
                                ssid = scanResults[0].SSID
                            }
                        }
                    }

                    val now = Calendar.getInstance()
                    val date = "%04d-%02d-%02d".format(
                        now.get(Calendar.YEAR),
                        now.get(Calendar.MONTH) + 1,
                        now.get(Calendar.DAY_OF_MONTH)
                    )
                    val time = "%02d:%02d:%02d".format(
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        now.get(Calendar.SECOND)
                    )

                    val entity = WifiLocationEntity(
                        date = date,
                        time = time,
                        ssid = ssid,
                        latitude = location.latitude.toString(),
                        longitude = location.longitude.toString()
                    )

                    serviceScope.launch {
                        db.wifiLocationDao().insert(entity)
                        Log.d("WifiForegroundService", "✅ 保存成功: $entity")
                    }
                } else {
                    Log.w("WifiForegroundService", "❌ 位置情報が null")
                }
            }
        } catch (e: SecurityException) {
            Log.e("WifiForegroundService", "❌ パーミッションエラー: ${e.message}")
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        serviceScope.cancel()
        super.onDestroy()
        Log.d("WifiForegroundService", "🛑 ForegroundService 停止")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
