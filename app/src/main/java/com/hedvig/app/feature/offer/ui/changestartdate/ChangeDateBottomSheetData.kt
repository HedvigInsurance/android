package com.hedvig.app.feature.offer.ui.changestartdate

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.OfferQuery
import java.time.LocalDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChangeDateBottomSheetData(
    val inceptions: List<Inception>
) : Parcelable {

    constructor(quoteBundle: OfferQuery.QuoteBundle) : this(
        inceptions = quoteBundle.quotes.map {
            Inception(
                quoteId = it.id,
                startDate = it.startDate ?: LocalDate.now(),
                currentInsurer = it.currentInsurer?.let { currentInsurer ->
                    CurrentInsurer(
                        id = currentInsurer.id!!,
                        displayName = currentInsurer.displayName!!,
                        switchable = currentInsurer.switchable!!
                    )
                }
            )
        }
    )

    @IgnoredOnParcel
    val idsInBundle: List<String> = inceptions.map { it.quoteId }

    @Parcelize
    data class Inception(
        val quoteId: String,
        val startDate: LocalDate,
        val currentInsurer: CurrentInsurer?
    ) : Parcelable

    @Parcelize
    data class CurrentInsurer(
        val id: String,
        val displayName: String,
        val switchable: Boolean
    ) : Parcelable
}
