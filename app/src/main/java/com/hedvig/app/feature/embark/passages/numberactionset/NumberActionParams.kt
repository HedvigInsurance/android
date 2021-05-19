package com.hedvig.app.feature.embark.passages.numberactionset

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NumberActionParams(
    val messages: List<String>,
    val passageName: String,
    val numberActions: List<NumberAction>,
    val link: String,
    val submitLabel: String,
) : Parcelable {
    @Parcelize
    data class NumberAction(
        val key: String,
        val title: String?,
        val placeholder: String,
        val unit: String?,
        val maxValue: Int?,
        val minValue: Int?
    ) : Parcelable
}
