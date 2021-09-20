package com.hedvig.app.feature.offer.ui.changestartdate

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationStartDateTerminology
import com.hedvig.app.feature.offer.ui.OfferStartDate
import com.hedvig.app.feature.offer.ui.StartDateLabel
import java.time.LocalDate

fun OfferQuery.Inception1.toChangeDateBottomSheetData() = ChangeDateBottomSheetData(
    inceptions = asConcurrentInception?.let { concurrentInception ->
        concurrentInception.correspondingQuotes.map { quote ->
            ChangeDateBottomSheetData.Inception(
                title = quote.asCompleteQuote?.displayName
                    ?: throw IllegalArgumentException("Quote displayName not found"),
                quoteId = quote.asCompleteQuote?.id
                    ?: throw IllegalArgumentException("Quote id not found"),
                startDate = concurrentInception.startDate,
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
                startDate = inception.startDate,
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

fun OfferQuery.Inception1.getStartDate() = when {
    isSwitcher() && hasNoDate() -> OfferStartDate.WhenCurrentPlanExpires
    hasNoDate() -> OfferStartDate.AtDate(LocalDate.now())
    asConcurrentInception != null -> OfferStartDate.AtDate(asConcurrentInception?.startDate ?: LocalDate.now())
    asIndependentInceptions != null -> {
        val inception = asIndependentInceptions?.inceptions?.firstOrNull()
        val allStartDatesEqual = asIndependentInceptions?.inceptions?.all { it.startDate == inception?.startDate }
        if (allStartDatesEqual == true) {
            OfferStartDate.AtDate(inception?.startDate ?: LocalDate.now())
        } else {
            OfferStartDate.Multiple
        }
    }
    else -> throw IllegalArgumentException("Could not parse inception")
}

private fun OfferQuery.Inception1.hasNoDate(): Boolean {
    return (asConcurrentInception != null && asConcurrentInception?.startDate == null) ||
        (asIndependentInceptions != null && asIndependentInceptions?.inceptions?.all { it.startDate == null } == true)
}

private fun OfferQuery.Inception1.isSwitcher(): Boolean {
    return (
        asIndependentInceptions?.inceptions?.all {
            it.currentInsurer?.fragments?.currentInsurerFragment?.switchable == true
        } == true
        ) ||
        (asConcurrentInception?.currentInsurer?.fragments?.currentInsurerFragment?.switchable == true)
}

fun OfferQuery.Inception1.getStartDateLabel(
    startDateTerminology: QuoteBundleAppConfigurationStartDateTerminology
) = when (startDateTerminology) {
    QuoteBundleAppConfigurationStartDateTerminology.START_DATE -> {
        when {
            asIndependentInceptions?.inceptions?.size == 1 -> StartDateLabel.SINGLE_START_DATE
            asIndependentInceptions != null -> StartDateLabel.MULTIPLE_START_DATES
            asConcurrentInception != null -> StartDateLabel.SINGLE_START_DATE
            else -> StartDateLabel.SINGLE_START_DATE
        }
    }
    QuoteBundleAppConfigurationStartDateTerminology.ACCESS_DATE -> StartDateLabel.ACCESS_DATE
    QuoteBundleAppConfigurationStartDateTerminology.UNKNOWN__ -> StartDateLabel.SINGLE_START_DATE
}
