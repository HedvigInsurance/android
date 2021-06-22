package com.hedvig.app.feature.offer.ui.changestartdate

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferStartDate
import com.hedvig.app.feature.offer.ui.OfferStartDateLabel
import java.lang.IllegalArgumentException
import java.time.LocalDate

fun OfferQuery.Inception1.toChangeDateBottomSheetData() = ChangeDateBottomSheetData(
    inceptions = asConcurrentInception?.let { concurrentInception ->
        concurrentInception.correspondingQuotes.map { quote ->
            ChangeDateBottomSheetData.Inception(
                title = quote.asCompleteQuote?.displayName
                    ?: throw IllegalArgumentException("Quote displayName not found"),
                quoteId = quote.asCompleteQuote?.id
                    ?: throw IllegalArgumentException("Quote id not found"),
                startDate = concurrentInception.startDate ?: LocalDate.now(),
                currentInsurer = concurrentInception.currentInsurer?.fragments?.currentInsurerFragment?.let {
                    ChangeDateBottomSheetData.CurrentInsurer(
                        id = it.id
                            ?: throw IllegalArgumentException("Current insurer id not found"),
                        displayName = it.displayName
                            ?: throw IllegalArgumentException("Current insurer display name not found"),
                        switchable = it.switchable
                            ?: throw IllegalArgumentException("Current insurer switchable not found"),
                    )
                },
                isConcurrent = true
            )
        }
    } ?: asIndependentInceptions?.let { independentInceptions ->
        independentInceptions.inceptions.map { inception ->
            ChangeDateBottomSheetData.Inception(
                title = inception.correspondingQuote.asCompleteQuote1?.displayName
                    ?: throw IllegalArgumentException("Quote displayName not found"),
                quoteId = inception.correspondingQuote.asCompleteQuote1?.id
                    ?: throw IllegalArgumentException("Quote id not found"),
                startDate = inception.startDate ?: LocalDate.now(),
                currentInsurer = inception.currentInsurer?.fragments?.currentInsurerFragment?.let {
                    ChangeDateBottomSheetData.CurrentInsurer(
                        id = it.id
                            ?: throw IllegalArgumentException("Current insurer id not found"),
                        displayName = it.displayName
                            ?: throw IllegalArgumentException("Current insurer display name not found"),
                        switchable = it.switchable
                            ?: throw IllegalArgumentException("Current insurer switchable not found"),
                    )
                },
                isConcurrent = false
            )
        }
    } ?: throw IllegalArgumentException("Could not parse inception")
)

fun OfferQuery.Inception1.getStartDate(): OfferStartDate {
    if (hasNoDate()) {
        return OfferStartDate.WhenCurrentPlanExpires
    }

    return when {
        asConcurrentInception != null -> OfferStartDate.AtDate(asConcurrentInception?.startDate ?: LocalDate.now())
        asIndependentInceptions != null -> {
            val inception = asIndependentInceptions?.inceptions?.firstOrNull()
            val allStartDatesEqual = asIndependentInceptions?.inceptions?.all { it.startDate == inception?.startDate }
            if (allStartDatesEqual == true) {
                OfferStartDate.AtDate(inception?.startDate ?: LocalDate.now())
            } else {
                OfferStartDate.None
            }
        }
        else -> throw IllegalArgumentException("Could not parse inception")
    }
}

fun OfferQuery.Inception1.hasNoDate(): Boolean {
    return (asConcurrentInception != null && asConcurrentInception?.startDate == null) ||
        (asIndependentInceptions != null && asIndependentInceptions?.inceptions?.all { it.startDate == null } == true)
}

fun OfferQuery.Inception1.getStartDateLabel(): OfferStartDateLabel {
    return if (asIndependentInceptions != null) {
        OfferStartDateLabel.Multiple
    } else {
        OfferStartDateLabel.Single
    }
}
