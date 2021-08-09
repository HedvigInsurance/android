package com.hedvig.app.feature.embark.passages.datepicker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DatePickerParams(
    val messages: List<String>,
    val passageName: String,
    val storeKey: String,
    val placeholder: String,
    val label: String?,
    val link: String,
) : Parcelable
