package com.example.test_flutter_kotlin_retrofit_sample

// Retrofit のアノテーションを使うためのインポート
import retrofit2.http.Body
import retrofit2.http.POST

// Retrofit 本体のクラス
import retrofit2.Retrofit

// JSON → Kotlin データクラスへ自動変換する Moshi ライブラリ
import retrofit2.converter.moshi.MoshiConverterFactory

// 通信のログ出力用ライブラリ（OkHttp + Interceptor）
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * ✅ API定義インターフェース
 *
 * Retrofit を使って定義する「HTTPリクエストの仕様」。
 * この中のメソッドが、実際に通信されるエンドポイントになる。
 */
interface ApiService {

    /**
     * POST リクエストを送信する。
     *
     * @param body 通信時に一緒に送るリクエストボディ（ここでは Map 型）
     *             デフォルトは空のマップ（つまりボディなしと同じ）。
     * @return ApiResponse 型（事前に定義したデータ構造）でレスポンスを受け取る。
     */
    @POST("api/getTokyoTrainStation")
    suspend fun getTokyoTrainStations(
        @Body body: Map<String, String> = emptyMap()
    ): ApiResponse
}

/**
 * ✅ Retrofit クライアントの構築用オブジェクト
 *
 * アプリ全体で1つのインスタンスだけを使い回すために `object` として定義。
 */
object ApiClient {

    /**
     * ✅ 通信内容のログを出力する Interceptor（OkHttp の機能）
     *
     * - BODY レベルに設定することで、リクエスト・レスポンスの本文まで全て出力される。
     * - デバッグ時にはとても役立つ。
     * - 本番アプリではセキュリティやパフォーマンス上の理由で無効にすることが多い。
     */
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * ✅ OkHttpClient のビルダー
     *
     * Retrofit の内部で使われる HTTP クライアント。
     * Interceptor を追加して、ログ出力や共通ヘッダーなどを設定できる。
     */
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging) // ログ用インターセプタを追加
        .build()

    /**
     * ✅ Retrofit インスタンスの生成
     *
     * - baseUrl(): 通信の基点となるURL（末尾はスラッシュで終わる必要がある）
     * - client(): OkHttpClient を指定
     * - addConverterFactory(): Moshi によって JSON ⇄ データクラスの変換を自動化
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://toyohide.work/BrainLog/") // 通信先のドメイン（必ずスラッシュで終える）
        .client(client)                            // 上で作成した OkHttpClient を指定
        .addConverterFactory(MoshiConverterFactory.create()) // JSON 変換ライブラリを指定
        .build()

    /**
     * ✅ Retrofit から生成した API サービス実体
     *
     * - 上記の `ApiService` インターフェースの実装がここで自動的に作られる。
     * - `service.getTokyoTrainStations()` のように使えるようになる。
     */
    val service: ApiService = retrofit.create(ApiService::class.java)
}
