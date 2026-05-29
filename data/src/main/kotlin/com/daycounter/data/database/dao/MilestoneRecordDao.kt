package com.daycounter.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daycounter.data.database.entity.MilestoneRecordEntity

@Dao
interface MilestoneRecordDao {

    /** Returns the new row ID, or -1 on conflict (duplicate). */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(record: MilestoneRecordEntity): Long

    @Query("DELETE FROM milestone_records WHERE counter_id = :counterId")
    suspend fun deleteAllForCounter(counterId: Long)

    /** All milestone rows for the counter (drives the achieved list and dedup). */
    @Query("SELECT * FROM milestone_records WHERE counter_id = :counterId ORDER BY milestone_days ASC")
    suspend fun selectForCounter(counterId: Long): List<MilestoneRecordEntity>

    /** Marks every milestone row of the counter as celebration-shown (FR-021). */
    @Query("UPDATE milestone_records SET celebration_shown = 1 WHERE counter_id = :counterId")
    suspend fun markAllShownForCounter(counterId: Long)
}
