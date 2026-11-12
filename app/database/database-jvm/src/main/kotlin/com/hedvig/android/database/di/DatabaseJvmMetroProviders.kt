package com.hedvig.android.database.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.DesktopDataDirectory
import com.hedvig.android.core.common.di.IoDispatcher
import com.hedvig.android.database.AppDatabase
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.io.File
import kotlin.coroutines.CoroutineContext

@ContributesTo(AppScope::class)
interface DatabaseJvmMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideRoomDatabaseBuilder(
    @DesktopDataDirectory dataDirectory: File,
    @IoDispatcher ioDispatcher: CoroutineContext,
  ): RoomDatabase.Builder<AppDatabase> = Room
    .databaseBuilder<AppDatabase>(File(dataDirectory, DATABASE_FILE_NAME).absolutePath)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(ioDispatcher)
}

/** The same file name the Android app uses for its database. */
internal const val DATABASE_FILE_NAME = "hedvig_chat_database.db"
