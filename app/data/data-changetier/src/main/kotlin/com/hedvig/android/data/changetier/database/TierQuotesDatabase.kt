package com.hedvig.android.data.changetier.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
  entities = [ChangeTierQuoteEntity::class],
  version = 1,
)
@TypeConverters(TierQuoteTypeConverter::class)
internal abstract class TierQuotesDatabase : RoomDatabase() {
  abstract fun tierQuoteDao(): TierQuoteDao
}
