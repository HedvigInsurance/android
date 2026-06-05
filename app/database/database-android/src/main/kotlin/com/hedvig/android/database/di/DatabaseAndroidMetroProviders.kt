package com.hedvig.android.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DatabaseFile
import com.hedvig.android.core.common.di.IoDispatcher
import com.hedvig.android.database.AppDatabase
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.io.File
import kotlin.coroutines.CoroutineContext

@ContributesTo(AppScope::class)
interface DatabaseAndroidMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideRoomDatabaseBuilder(
    applicationContext: Context,
    @DatabaseFile dbFile: File,
    @IoDispatcher ioDispatcher: CoroutineContext,
  ): RoomDatabase.Builder<AppDatabase> = Room
    .databaseBuilder<AppDatabase>(applicationContext, dbFile.absolutePath)
    .setQueryCoroutineContext(ioDispatcher)
}
