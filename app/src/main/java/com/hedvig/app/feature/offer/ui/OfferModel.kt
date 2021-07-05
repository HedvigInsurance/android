package com.hedvig.app.feature.offer.ui

import android.content.Context
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.R
import com.hedvig.app.feature.embark.masking.ISO_8601_DATE
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.table.Table
import com.hedvig.app.util.extensions.isToday
import java.time.LocalDate
import javax.money.MonetaryAmount

sealed class OfferModel {

    data class Header(
        val title: String?,
        val startDate: OfferStartDate,
        val startDateLabel: OfferStartDateLabel,
        val netMonthlyCost: MonetaryAmount,
        val grossMonthlyCost: MonetaryAmount,
        val incentiveDisplayValue: String?,
        val changeDateBottomSheetData: ChangeDateBottomSheetData,
        val signMethod: SignMethod
    ) : OfferModel()

    data class Facts(
        val table: Table,
    ) : OfferModel()

    data class Switcher(
        val displayName: String?,
    ) : OfferModel()

    data class Footer(
        val url: String,
    ) : OfferModel()

    object Loading : OfferModel()
}

sealed class OfferStartDate {
    object WhenCurrentPlanExpires : OfferStartDate()
    object None : OfferStartDate()
    data class AtDate(val date: LocalDate) : OfferStartDate()
}

enum class OfferStartDateLabel {
    Multiple, Single
}

fun OfferStartDate.getString(context: Context): String? = when (this) {
    is OfferStartDate.AtDate -> if (date.isToday()) {
        context.getString(R.string.START_DATE_TODAY)
    } else {
        date.format(ISO_8601_DATE)
    }
    OfferStartDate.WhenCurrentPlanExpires -> context.getString(R.string.ACTIVATE_INSURANCE_END_BTN)
    OfferStartDate.None -> null
}

fun OfferStartDateLabel.getString(context: Context): String = when (this) {
    OfferStartDateLabel.Multiple -> context.getString(R.string.OFFER_START_DATE_PLURAL)
    OfferStartDateLabel.Single -> context.getString(R.string.OFFER_START_DATE)
}
