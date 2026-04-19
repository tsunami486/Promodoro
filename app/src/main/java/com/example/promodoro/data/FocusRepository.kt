package com.example.promodoro.data

import kotlinx.coroutines.flow.Flow

class FocusRepository(private val focusDao: FocusDao) {
    val allRecords: Flow<List<FocusRecord>> = focusDao.getAllRecords()

    suspend fun insertRecord(record: FocusRecord): Long {
        return focusDao.insertRecord(record)
    }
    suspend fun updateFocusMinutes(id: Long, minutes: Int) {
        focusDao.updateFocusMinutes(id, minutes)
    }
}