package com.wakkt.app.di

import android.content.Context
import androidx.room.Room
import com.wakkt.app.data.LabelDao
import com.wakkt.app.data.WakktDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WakktDatabase =
        Room.databaseBuilder(context, WakktDatabase::class.java, "wakkt.db")
            .addMigrations(WakktDatabase.MIGRATION_1_2, WakktDatabase.MIGRATION_2_3, WakktDatabase.MIGRATION_3_4)
            .build()

    @Provides
    fun provideLabelDao(db: WakktDatabase): LabelDao = db.labelDao()
}
