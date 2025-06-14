package com.example.test_flutter_kotlin_hello_world.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WifiLocationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun wifiLocationDao(): WifiLocationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wifi_location_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
