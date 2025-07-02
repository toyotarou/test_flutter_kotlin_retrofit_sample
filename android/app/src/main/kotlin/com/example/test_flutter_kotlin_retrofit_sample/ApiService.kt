package com.example.test_flutter_kotlin_retrofit_sample

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

interface ApiService {
    @POST("api/getTokyoTrainStation")
    suspend fun getTokyoTrainStations(@Body body: Map<String, String> = emptyMap()): ApiResponse
}

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // ✅ 通信内容を出力
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://toyohide.work/BrainLog/")
        .client(client) // ✅ OkHttpClient をセット
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: ApiService = retrofit.create(ApiService::class.java)
}
