package com.wakkt.app.di

import com.wakkt.app.service.ForegroundAppDetector
import com.wakkt.app.service.UsageStatsForegroundAppDetector
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    @Singleton
    abstract fun bindForegroundAppDetector(impl: UsageStatsForegroundAppDetector): ForegroundAppDetector
}
