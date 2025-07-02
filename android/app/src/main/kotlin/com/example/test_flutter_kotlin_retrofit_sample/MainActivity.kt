package com.example.test_flutter_kotlin_retrofit_sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StationListScreen()
            }
        }
    }
}

@Composable
fun StationListScreen() {
    var trainLines by remember { mutableStateOf<List<TrainLine>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        try {
            val response = ApiClient.service.getTokyoTrainStations()
            if (response.data != null) {
                trainLines = response.data!!
                Log.d("API", "✅ 取得成功: ${trainLines.size}路線")
            } else {
                Log.e("API", "❌ APIレスポンスに data が含まれていません")
            }
        } catch (e: Exception) {
            Log.e("API", "❌ API呼び出し失敗", e)
        }
        loading = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                trainLines.forEach { line ->
                    item {
                        Text("🚆 ${line.train_name}", style = MaterialTheme.typography.titleMedium)
                    }
                    items(line.station) { station ->
                        Text(
                            "・${station.station_name} (${station.lat}, ${station.lng})",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
