package com.hedvig.app.feature.profile.ui.tab

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.nullable
import com.google.errorprone.annotations.Immutable
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import giraffe.ProfileQuery
import giraffe.fragment.MonetaryAmountFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import slimber.log.d
import kotlin.time.Duration.Companion.seconds

internal class ProfileViewModel(
  private val profileRepository: ProfileRepository,
  private val getEuroBonusStatusUseCase: GetEuroBonusStatusUseCase,
  private val featureManager: FeatureManager,
  private val marketManager: MarketManager,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  private val retryChannel = RetryChannel()

  private val euroBonusLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val businessModelLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  private val profileLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val loading: StateFlow<Boolean> = combine(
    euroBonusLoading,
    businessModelLoading,
    profileLoading,
  ) { euroBonusLoading, businessModelLoading, profileLoading ->
    euroBonusLoading || businessModelLoading || profileLoading
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5.seconds),
    initialValue = false,
  )

  private val euroBonus: StateFlow<EuroBonus?> = retryChannel.transformLatest {
    euroBonusLoading.update { true }
    d { "Stelios, reloading;" }
    emit(
      getEuroBonusStatusUseCase.invoke()
        .onLeft { error -> d { "Euro bonus not showing because: $error" } }
        .getOrNull(),
    )
    euroBonusLoading.update { false }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5.seconds),
    initialValue = null,
  )

  private val businessModel: StateFlow<Boolean> = retryChannel.transformLatest {
    businessModelLoading.update { true }
    emit(featureManager.isFeatureEnabled(Feature.SHOW_BUSINESS_MODEL))
    businessModelLoading.update { false }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5.seconds),
    initialValue = false,
  )

  private val profileQuery: StateFlow<ProfileQuery.Data?> = retryChannel.transformLatest {
    profileLoading.update { true }
    emitAll(
      profileRepository.profile()
        .mapLatest { profileQueryDataResult ->
          val result = profileQueryDataResult
            .onLeft { d { "profileRepository.profile() failed with error:$it" } }
            .getOrNull()
          profileLoading.update { false }
          result
        },
    )
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = null,
    )

  val data: StateFlow<ProfileUiState> =
    combine(euroBonus, businessModel, profileQuery) { euroBonus, showBusinessModel, profileQueryData ->
      Triple(euroBonus, showBusinessModel, profileQueryData)
    }
      .mapLatest { (euroBonus: EuroBonus?, showBusinessModel: Boolean, profileQueryData: ProfileQuery.Data?) ->
        val paymentInfo: PaymentInfo? = nullable {
          ensure(featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN))
          ensureNotNull(profileQueryData)
          PaymentInfo(
            monetaryMonthlyNet = profileQueryData.chargeEstimation.charge.fragments.monetaryAmountFragment,
            priceCaptionResId = marketManager.market?.getPriceCaption(
              profileQueryData.bankAccount?.directDebitStatus,
              profileQueryData.activePaymentMethodsV2?.fragments?.activePaymentMethodsFragment,
            ),
          )
        }
        val contactInfoName = nullable {
          ensureNotNull(profileQueryData)
          "${profileQueryData.member.firstName} ${profileQueryData.member.lastName}"
        }
        ProfileUiState(
          contactInfoName = contactInfoName,
          paymentInfo = paymentInfo,
          euroBonus = euroBonus,
          showBusinessModel = showBusinessModel,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = ProfileUiState(),
      )

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
)

@Immutable
internal data class PaymentInfo(
  val monetaryMonthlyNet: MonetaryAmountFragment,
  @StringRes val priceCaptionResId: Int?,
)
