package com.example.promodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Timestamp


@Entity(tableName = "focus_records")
data class FocusRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,
    val focusMinutes: Int,
    val breakMinutes: Int,
    val timestamp: Long = System.currentTimeMillis()
)
