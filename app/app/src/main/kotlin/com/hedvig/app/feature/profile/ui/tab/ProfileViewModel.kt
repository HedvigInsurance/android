package com.hedvig.app.feature.profile.ui.tab

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.errorprone.annotations.Immutable
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import javax.money.MonetaryAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ProfileViewModel(
  private val profileRepository: ProfileRepository,
  private val getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  private val featureManager: FeatureManager,
  private val marketManager: MarketManager,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  private val retryChannel = RetryChannel()

  private val _data = MutableStateFlow(ProfileUiState())
  val data: StateFlow<ProfileUiState> = _data

  init {
    viewModelScope.launch {
      profileRepository.profile().fold(
        ifLeft = { _data.update { it.copy(errorMessage = it.errorMessage) } },
        ifRight = { profile ->
          _data.update {
            it.copy(
              contactInfoName = "${profile.member.firstName} ${profile.member.lastName}",
              paymentInfo = PaymentInfo(
                monetaryMonthlyNet = profile.chargeEstimation.charge,
                priceCaptionResId = marketManager.market?.let(profile::getPriceCaption)
              )
            )
          }
        },
      )

      getEuroBonusStatusUseCase.invoke().fold(
        ifLeft = { _data.update { it.copy(errorMessage = it.errorMessage) } },
        ifRight = { euroBonus -> _data.update { it.copy(euroBonus = euroBonus) } },
      )

      _data.update { it.copy(showBusinessModel = featureManager.isFeatureEnabled(Feature.SHOW_BUSINESS_MODEL)) }
    }
  }

  fun reload() {
    retryChannel.retry()
  }

  fun onLogout() {
    logoutUseCase.invoke()
  }
}

internal data class ProfileUiState(
  val contactInfoName: String? = null,
  val paymentInfo: PaymentInfo? = null,
  val euroBonus: EuroBonus? = null,
  val showBusinessModel: Boolean = false,
  val errorMessage: String? = null,
  val isLoading: Boolean = false
)

@Immutable
internal data class PaymentInfo(
  val monetaryMonthlyNet: MonetaryAmount,
  @StringRes val priceCaptionResId: Int?,
)
