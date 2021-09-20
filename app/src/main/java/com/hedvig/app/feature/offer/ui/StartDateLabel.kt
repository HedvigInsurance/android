package com.hedvig.app.feature.offer.ui

import android.content.Context
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
