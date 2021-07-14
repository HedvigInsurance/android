package com.hedvig.app.feature.table

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Table(
    val title: String,
    val sections: List<Section>,
) : Parcelable {
    @Parcelize
    data class Section(
        val title: String,
        val rows: List<Row>,
    ) : Parcelable

    @Parcelize
    data class Row(
        val title: String,
        val subtitle: String?,
        val value: String,
    ) : Parcelable
}
