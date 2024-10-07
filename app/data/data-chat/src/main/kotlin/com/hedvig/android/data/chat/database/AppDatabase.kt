package com.hedvig.android.data.chat.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.hedvig.android.data.chat.database.converter.InstantConverter
import com.hedvig.android.data.chat.database.converter.TierQuoteTypeConverter
import com.hedvig.android.data.chat.database.converter.UuidConverter

@Database(
  entities = [ChatMessageEntity::class, RemoteKeyEntity::class, ChangeTierQuoteEntity::class],
  version = 4,
  autoMigrations = [
    AutoMigration(from = 1, to = 2, spec = Migration1To2::class),
    AutoMigration(from = 2, to = 3),
    AutoMigration(from = 3, to = 4),
  ],
)
@TypeConverters(InstantConverter::class, UuidConverter::class, TierQuoteTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun chatDao(): ChatDao

  abstract fun remoteKeyDao(): RemoteKeyDao

  abstract fun tierQuoteDao(): TierQuoteDao
}

@DeleteTable(tableName = "conversations")
internal class Migration1To2 : AutoMigrationSpec
