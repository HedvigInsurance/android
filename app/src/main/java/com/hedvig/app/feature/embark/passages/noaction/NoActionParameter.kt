package com.hedvig.app.feature.embark.passages.noaction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoActionParameter(
    val messages: List<String>,
) : Parcelable
