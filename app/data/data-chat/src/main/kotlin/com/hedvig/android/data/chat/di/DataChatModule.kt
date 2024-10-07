package com.hedvig.android.data.chat.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hedvig.android.core.common.di.databaseFileQualifier
import com.hedvig.android.core.common.di.ioDispatcherQualifier
import com.hedvig.android.data.chat.database.AppDatabase
import com.hedvig.android.data.chat.database.ChatDao
import com.hedvig.android.data.chat.database.RemoteKeyDao
import com.hedvig.android.data.chat.database.TierQuoteDao
import java.io.File
import kotlin.coroutines.CoroutineContext
import org.koin.dsl.module

val dataChatModule = module {
  single<RoomDatabase.Builder<AppDatabase>> {
    val dbFile = get<File>(databaseFileQualifier)
    val applicationContext = get<Context>()
    Room
      .databaseBuilder<AppDatabase>(applicationContext, dbFile.absolutePath)
      .setQueryCoroutineContext(get<CoroutineContext>(ioDispatcherQualifier))
  }
  single<AppDatabase> {
    get<RoomDatabase.Builder<AppDatabase>>().build()
  }
  single<ChatDao> {
    get<AppDatabase>().chatDao()
  }
  single<TierQuoteDao> {
    get<AppDatabase>().tierQuoteDao()
  }
  single<RemoteKeyDao> {
    get<AppDatabase>().remoteKeyDao()
  }
}
