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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class HelloWorldActivity : ComponentActivity() {

    private val locationPermissionRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
    var savedData by remember { mutableStateOf<List<WifiLocationEntity>>(emptyList()) }

    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "wifi_location_db"
        ).build()
    }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        savedData = withContext(Dispatchers.IO) {
            db.wifiLocationDao().getAll()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(100.dp))

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

            if (ssid == null || ssid == "<unknown ssid>" || ssid == "0x") {
                val success = wifiManager?.startScan()
                if (success == true) {
                    val scanResults = wifiManager.scanResults
                    if (scanResults.isNotEmpty()) {
                        ssid = scanResults[0].SSID
                    } else {
                        ssid = "取得失敗"
                    }
                } else {
                    ssid = "スキャン失敗"
                }
            }
        }) {
            Text("位置情報とSSIDを取得")
        }

        Spacer(modifier = Modifier.height(16.dp))

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

                        scope.launch {
                            db.wifiLocationDao().insert(entity)
                            savedData = db.wifiLocationDao().getAll()
                        }
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }) {
            Text("保存")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("SSID: $ssid", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text("現在地: $locationText", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                savedData = db.wifiLocationDao().getAll()
            }
        }) {
            Text("保存済みデータを読み込む")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("保存されたデータ一覧:", fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))

        savedData.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📍 ${item.date} ${item.time} | ${item.ssid} | 緯度:${item.latitude}, 経度:${item.longitude}",
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            db.wifiLocationDao().delete(item)
                            savedData = db.wifiLocationDao().getAll()
                        }
                    },
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("削除", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
