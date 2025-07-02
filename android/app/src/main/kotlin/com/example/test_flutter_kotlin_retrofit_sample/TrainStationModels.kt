package com.example.test_flutter_kotlin_retrofit_sample

data class ApiResponse(
    val data: List<TrainLine>? // ← nullを許容
)

data class TrainLine(
    val train_name: String,
    val station: List<Station>
)

data class Station(
    val station_name: String,
    val lat: String,
    val lng: String
)
