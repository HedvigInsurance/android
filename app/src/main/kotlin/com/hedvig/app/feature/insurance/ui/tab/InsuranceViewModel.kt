package com.hedvig.app.feature.insurance.ui.tab

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.continuations.either
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

  data class ViewState(
    val items: List<InsuranceModel>? = null,
    val storeUrl: Uri? = null,
    val hasError: Boolean = false,
    val loading: Boolean = false,
  )

  private val _viewState = MutableStateFlow(ViewState(loading = true))
  val viewState = _viewState.asStateFlow()

  fun load() {
    viewModelScope.launch {
      _viewState.value = ViewState(loading = true)
      _viewState.value = either {
        val contracts = getContractsUseCase.invoke().bind()
        val crossSells = getCrossSellsUseCase.invoke().bind()
        createInsuranceItems(contracts, crossSells)
      }.fold(
        ifLeft = { ViewState(hasError = true, loading = false) },
        ifRight = { ViewState(items = it, loading = false) },
      )
    }
  }

  private suspend fun createInsuranceItems(
    result: InsuranceQuery.Data,
    crossSells: List<CrossSellData>,
  ): List<InsuranceModel> {
    val showNotificationBadge = crossSellCardNotificationBadgeService.showNotification().first()

    return items(
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
      _viewState.value = ViewState(storeUrl = Uri.parse(data.storeUrl))
    }
  }

  fun crossSellActionOpened() {
    _viewState.update {
      it.copy(storeUrl = null)
    }
  }
}
