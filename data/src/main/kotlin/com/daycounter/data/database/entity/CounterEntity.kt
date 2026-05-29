package com.daycounter.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.daycounter.domain.model.Counter
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "counters")
data class CounterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "goal_name")
    val goalName: String,
    @ColumnInfo(name = "start_date")
    val startDate: LocalDate,
    @ColumnInfo(name = "created_at")
    val createdAt: Instant,
    @ColumnInfo(name = "category")
    val category: String? = null,
    @ColumnInfo(name = "goal_milestone_target", defaultValue = "30")
    val goalMilestoneTarget: Int = Counter.DEFAULT_GOAL_TARGET,
)

/** Maps a persisted [CounterEntity] to its pure-Kotlin [Counter] domain model. */
fun CounterEntity.toDomain(): Counter = Counter(
    id = id,
    goalName = goalName,
    startDate = startDate,
    createdAt = createdAt,
    category = category,
    goalMilestoneTarget = goalMilestoneTarget,
)

/** Maps a [Counter] domain model to its [CounterEntity] for persistence. */
fun Counter.toEntity(): CounterEntity = CounterEntity(
    id = id,
    goalName = goalName,
    startDate = startDate,
    createdAt = createdAt,
    category = category,
    goalMilestoneTarget = goalMilestoneTarget,
)
