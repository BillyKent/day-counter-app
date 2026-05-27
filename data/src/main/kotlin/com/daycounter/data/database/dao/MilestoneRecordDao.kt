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
}
