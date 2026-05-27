package com.daycounter.data.repository

import com.daycounter.data.database.dao.WidgetBindingDao
import com.daycounter.data.database.entity.WidgetBindingEntity
import com.daycounter.domain.model.WidgetBinding
import com.daycounter.domain.repository.WidgetBindingRepository
import javax.inject.Inject

class WidgetBindingRepositoryImpl @Inject constructor(
    private val dao: WidgetBindingDao,
) : WidgetBindingRepository {

    override suspend fun get(widgetId: Int): WidgetBinding? =
        dao.getByWidgetId(widgetId)?.toDomain()

    override suspend fun getAll(): List<WidgetBinding> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getAllForCounter(counterId: Long): List<WidgetBinding> =
        dao.getAllForCounter(counterId).map { it.toDomain() }

    override suspend fun insert(binding: WidgetBinding) {
        dao.insert(binding.toEntity())
    }

    override suspend fun update(binding: WidgetBinding) {
        dao.update(binding.toEntity())
    }

    override suspend fun delete(widgetId: Int) {
        dao.delete(widgetId)
    }

    override suspend fun setCounterNull(counterId: Long) {
        dao.setCounterNull(counterId)
    }

    private fun WidgetBindingEntity.toDomain() = WidgetBinding(widgetId, counterId)
    private fun WidgetBinding.toEntity() = WidgetBindingEntity(widgetId, counterId)
}
