package com.hedvig.app.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.app.feature.home.data.GetHomeUseCase
import com.hedvig.app.feature.home.model.HomeItemsBuilder
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.hanalytics.HAnalytics
import giraffe.HomeQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class HomeViewModel(
  private val hAnalytics: HAnalytics,
) : ViewModel() {
  sealed class ViewState {
    data class Success(
      val homeData: HomeQuery.Data,
      val homeItems: List<HomeModel>,
    ) : ViewState()

    data class Error(val message: String?) : ViewState()
    object Loading : ViewState()
  }

  protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
  val viewState: StateFlow<ViewState> = _viewState

  abstract fun load()
  abstract fun reload()

  fun onClaimDetailCardClicked(claimId: String) {
    val claim = getClaimById(claimId) ?: return

    hAnalytics.claimCardClick(claimId, claim.status.rawValue)
  }

  fun onClaimDetailCardShown(claimId: String) {
    val claim = getClaimById(claimId) ?: return

    hAnalytics.claimCardVisible(claimId, claim.status.rawValue)
  }

  private fun getClaimById(claimId: String): HomeQuery.Claim? =
    (_viewState.value as? ViewState.Success)
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
}

class HomeViewModelImpl(
  private val getHomeUseCase: GetHomeUseCase,
  private val getTravelCertificateUseCase: GetTravelCertificateUseCase,
  private val homeItemsBuilder: HomeItemsBuilder,
  private val featureManager: FeatureManager,
  hAnalytics: HAnalytics,
) : HomeViewModel(hAnalytics) {
  init {
    load()
  }

  override fun load() {
    viewModelScope.launch {
      createViewState(forceReload = false)
    }
  }

  override fun reload() {
    viewModelScope.launch {
      createViewState(forceReload = true)
    }
  }

  private suspend fun createViewState(forceReload: Boolean) {
    _viewState.value = ViewState.Loading
    either {
      val homeData = getHomeUseCase.invoke(forceReload).bind()
      val travelCertificateData = if (featureManager.isFeatureEnabled(Feature.TRAVEL_CERTIFICATE)) {
        getTravelCertificateUseCase.invoke().bind()
      } else {
        null
      }

      ViewState.Success(
        homeData = homeData,
        homeItems = homeItemsBuilder.buildItems(
          homeData = homeData,
          travelCertificateData = travelCertificateData,
        ),
      )
    }
      .mapLeft { ViewState.Error(it.message) }
      .fold(
        ifLeft = { _viewState.value = it },
        ifRight = { _viewState.value = it },
      )
  }
}
