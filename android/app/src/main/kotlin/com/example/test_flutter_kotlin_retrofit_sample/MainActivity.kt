package com.example.test_flutter_kotlin_retrofit_sample

// Androidライフサイクル系
import android.os.Bundle
import android.util.Log

// Jetpack Compose の UI 基本コンポーネント
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

// Compose の状態管理や副作用処理
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * ✅ アプリのエントリーポイント（Activity）
 *
 * ComponentActivity を継承しており、Jetpack Compose の画面構築を行っている。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Jetpack Compose の画面を構築する関数
        setContent {
            // ✅ Material3 テーマでUIを構成
            MaterialTheme {
                StationListScreen()
            }
        }
    }
}

/**
 * ✅ 駅一覧を表示する Compose UI 関数
 *
 * API から取得した路線＋駅一覧データを画面に表示する。
 */
@Composable
fun StationListScreen() {
    // ✅ 駅データのリスト（状態を保持）
    var trainLines by remember { mutableStateOf<List<TrainLine>>(emptyList()) }

    // ✅ 読み込み中フラグ（状態を保持）
    var loading by remember { mutableStateOf(false) }

    /**
     * ✅ 最初の一度だけ呼ばれる副作用ブロック（非同期API呼び出し）
     *
     * - LaunchedEffect(Unit) により、Composable 初回表示時に1回だけ実行される。
     * - suspend 関数（API）を呼ぶために coroutineScope が裏で使われる。
     */
    LaunchedEffect(Unit) {
        loading = true
        try {
            // ✅ API 呼び出し（suspend 関数）
            val response = ApiClient.service.getTokyoTrainStations()

            if (response.data != null) {
                trainLines = response.data!! // ✅ データをステートにセット（再描画される）
                Log.d("API", "✅ 取得成功: ${trainLines.size}路線")
            } else {
                Log.e("API", "❌ APIレスポンスに data が含まれていません")
            }

        } catch (e: Exception) {
            Log.e("API", "❌ API呼び出し失敗", e)
        }
        loading = false
    }

    /**
     * ✅ 画面全体のレイアウト
     *
     * - Box を使うことで、読み込み中表示とリストを同じ場所に重ねて制御できる。
     */
    Box(
        modifier = Modifier
            .fillMaxSize()     // 画面全体を使う
            .padding(16.dp)    // 周囲に余白をつける
    ) {
        if (loading) {
            // ✅ ローディング中：中央にインジケーターを表示
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            // ✅ 通常時：APIから取得した駅情報をリスト表示
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                trainLines.forEach { line ->
                    // ✅ 路線名を表示（中央線・山手線など）
                    item {
                        Text(
                            text = "🚆 ${line.train_name}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // ✅ 各路線に属する駅一覧を表示
                    items(line.station) { station ->
                        Text(
                            text = "・${station.station_name} (${station.lat}, ${station.lng})",
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }

                    // ✅ 各路線の間にスペースを追加
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
