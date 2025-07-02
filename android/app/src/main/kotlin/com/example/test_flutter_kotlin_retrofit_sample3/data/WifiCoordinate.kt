package com.example.test_flutter_kotlin_retrofit_sample3.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_coordinates")
data class WifiCoordinate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,
    val time: String,
    val ssid: String,
    val latitude: String,
    val longitude: String
)
