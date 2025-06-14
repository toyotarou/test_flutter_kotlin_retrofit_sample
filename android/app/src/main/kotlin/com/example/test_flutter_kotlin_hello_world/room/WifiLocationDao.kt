package com.example.test_flutter_kotlin_hello_world.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WifiLocationDao {

    @Insert
    suspend fun insert(location: WifiLocationEntity)

    @Query("SELECT * FROM wifi_location")
    suspend fun getAll(): List<WifiLocationEntity>
}
