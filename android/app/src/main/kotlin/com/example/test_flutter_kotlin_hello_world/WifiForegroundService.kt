package com.example.test_flutter_kotlin_hello_world

import android.content.pm.ServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.wifi.WifiManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.example.test_flutter_kotlin_hello_world.room.AppDatabase
import com.example.test_flutter_kotlin_hello_world.room.WifiLocationEntity
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class WifiForegroundService : Service() {

    companion object {
        var isRunning: Boolean = false
        private const val CHANNEL_ID = "WifiLocationServiceChannel"
    }

    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        createNotificationChannel()

        val notification = createNotification("Wi-Fi位置情報取得中...")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ は service type を明示する必要あり
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(1, notification)
        }

        startCollecting()
    }

    override fun onDestroy() {
        isRunning = false
        job?.cancel()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startCollecting() {
        val dao = AppDatabase.getDatabase(this).wifiLocationDao()
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    val now = Date()
                    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

                    val ssid = wifiManager.connectionInfo.ssid ?: "Unknown"

                    val location: Location? = withContext(Dispatchers.IO) {
                        try {
                            suspendCancellableCoroutine<Location?> { cont ->
                                fusedLocationClient.lastLocation
                                    .addOnSuccessListener { cont.resume(it, null) }
                                    .addOnFailureListener { cont.resume(null, null) }
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    if (location != null) {
                        val entity = WifiLocationEntity(
                            date = sdfDate.format(now),
                            time = sdfTime.format(now),
                            ssid = ssid,
                            latitude = location.latitude.toString(),
                            longitude = location.longitude.toString()
                        )
                        dao.insert(entity)
                        Log.d("WifiForegroundService", "✅ 位置情報を保存しました: $entity")
                    } else {
                        Log.w("WifiForegroundService", "⚠ 位置情報が取得できませんでした")
                    }

                    delay(60_000) // 1分待機
                } catch (e: Exception) {
                    Log.e("WifiForegroundService", "❌ データ保存中にエラー", e)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Wi-Fi位置情報取得サービス",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(content: String): Notification {
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Wi-Fi位置情報取得")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
    }
}
