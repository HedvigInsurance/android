package com.hedvig.app.feature.offer.ui.changestartdate

import com.hedvig.android.core.common.android.QuoteCartId
import giraffe.fragment.QuoteBundleFragment

fun QuoteBundleFragment.Inception1.toChangeDateBottomSheetData(
  quoteCartId: QuoteCartId,
  quoteNames: List<String>,
) = ChangeDateBottomSheetData(
  quoteCartId = quoteCartId,
  inceptions = asConcurrentInception?.let { concurrentInception ->
    concurrentInception.correspondingQuoteIds.mapIndexed { index, quoteId ->
      ChangeDateBottomSheetData.Inception(
        title = quoteNames.getOrElse(index) { "Insurance #$index" },
        quoteId = quoteId,
        startDate = concurrentInception.startDate,
        currentInsurer = concurrentInception.currentInsurer?.fragments?.currentInsurerFragment?.let {
          ChangeDateBottomSheetData.CurrentInsurer(
            id = it.id
              ?: error("Current insurer id not found"),
            displayName = it.displayName ?: "",
            switchable = it.switchable
              ?: error("Current insurer switchable not found"),
          )
        },
        isConcurrent = true,
      )
    }
  } ?: asIndependentInceptions?.let { independentInceptions ->
    independentInceptions.inceptions.mapIndexed { index, inception ->
      ChangeDateBottomSheetData.Inception(
        title = quoteNames.getOrElse(index) { "Insurance #$index" },
        quoteId = inception.correspondingQuoteId,
        startDate = inception.startDate,
        currentInsurer = inception.currentInsurer?.fragments?.currentInsurerFragment?.let {
          ChangeDateBottomSheetData.CurrentInsurer(
            id = it.id
              ?: error("Current insurer id not found"),
            displayName = it.displayName ?: "",
            switchable = it.switchable
              ?: error("Current insurer switchable not found"),
          )
        },
        isConcurrent = false,
      )
    }
  } ?: error("Could not parse inception"),
)
