package com.hedvig.app.feature.offer.ui.changestartdate

import android.os.Parcelable
import java.time.LocalDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeDateBottomSheetData(
    val inceptions: List<Inception>
) : Parcelable {

    @IgnoredOnParcel
    val idsInBundle: List<String> = inceptions.map { it.quoteId }

    @Parcelize
    data class Inception(
        val title: String,
        val quoteId: String,
        val startDate: LocalDate,
        val currentInsurer: CurrentInsurer?,
        val isConcurrent: Boolean
    ) : Parcelable

    @Parcelize
    data class CurrentInsurer(
        val id: String,
        val displayName: String,
        val switchable: Boolean
    ) : Parcelable
}
