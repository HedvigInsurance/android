package com.hedvig.android.feature.home.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.merge
import arrow.core.raise.either
import arrow.fx.coroutines.parZip
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.home.data.GetHomeUseCase
import com.hedvig.android.feature.home.home.HomeItemsBuilder
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.hanalytics.HAnalytics
import giraffe.HomeQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HomeViewModel(
  private val getHomeUseCase: GetHomeUseCase,
  private val getTravelCertificateUseCase: GetTravelCertificateSpecificationsUseCase,
  private val homeItemsBuilder: HomeItemsBuilder,
  private val hAnalytics: HAnalytics,
) : ViewModel() {

  private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
  val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      createViewState(forceReload = false)
    }
  }

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

  private fun getClaimById(claimId: String): HomeQuery.Claim? {
    return (_uiState.value as? HomeUiState.Success)
      ?.claimStatusCards
      ?.firstOrNull { it.id == claimId }
      ?.claim
  }

  fun onPaymentCardShown() {
    hAnalytics.homePaymentCardVisible()
  }

  fun onPaymentCardClicked() {
    hAnalytics.homePaymentCardClick()
  }

  private suspend fun createViewState(forceReload: Boolean) {
    if (forceReload == true && _uiState.value.isLoading) return
    _uiState.update {
      if (it is HomeUiState.Success) {
        it.copy(isReloading = true)
      } else {
        HomeUiState.Loading
      }
    }
    val newUiState = either {
      parZip(
        { getHomeUseCase.invoke(forceReload).bind() },
        { getTravelCertificateUseCase.invoke().getOrNull() },
      ) { homeData, travelCertificateData ->

        HomeUiState.Success(
          claimStatusCards = homeData.claimStatusCards,
          homeItems = homeItemsBuilder.buildItems(
            homeData = homeData,
            showTravelCertificate = travelCertificateData != null,
          ),
        )
      }
    }
      .mapLeft { HomeUiState.Error(it.message) }
      .merge()
    _uiState.update { newUiState }
  }
}

internal sealed interface HomeUiState {
  val isLoading: Boolean
    get() = this is Loading || (this is Success && isReloading)

  data class Success(
    val claimStatusCards: List<HomeQuery.ClaimStatusCard>,
    val homeItems: List<HomeModel>,
    val isReloading: Boolean = false,
  ) : HomeUiState

  data class Error(val message: String?) : HomeUiState
  data object Loading : HomeUiState
}
