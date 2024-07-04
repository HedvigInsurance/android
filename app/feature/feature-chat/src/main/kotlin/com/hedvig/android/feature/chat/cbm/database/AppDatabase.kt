package com.hedvig.android.feature.chat.cbm.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hedvig.android.feature.chat.cbm.database.converter.InstantConverter
import com.hedvig.android.feature.chat.cbm.database.converter.UuidConverter

@Database(
  entities = [ConversationEntity::class, ChatMessageEntity::class, RemoteKeyEntity::class],
  version = 1,
)
@TypeConverters(InstantConverter::class, UuidConverter::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun chatDao(): ChatDao

  abstract fun remoteKeyDao(): RemoteKeyDao
}
