package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MultiActionParams(
    val messages: List<String>,
    val passageName: String,
    val components: List<MultiActionComponent>
) : Parcelable

@Parcelize
data class MultiActionComponent(
    val dropdown: Dropdown?,
    val switch: Switch?,
    val number: Number?
) : Parcelable

@Parcelize
data class Dropdown(
    val key: String,
    val label: String,
    val options: List<Option>
) : Parcelable

@Parcelize
data class Option(
    val text: String,
    val label: String
) : Parcelable

@Parcelize
data class Switch(
    val text: String,
    val label: String,
    val defaultValue: Boolean
) : Parcelable

@Parcelize
data class Number(
    val key: String,
    val label: String?,
    val placeholder: String,
    val unit: String?,
    val maxValue: Int?,
    val minValue: Int?,
    val link: String,
    val submitLabel: String,
) : Parcelable

