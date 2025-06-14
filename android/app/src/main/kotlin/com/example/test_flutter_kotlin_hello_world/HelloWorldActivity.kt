package com.example.test_flutter_kotlin_hello_world

import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import androidx.room.Room
import com.example.test_flutter_kotlin_hello_world.room.AppDatabase
import com.example.test_flutter_kotlin_hello_world.room.WifiLocationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HelloWorldActivity : ComponentActivity() {

    private val locationPermissionRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // パーミッションチェック
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE
                ),
                locationPermissionRequestCode
            )
        }

        setContent {
            WifiLocationScreen()
        }
    }
}

@Composable
fun WifiLocationScreen() {
    val context = LocalContext.current
    var ssid by remember { mutableStateOf("未取得") }
    var locationText by remember { mutableStateOf("未取得") }

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "wifi_location_db"
        ).build()
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // 位置情報とSSID取得ボタン
        Button(onClick = {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        locationText = "緯度: ${location.latitude}, 経度: ${location.longitude}"
                    } else {
                        locationText = "位置情報が取得できませんでした"
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                locationText = "パーミッションエラー"
            }

            val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java)
            val wifiInfo = wifiManager?.connectionInfo
            ssid = wifiInfo?.ssid ?: "取得失敗"
        }) {
            Text("位置情報とSSIDを取得")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Roomに保存ボタン（suspend関数対応）
        Button(onClick = {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val calendar = java.util.Calendar.getInstance()
                        val date = "%04d-%02d-%02d".format(
                            calendar.get(java.util.Calendar.YEAR),
                            calendar.get(java.util.Calendar.MONTH) + 1,
                            calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                        val time = "%02d:%02d:%02d".format(
                            calendar.get(java.util.Calendar.HOUR_OF_DAY),
                            calendar.get(java.util.Calendar.MINUTE),
                            calendar.get(java.util.Calendar.SECOND)
                        )

                        val entity = WifiLocationEntity(
                            date = date,
                            time = time,
                            ssid = ssid,
                            latitude = location.latitude.toString(),
                            longitude = location.longitude.toString()
                        )

                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                db.wifiLocationDao().insert(entity)
                            }
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }) {
            Text("保存")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("SSID: $ssid", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("現在地: $locationText", fontSize = 20.sp)
    }
}
