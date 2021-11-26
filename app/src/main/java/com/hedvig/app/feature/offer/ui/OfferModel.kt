package com.hedvig.app.feature.offer.ui

import android.content.Context
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationApproveButtonTerminology
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.R
import com.hedvig.app.feature.embark.masking.ISO_8601_DATE
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.offer.ui.changestartdate.ChangeDateBottomSheetData
import com.hedvig.app.feature.table.Table
import com.hedvig.app.util.extensions.isToday
import java.time.LocalDate
import javax.money.MonetaryAmount

sealed class OfferModel {

    data class Header(
        val title: String?,
        val startDate: OfferStartDate,
        val startDateLabel: StartDateLabel,
        val checkoutLabel: CheckoutLabel,
        val premium: MonetaryAmount,
        val hasDiscountedPrice: Boolean,
        val originalPremium: MonetaryAmount,
        val incentiveDisplayValue: List<String>,
        val hasCampaigns: Boolean,
        val changeDateBottomSheetData: ChangeDateBottomSheetData,
        val signMethod: SignMethod,
        val approveButtonTerminology: QuoteBundleAppConfigurationApproveButtonTerminology,
        val showCampaignManagement: Boolean,
        val ignoreCampaigns: Boolean,
        val gradientType: GradientType,
    ) : OfferModel()

    data class Facts(
        val table: Table,
    ) : OfferModel()

    data class CurrentInsurer(
        val displayName: String?,
        val associatedQuote: String?,
    ) : OfferModel()

    data class Footer(
        val checkoutLabel: CheckoutLabel,
    ) : OfferModel()

    sealed class Subheading : OfferModel() {
        object Coverage : Subheading()
        data class Switcher(val amountOfCurrentInsurers: Int) : Subheading()
    }

    sealed class Paragraph : OfferModel() {
        object Coverage : Paragraph()
    }

    sealed class InsurelyCard : OfferModel() {
        abstract val insuranceProvider: String?

        data class Loading(override val insuranceProvider: String?) : InsurelyCard()
        data class FailedToRetrieve(override val insuranceProvider: String?) : InsurelyCard()
        data class Retrieved(
            override val insuranceProvider: String?,
            val currentInsurances: List<CurrentInsurance>,
            val cheaperBy: MonetaryAmount?,
        ) : InsurelyCard() {
            data class CurrentInsurance(
                val name: String,
                val amount: MonetaryAmount,
            )
        }
    }

    data class QuoteDetails(
        val name: String,
        val id: String,
    ) : OfferModel()

    data class FAQ(
        val items: List<FAQItem>,
    ) : OfferModel()

    object AutomaticSwitchCard : OfferModel()
    object ManualSwitchCard : OfferModel()
    object Error : OfferModel()
}

sealed class OfferStartDate {
    object WhenCurrentPlanExpires : OfferStartDate()
    object Multiple : OfferStartDate()
    data class AtDate(val date: LocalDate) : OfferStartDate()
}

fun OfferStartDate.getString(context: Context): String? = when (this) {
    is OfferStartDate.AtDate -> if (date.isToday()) {
        context.getString(R.string.START_DATE_TODAY)
    } else {
        date.format(ISO_8601_DATE)
    }
    OfferStartDate.Multiple -> context.getString(R.string.OFFER_START_DATE_MULTIPLE)
    OfferStartDate.WhenCurrentPlanExpires -> context.getString(R.string.START_DATE_EXPIRES)
}
