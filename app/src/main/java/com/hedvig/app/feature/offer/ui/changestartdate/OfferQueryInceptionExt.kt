package com.hedvig.app.feature.offer.ui.changestartdate

import com.hedvig.android.owldroid.fragment.QuoteBundleFragment

fun QuoteBundleFragment.Inception1.toChangeDateBottomSheetData() = ChangeDateBottomSheetData(
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
