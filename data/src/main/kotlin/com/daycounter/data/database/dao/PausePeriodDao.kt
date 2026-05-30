package com.daycounter.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daycounter.data.database.entity.PausePeriodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PausePeriodDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(period: PausePeriodEntity): Long

    @Query("SELECT * FROM pause_periods WHERE counter_id = :counterId ORDER BY end_date ASC")
    suspend fun selectForCounter(counterId: Long): List<PausePeriodEntity>

    /** All pause periods across all counters (used by Stats + snapshot/restore). */
    @Query("SELECT * FROM pause_periods")
    fun observeAll(): Flow<List<PausePeriodEntity>>

    @Query("SELECT * FROM pause_periods")
    suspend fun selectAll(): List<PausePeriodEntity>

    /** Count of completed pause intervals across all counters. */
    @Query("SELECT COUNT(*) FROM pause_periods")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM pause_periods WHERE counter_id = :counterId")
    suspend fun deleteForCounter(counterId: Long)
}
