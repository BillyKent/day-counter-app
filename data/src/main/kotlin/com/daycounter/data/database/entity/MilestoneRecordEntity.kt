package com.daycounter.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "milestone_records",
    foreignKeys = [
        ForeignKey(
            entity = CounterEntity::class,
            parentColumns = ["id"],
            childColumns = ["counter_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["counter_id", "milestone_days"], unique = true),
        Index(value = ["counter_id"]),
    ],
)
data class MilestoneRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "counter_id")
    val counterId: Long,
    @ColumnInfo(name = "milestone_days")
    val milestoneDays: Int,
    @ColumnInfo(name = "notified_at")
    val notifiedAt: Instant,
)
