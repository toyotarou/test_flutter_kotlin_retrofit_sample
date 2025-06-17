package com.example.test_flutter_kotlin_hello_world

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.test_flutter_kotlin_hello_world.room.AppDatabase
import com.example.test_flutter_kotlin_hello_world.room.WifiLocationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class WifiLocationApiImpl(private val context: Context) : WifiLocationApi {

    override fun getWifiLocations(): List<WifiLocation> {
        val db = AppDatabase.getDatabase(context)
        val dao = db.wifiLocationDao()

        val entityList: List<WifiLocationEntity> = runBlocking {
            dao.getAll().first() // Flow → List に変換
        }

        return entityList.map {
            WifiLocation(
                date = it.date,
                time = it.time,
                ssid = it.ssid,
                latitude = it.latitude,
                longitude = it.longitude
            )
        }
    }

    override fun deleteAllWifiLocations() {
        val db = AppDatabase.getDatabase(context)
        val dao = db.wifiLocationDao()

        runBlocking {
            dao.deleteAll()
        }
    }

    override fun startLocationCollection() {
        val intent = Intent(context, WifiForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        WifiForegroundService.isRunning = true // 明示的に true
    }

    override fun isCollecting(): Boolean {
        return WifiForegroundService.isRunning
    }
}
