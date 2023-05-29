package com.hedvig.app.feature.insurance.ui.tab

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsUseCase
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.hanalytics.HAnalytics
import giraffe.InsuranceQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsuranceViewModel(
  private val getContractsUseCase: GetContractsUseCase,
  private val getCrossSellsUseCase: GetCrossSellsUseCase,
  private val crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
  private val hAnalytics: HAnalytics,
) : ViewModel() {

  private val _uiState = MutableStateFlow(InsuranceUiState(loading = true))
  val uiState = _uiState.asStateFlow()

  fun load() {
    viewModelScope.launch {
      _uiState.update { it.copy(loading = true) }
      either {
        val contracts = getContractsUseCase.invoke().bind()
        val crossSells = getCrossSellsUseCase.invoke().bind()
        createInsuranceItems(contracts, crossSells)
      }.fold(
        ifLeft = {
          _uiState.update { it.copy(hasError = true, loading = false) }
        },
        ifRight = { insuranceModels ->
          _uiState.update { it.copy(insuranceModels = insuranceModels, hasError = false, loading = false) }
        },
      )
    }
  }

  private suspend fun createInsuranceItems(
    result: InsuranceQuery.Data,
    crossSells: List<CrossSellData>,
  ): List<InsuranceModel> {
    val showNotificationBadge = crossSellCardNotificationBadgeService.showNotification().first()

    return buildInsuranceModelItems(
      insurances = result,
      crossSells = crossSells,
      showCrossSellNotificationBadge = showNotificationBadge,
    )
  }

  fun markCardCrossSellsAsSeen() {
    viewModelScope.launch {
      crossSellCardNotificationBadgeService.markAsSeen()
    }
  }

  fun onClickCrossSellCard(data: CrossSellData) {
    hAnalytics.cardClickCrossSellDetail(id = data.id)
  }

  fun onClickCrossSellAction(data: CrossSellData) {
    viewModelScope.launch {
      _uiState.update { it.copy(storeUrl = Uri.parse(data.storeUrl)) }
    }
  }

  fun crossSellActionOpened() {
    _uiState.update {
      it.copy(storeUrl = null)
    }
  }
}

data class InsuranceUiState(
  val insuranceModels: List<InsuranceModel>? = null,
  val storeUrl: Uri? = null,
  val hasError: Boolean = false,
  val loading: Boolean = false,
)
