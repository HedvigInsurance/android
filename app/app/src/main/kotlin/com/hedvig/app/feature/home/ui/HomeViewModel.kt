package com.hedvig.app.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.merge
import arrow.core.raise.either
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.app.feature.home.data.GetHomeUseCase
import com.hedvig.app.feature.home.model.HomeItemsBuilder
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.hanalytics.HAnalytics
import giraffe.HomeQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
  private val getHomeUseCase: GetHomeUseCase,
  private val getTravelCertificateUseCase: GetTravelCertificateSpecificationsUseCase,
  private val homeItemsBuilder: HomeItemsBuilder,
  private val hAnalytics: HAnalytics,
) : ViewModel() {

  init {
    viewModelScope.launch {
      createViewState(forceReload = false)
    }
  }

  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  val uiState: StateFlow<HomeUiState> = _uiState

  fun reload() {
    viewModelScope.launch {
      createViewState(forceReload = true)
    }
  }

  fun onClaimDetailCardClicked(claimId: String) {
    val claim = getClaimById(claimId) ?: return

    hAnalytics.claimCardClick(claimId, claim.status.rawValue)
  }

  fun onClaimDetailCardShown(claimId: String) {
    val claim = getClaimById(claimId) ?: return

    hAnalytics.claimCardVisible(claimId, claim.status.rawValue)
  }

  private fun getClaimById(claimId: String): HomeQuery.Claim? =
    (_uiState.value as? HomeUiState.Success)
      ?.homeData
      ?.claimStatusCards
      ?.firstOrNull { it.id == claimId }
      ?.claim

  fun onPaymentCardShown() {
    hAnalytics.homePaymentCardVisible()
  }

  fun onPaymentCardClicked() {
    hAnalytics.homePaymentCardClick()
  }

  private suspend fun createViewState(forceReload: Boolean) {
    if (_uiState.value.isLoading) return
    _uiState.update {
      if (it is HomeUiState.Success) {
        it.copy(isReloading = true)
      } else {
        HomeUiState.Loading
      }
    }
    val newUiState = either {
      val homeData = getHomeUseCase.invoke(forceReload).bind()
      val travelCertificateData = getTravelCertificateUseCase.invoke().getOrNull()

      HomeUiState.Success(
        homeData = homeData,
        homeItems = homeItemsBuilder.buildItems(
          homeData = homeData,
          travelCertificateData = travelCertificateData,
        ),
      )
    }
      .mapLeft { HomeUiState.Error(it.message) }
      .merge()
    _uiState.update { newUiState }
  }
}

sealed interface HomeUiState {
  val isLoading: Boolean
    get() = this is Loading || (this is Success && isReloading)

  data class Success(
    val homeData: HomeQuery.Data,
    val homeItems: List<HomeModel>,
    val isReloading: Boolean = false,
  ) : HomeUiState

  data class Error(val message: String?) : HomeUiState
  object Loading : HomeUiState
}
