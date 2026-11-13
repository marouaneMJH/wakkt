package com.wakkt.app.di

import com.wakkt.app.data.LabelRepository
import com.wakkt.app.data.RoomLabelRepository
import com.wakkt.app.settings.DataStoreSettingsRepository
import com.wakkt.app.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLabelRepository(impl: RoomLabelRepository): LabelRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository
}
