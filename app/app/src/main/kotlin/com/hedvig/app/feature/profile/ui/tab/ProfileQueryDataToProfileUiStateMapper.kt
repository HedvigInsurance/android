package com.hedvig.app.feature.profile.ui.tab

import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.app.common.Mapper
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import giraffe.ProfileQuery
import java.util.Locale

class ProfileQueryDataToProfileUiStateMapper(
  private val featureManager: FeatureManager,
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) : Mapper<ProfileQuery.Data, ProfileUiState> {

  override suspend fun map(from: ProfileQuery.Data): ProfileUiState {
    val priceData = if (featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN)) {
      PaymentState.Show(
        monetaryMonthlyNet = from.chargeEstimation.charge.formatMonetaryMonthlyNet(languageService.getLocale()),
        priceCaptionResId = marketManager.market?.getPriceCaption(
          from.bankAccount?.directDebitStatus,
          from.activePaymentMethodsV2?.fragments?.activePaymentMethodsFragment,
        ),
      )
    } else {
      PaymentState.DontShow
    }
    return ProfileUiState(
      member = Member.fromDto(from.member),
      contactInfoName = "${from.member.firstName} ${from.member.lastName}",
      showBusinessModel = featureManager.isFeatureEnabled(Feature.SHOW_BUSINESS_MODEL),
      paymentState = priceData,
    )
  }

  private fun ProfileQuery.Charge.formatMonetaryMonthlyNet(locale: Locale): String {
    val monetaryAmount = fragments.monetaryAmountFragment.toMonetaryAmount()
    return monetaryAmount.format(locale)
  }
}
