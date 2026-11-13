package com.wakkt.app.di

import com.wakkt.app.overlay.OverlayController
import com.wakkt.app.overlay.WindowOverlayController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OverlayModule {
    @Binds
    @Singleton
    abstract fun bindOverlayController(impl: WindowOverlayController): OverlayController
}
