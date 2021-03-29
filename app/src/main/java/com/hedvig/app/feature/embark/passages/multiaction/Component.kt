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
        val selectedDropDown: String,
        val input: String,
        val inputUnit: String,
        val switch: Boolean,
    ) : MultiAction(), Parcelable
}

