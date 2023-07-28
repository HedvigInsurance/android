package com.hedvig.android.feature.profile.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ProfileViewModel(
  private val getEuroBonusStatusUseCase: GetEurobonusStatusUseCase,
  private val featureManager: FeatureManager,
  private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

  private val _data = MutableStateFlow(ProfileUiState())
  val data: StateFlow<ProfileUiState> = _data

  init {
    loadProfileData()
  }

  private fun loadProfileData() {
    viewModelScope.launch {
      val showPaymentScreen = featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN)
      _data.update {
        it.copy(showPaymentScreen = showPaymentScreen)
      }

      getEuroBonusStatusUseCase.invoke().fold(
        ifLeft = { _data.update { it.copy(errorMessage = it.errorMessage) } },
        ifRight = { euroBonus -> _data.update { it.copy(euroBonus = euroBonus) } },
      )
    }
  }

  fun reload() {
    loadProfileData()
  }

  fun onLogout() {
    logoutUseCase.invoke()
  }
}

internal data class ProfileUiState(
  val euroBonus: EuroBonus? = null,
  val showPaymentScreen: Boolean = false,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
)

