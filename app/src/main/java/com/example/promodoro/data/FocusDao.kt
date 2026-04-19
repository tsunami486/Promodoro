package com.example.promodoro.data

import android.adservices.ondevicepersonalization.EventLogRecord
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface FocusDao {
    @Insert
    suspend fun insertRecord(record: FocusRecord): Long
    @Query("UPDATE focus_records SET focusMinutes = :minutes WHERE id = :id")
    suspend fun updateFocusMinutes(id: Long, minutes: Int)
    @Query("SELECT * FROM focus_records WHERE date = :date")
    fun getRecordsByDate(date: String): Flow<List<FocusRecord>>

    @Query("SELECT * FROM focus_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<FocusRecord>>


}