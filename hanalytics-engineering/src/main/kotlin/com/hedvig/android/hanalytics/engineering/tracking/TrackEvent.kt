package com.hedvig.android.hanalytics.engineering.tracking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
internal data class TrackEvent(
  val name: String,
  val propertiesJsonString: String?,
  val timestamp: LocalDateTime,
) : Parcelable
