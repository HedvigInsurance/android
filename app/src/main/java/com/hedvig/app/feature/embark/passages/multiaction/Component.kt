package com.hedvig.app.feature.embark.passages.multiaction

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class MultiAction {
    data class AddButton(
        val onClick: () -> Unit
    ) : MultiAction()

    @Parcelize
    data class Component(
        val id: Long,
        val selectedDropDown: KeyValue,
        val input: KeyValue,
        val switch: Boolean,
    ) : MultiAction(), Parcelable {

        @Parcelize
        data class KeyValue(
            val key: String,
            val value: String,
        ) : Parcelable
    }
}

