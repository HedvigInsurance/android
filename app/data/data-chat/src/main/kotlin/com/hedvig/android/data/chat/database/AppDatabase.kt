package com.hedvig.android.data.chat.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hedvig.android.data.chat.database.converter.InstantConverter
import com.hedvig.android.data.chat.database.converter.UuidConverter

@Database(
  entities = [ConversationEntity::class, ChatMessageEntity::class, RemoteKeyEntity::class],
  version = 1,
)
@TypeConverters(InstantConverter::class, UuidConverter::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun chatDao(): ChatDao

  abstract fun remoteKeyDao(): RemoteKeyDao

  abstract fun conversationDao(): ConversationDao
}
