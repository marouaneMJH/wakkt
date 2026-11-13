package com.wakkt.app.di

import android.content.Context
import com.wakkt.app.service.MonitorStarter
import com.wakkt.app.ui.MonitorControl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MonitorModule {
    @Provides
    fun provideMonitorControl(@ApplicationContext context: Context): MonitorControl = object : MonitorControl {
        override fun start() { MonitorStarter.start(context) }
        override fun stop() = MonitorStarter.stop(context)
    }
}
