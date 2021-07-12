package com.hedvig.app.feature.offer.ui

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.R
import com.hedvig.app.feature.embark.masking.ISO_8601_DATE
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.offer.ui.faq.FAQItem
import com.hedvig.app.feature.table.Table
import com.hedvig.app.util.extensions.isToday
import java.time.LocalDate
import javax.money.MonetaryAmount

sealed class OfferModel {

    data class Header(
        val title: String?,
        val startDate: OfferStartDate,
        @StringRes
        val startDateLabel: Int,
        val netMonthlyCost: MonetaryAmount,
        val grossMonthlyCost: MonetaryAmount,
        val incentiveDisplayValue: List<String>,
        val hasCampaigns: Boolean,
        val changeDateBottomSheetData: ChangeDateBottomSheetData,
        val signMethod: SignMethod,
        val showCampaignManagement: Boolean,
        @DrawableRes
        val gradientRes: Int
    ) : OfferModel()

    data class Facts(
        val table: Table,
    ) : OfferModel()

    data class CurrentInsurer(
        val displayName: String?,
        val associatedQuote: String?,
    ) : OfferModel()

    data class Footer(
        val url: String,
    ) : OfferModel()

    sealed class Subheading : OfferModel() {
        object Coverage : Subheading()
        data class Switcher(val amountOfCurrentInsurers: Int) : Subheading()
    }

    sealed class Paragraph : OfferModel() {
        object Coverage : Paragraph()
    }

    data class QuoteDetails(
        val name: String,
        val id: String,
    ) : OfferModel()

    data class FAQ(
        val items: List<FAQItem>
    ) : OfferModel()

    object AutomaticSwitchCard : OfferModel()
    object ManualSwitchCard : OfferModel()
}

sealed class OfferStartDate {
    object WhenCurrentPlanExpires : OfferStartDate()
    object None : OfferStartDate()
    data class AtDate(val date: LocalDate) : OfferStartDate()
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
