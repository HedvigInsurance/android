package com.hedvig.android.database.di

import androidx.room.RoomDatabase
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.database.AppDatabase
import org.koin.dsl.module

val databaseModule = module {
  single<AppDatabase> {
    get<RoomDatabase.Builder<AppDatabase>>().build()
  }
  single<RoomDatabase> {
    get<AppDatabase>()
  }
  single<ChatDao> {
    get<AppDatabase>().chatDao()
  }
  single<RemoteKeyDao> {
    get<AppDatabase>().remoteKeyDao()
  }
}
