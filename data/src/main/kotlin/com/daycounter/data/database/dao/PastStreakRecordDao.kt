package com.daycounter.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.daycounter.data.database.entity.PastStreakRecordEntity

@Dao
interface PastStreakRecordDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: PastStreakRecordEntity): Long

    /** Returns a page of archived streaks for the counter, newest end date first. */
    @Query(
        "SELECT * FROM past_streak_records WHERE counter_id = :counterId " +
            "ORDER BY end_date DESC, id DESC LIMIT :limit OFFSET :offset",
    )
    suspend fun pagedByCounter(counterId: Long, limit: Int, offset: Int): List<PastStreakRecordEntity>
}
