package com.example.test_flutter_kotlin_retrofit_sample3.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiLocationDao {

    @Insert
    suspend fun insert(location: WifiLocationEntity)

    @Query("SELECT * FROM wifi_location")
    fun getAll(): Flow<List<WifiLocationEntity>>

    @Query("DELETE FROM wifi_location")
    suspend fun deleteAll()

    // ✅ 1件削除用メソッド
    @Delete
    suspend fun delete(location: WifiLocationEntity)
}
