package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class MultiActionItem {
    data class AddButton(
        val label: String
    ) : MultiActionItem()

    @Parcelize
    data class Component(
        val id: Long,
        val selectedDropDowns: List<DropDown>,
        val inputs: List<Input>,
        val switches: List<Switch>,
    ) : MultiActionItem(), Parcelable

    @Parcelize
    data class DropDown(
        val key: String,
        val text: String,
        val value: String,
    ) : Parcelable

    @Parcelize
    data class Input(
        val key: String,
        val value: String,
        val unit: String,
    ) : Parcelable {
        override fun toString(): String {
            return "$value $unit"
        }
    }

    @Parcelize
    data class Switch(
        val key: String,
        val label: String,
        val value: Boolean,
    ) : Parcelable
}
