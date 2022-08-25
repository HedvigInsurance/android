package com.hedvig.app.feature.profile.ui.tab

import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.common.Mapper
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import java.util.Locale

class ProfileQueryDataToProfileUiStateMapper(
  private val featureManager: FeatureManager,
  private val marketManager: MarketManager,
  private val localeManager: LocaleManager,
) : Mapper<ProfileQuery.Data, ProfileUiState> {

  override suspend fun map(from: ProfileQuery.Data): ProfileUiState {
    val cashbackFragment = from.cashback?.fragments?.cashbackFragment
    val charityState = if (featureManager.isFeatureEnabled(Feature.SHOW_CHARITY).not()) {
      CharityState.DontShow
    } else {
      CharityState.Show(cashbackFragment?.name)
    }
    val priceData = if (featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN)) {
      PaymentState.Show(
        monetaryMonthlyNet = from.insuranceCost.formatMonetaryMonthlyNet(localeManager.getJavaUtilLocale()),
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
      charityState = charityState,
      paymentState = priceData,
      cashbackUiState = CashbackUiState.fromDto(cashbackFragment),
      charityOptions = from.cashbackOptions.filterNotNull().map(CharityOption.Companion::fromDto),
    )
  }

  private fun ProfileQuery.InsuranceCost?.formatMonetaryMonthlyNet(locale: Locale): String {
    if (this == null) return ""
    val monetaryAmount = fragments.costFragment.monthlyNet.fragments.monetaryAmountFragment.toMonetaryAmount()
    return monetaryAmount.format(locale)
  }
}
