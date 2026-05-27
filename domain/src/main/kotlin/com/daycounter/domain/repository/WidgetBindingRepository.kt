package com.daycounter.domain.repository

import com.daycounter.domain.model.WidgetBinding

/**
 * Repository for [WidgetBinding] persistence. Implementations live in `:data`.
 */
interface WidgetBindingRepository {
    suspend fun get(widgetId: Int): WidgetBinding?

    suspend fun getAll(): List<WidgetBinding>

    suspend fun getAllForCounter(counterId: Long): List<WidgetBinding>

    suspend fun insert(binding: WidgetBinding)

    suspend fun update(binding: WidgetBinding)

    suspend fun delete(widgetId: Int)

    /** Sets counter_id = NULL for all bindings tied to the given counter. */
    suspend fun setCounterNull(counterId: Long)
}
