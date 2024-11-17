package com.hedvig.android.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hedvig.android.core.common.di.databaseFileQualifier
import com.hedvig.android.core.common.di.ioDispatcherQualifier
import com.hedvig.android.database.AppDatabase
import java.io.File
import kotlin.coroutines.CoroutineContext
import org.koin.dsl.module

val databaseAndroidModule = module {
  single<RoomDatabase.Builder<AppDatabase>> {
    val dbFile = get<File>(databaseFileQualifier)
    val applicationContext = get<Context>()
    Room
      .databaseBuilder<AppDatabase>(applicationContext, dbFile.absolutePath)
      .setQueryCoroutineContext(get<CoroutineContext>(ioDispatcherQualifier))
  }
}
