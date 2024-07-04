package com.hedvig.android.feature.chat.cbm.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant

class InstantConverter {
  @TypeConverter
  fun parse(value: String): Instant {
    return Instant.parse(value)
  }

  @TypeConverter
  fun toString(value: Instant): String {
    return value.toString()
  }
}
