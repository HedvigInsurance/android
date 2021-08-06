package com.hedvig.app.feature.tracking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class TrackEvent(
    val name: String,
    val propertiesJsonString: String?,
    val timestamp: LocalDateTime,
) : Parcelable
