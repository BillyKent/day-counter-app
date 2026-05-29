package com.daycounter.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.daycounter.domain.model.PastStreakRecord
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "past_streak_records",
    foreignKeys = [
        ForeignKey(
            entity = CounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["counter_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["counter_id", "end_date"]),
    ],
)
data class PastStreakRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "counter_id")
    val counterId: Long,
    @ColumnInfo(name = "streak_days")
    val streakDays: Int,
    @ColumnInfo(name = "reason")
    val reason: String,
    @ColumnInfo(name = "end_date")
    val endDate: LocalDate,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
)

/** Maps a persisted [PastStreakRecordEntity] to its [PastStreakRecord] domain model. */
fun PastStreakRecordEntity.toDomain(): PastStreakRecord = PastStreakRecord(
    id = id,
    counterId = counterId,
    streakDays = streakDays,
    reason = reason,
    endDate = endDate,
    createdAt = createdAt,
)

/** Maps a [PastStreakRecord] domain model to its [PastStreakRecordEntity] for persistence. */
fun PastStreakRecord.toEntity(): PastStreakRecordEntity = PastStreakRecordEntity(
    id = id,
    counterId = counterId,
    streakDays = streakDays,
    reason = reason,
    endDate = endDate,
    createdAt = createdAt,
)
