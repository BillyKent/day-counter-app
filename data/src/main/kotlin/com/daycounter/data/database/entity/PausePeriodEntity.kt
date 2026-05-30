package com.daycounter.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.daycounter.domain.model.PausePeriod
import java.time.LocalDate

@Entity(
    tableName = "pause_periods",
    foreignKeys = [
        ForeignKey(
            entity = CounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["counter_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["counter_id"]),
    ],
)
data class PausePeriodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "counter_id")
    val counterId: Long,
    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,
    @ColumnInfo(name = "end_date")
    val endDate: LocalDate,
)

/** Maps a persisted [PausePeriodEntity] to its [PausePeriod] domain model. */
fun PausePeriodEntity.toDomain(): PausePeriod = PausePeriod(
    id = id,
    counterId = counterId,
    startDate = startDate,
    endDate = endDate,
)

/** Maps a [PausePeriod] domain model to its [PausePeriodEntity] for persistence. */
fun PausePeriod.toEntity(): PausePeriodEntity = PausePeriodEntity(
    id = id,
    counterId = counterId,
    startDate = startDate,
    endDate = endDate,
)
