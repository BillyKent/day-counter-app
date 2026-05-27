package com.daycounter.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daycounter.data.database.entity.CounterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {

    /**
     * Emits counters ordered by [CounterEntity.startDate] ASC (oldest start date =
     * longest streak), with [CounterEntity.createdAt] ASC as the stable tie-breaker.
     */
    @Query("SELECT * FROM counters ORDER BY start_date ASC, created_at ASC")
    fun getAllCountersSortedByStreak(): Flow<List<CounterEntity>>

    @Query("SELECT * FROM counters WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CounterEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(counter: CounterEntity): Long

    @Update
    suspend fun update(counter: CounterEntity)

    @Delete
    suspend fun delete(counter: CounterEntity)
}
