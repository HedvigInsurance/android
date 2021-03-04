package com.hedvig.onboarding.createoffer.passages.datepicker

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DatePickerParams(
    val messages: List<String>,
    val passageName: String,
    val storeKey: String,
    val placeholder: String,
    val label: String?,
    val link: String,
) : Parcelable
