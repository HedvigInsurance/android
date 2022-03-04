package com.hedvig.app.feature.offer.model.quotebundle

import android.content.Context
import com.hedvig.android.owldroid.fragment.QuoteBundleFragment
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationStartDateTerminology
import com.hedvig.app.R

enum class StartDateLabel {
    SINGLE_START_DATE,
    MULTIPLE_START_DATES,
    ACCESS_DATE
}

fun StartDateLabel.toString(context: Context) = when (this) {
    StartDateLabel.SINGLE_START_DATE -> context.getString(R.string.OFFER_START_DATE)
    StartDateLabel.MULTIPLE_START_DATES -> context.getString(R.string.OFFER_START_DATE_PLURAL)
    StartDateLabel.ACCESS_DATE -> context.getString(R.string.OFFER_ACCESS_DATE)
}

fun QuoteBundleFragment.Inception1.getStartDateLabel(
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
