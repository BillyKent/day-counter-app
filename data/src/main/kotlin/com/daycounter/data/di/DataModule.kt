package com.daycounter.data.di

import android.content.Context
import androidx.room.Room
import com.daycounter.data.database.AppDatabase
import com.daycounter.data.database.dao.CounterDao
import com.daycounter.data.database.dao.MilestoneRecordDao
import com.daycounter.data.database.dao.PastStreakRecordDao
import com.daycounter.data.database.dao.WidgetBindingDao
import com.daycounter.data.repository.CounterRepositoryImpl
import com.daycounter.data.repository.MilestoneRepositoryImpl
import com.daycounter.data.repository.PastStreakRepositoryImpl
import com.daycounter.data.repository.WidgetBindingRepositoryImpl
import com.daycounter.domain.repository.CounterRepository
import com.daycounter.domain.repository.MilestoneRepository
import com.daycounter.domain.repository.PastStreakRepository
import com.daycounter.domain.repository.WidgetBindingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        AppDatabase.DATABASE_NAME,
    )
        // Fresh-install assumption (spec Clarification Q1 / plan Complexity Tracking): no written
        // migration — the schema bump to v2 drops and recreates on incompatible versions.
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideCounterDao(db: AppDatabase): CounterDao = db.counterDao()

    @Provides
    fun provideMilestoneRecordDao(db: AppDatabase): MilestoneRecordDao = db.milestoneRecordDao()

    @Provides
    fun providePastStreakRecordDao(db: AppDatabase): PastStreakRecordDao = db.pastStreakRecordDao()

    @Provides
    fun provideWidgetBindingDao(db: AppDatabase): WidgetBindingDao = db.widgetBindingDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCounterRepository(impl: CounterRepositoryImpl): CounterRepository

    @Binds
    @Singleton
    abstract fun bindMilestoneRepository(impl: MilestoneRepositoryImpl): MilestoneRepository

    @Binds
    @Singleton
    abstract fun bindPastStreakRepository(impl: PastStreakRepositoryImpl): PastStreakRepository

    @Binds
    @Singleton
    abstract fun bindWidgetBindingRepository(impl: WidgetBindingRepositoryImpl): WidgetBindingRepository
}
