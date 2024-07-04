package com.hedvig.android.feature.chat.cbm.database.converter

import androidx.room.TypeConverter
import com.benasher44.uuid.Uuid

class UuidConverter {
  @TypeConverter
  fun parse(value: String): Uuid {
    return Uuid.fromString(value)
  }

  @TypeConverter
  fun toString(value: Uuid): String {
    return value.toString()
  }
}
