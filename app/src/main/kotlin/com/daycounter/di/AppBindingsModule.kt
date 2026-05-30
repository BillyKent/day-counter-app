package com.daycounter.di

import com.daycounter.data.work.DailyReminderNotifier
import com.daycounter.data.work.MilestoneNotifier
import com.daycounter.data.work.WidgetRefresher
import com.daycounter.notifications.AndroidDailyReminderNotifier
import com.daycounter.notifications.AndroidMilestoneNotifier
import com.daycounter.widgets.WidgetRefresherImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {

    @Binds
    @Singleton
    abstract fun bindMilestoneNotifier(impl: AndroidMilestoneNotifier): MilestoneNotifier

    @Binds
    @Singleton
    abstract fun bindWidgetRefresher(impl: WidgetRefresherImpl): WidgetRefresher

    @Binds
    @Singleton
    abstract fun bindDailyReminderNotifier(impl: AndroidDailyReminderNotifier): DailyReminderNotifier
}
