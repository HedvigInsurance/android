package com.hedvig.android.database.di

import androidx.room.RoomDatabase
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.database.AppDatabase
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface DatabaseMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase = builder.build()

  @Provides
  @SingleIn(AppScope::class)
  fun provideRoomDatabase(appDatabase: AppDatabase): RoomDatabase = appDatabase

  @Provides
  @SingleIn(AppScope::class)
  fun provideChatDao(appDatabase: AppDatabase): ChatDao = appDatabase.chatDao()

  @Provides
  @SingleIn(AppScope::class)
  fun provideRemoteKeyDao(appDatabase: AppDatabase): RemoteKeyDao = appDatabase.remoteKeyDao()
}
