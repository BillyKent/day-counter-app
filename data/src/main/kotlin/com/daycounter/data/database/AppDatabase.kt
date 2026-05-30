package com.daycounter.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.daycounter.data.database.converter.Converters
import com.daycounter.data.database.dao.CounterDao
import com.daycounter.data.database.dao.MilestoneRecordDao
import com.daycounter.data.database.dao.PastStreakRecordDao
import com.daycounter.data.database.dao.PausePeriodDao
import com.daycounter.data.database.dao.WidgetBindingDao
import com.daycounter.data.database.entity.CounterEntity
import com.daycounter.data.database.entity.MilestoneRecordEntity
import com.daycounter.data.database.entity.PastStreakRecordEntity
import com.daycounter.data.database.entity.PausePeriodEntity
import com.daycounter.data.database.entity.WidgetBindingEntity

@Database(
    entities = [
        CounterEntity::class,
        MilestoneRecordEntity::class,
        PastStreakRecordEntity::class,
        PausePeriodEntity::class,
        WidgetBindingEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun counterDao(): CounterDao
    abstract fun milestoneRecordDao(): MilestoneRecordDao
    abstract fun pastStreakRecordDao(): PastStreakRecordDao
    abstract fun pausePeriodDao(): PausePeriodDao
    abstract fun widgetBindingDao(): WidgetBindingDao

    companion object {
        const val DATABASE_NAME = "day_counter.db"
    }
}
