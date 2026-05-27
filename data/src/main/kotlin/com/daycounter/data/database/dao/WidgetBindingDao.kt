package com.daycounter.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.daycounter.data.database.entity.WidgetBindingEntity

@Dao
interface WidgetBindingDao {

    @Query("SELECT * FROM widget_bindings WHERE widget_id = :widgetId LIMIT 1")
    suspend fun getByWidgetId(widgetId: Int): WidgetBindingEntity?

    @Query("SELECT * FROM widget_bindings")
    suspend fun getAll(): List<WidgetBindingEntity>

    @Query("SELECT * FROM widget_bindings WHERE counter_id = :counterId")
    suspend fun getAllForCounter(counterId: Long): List<WidgetBindingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(binding: WidgetBindingEntity)

    @Update
    suspend fun update(binding: WidgetBindingEntity)

    @Query("DELETE FROM widget_bindings WHERE widget_id = :widgetId")
    suspend fun delete(widgetId: Int)

    @Query("UPDATE widget_bindings SET counter_id = NULL WHERE counter_id = :counterId")
    suspend fun setCounterNull(counterId: Long)
}
