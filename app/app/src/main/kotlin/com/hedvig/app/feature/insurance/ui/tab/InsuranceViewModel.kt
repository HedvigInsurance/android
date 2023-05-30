package com.hedvig.app.feature.insurance.ui.tab

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.usecase.GetCrossSellsUseCase
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.hanalytics.HAnalytics
import giraffe.InsuranceQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import slimber.log.e
import kotlin.time.Duration.Companion.seconds

class InsuranceViewModel(
  private val getContractsUseCase: GetContractsUseCase,
  private val getCrossSellsUseCase: GetCrossSellsUseCase,
  private val crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
  private val hAnalytics: HAnalytics,
) : ViewModel() {

  private val retryChannel = RetryChannel()
  private val storeUrl = MutableStateFlow<Uri?>(null)
  private val loading = MutableStateFlow(false)
  private val dataOrNullFlow: Flow<List<InsuranceModel>?> = retryChannel.transformLatest {
    loading.update { true }
    either {
      val contracts = getContractsUseCase.invoke().bind()
      val crossSells = getCrossSellsUseCase.invoke().bind()
      createInsuranceItems(contracts, crossSells)
    }.fold(
      ifLeft = {
        e { "Insurance items failed to load: ${it.message}" }
        emit(null)
      },
      ifRight = { insuranceModels ->
        emit(insuranceModels)
      },
    )
    loading.update { false }
  }
  val uiState: StateFlow<InsuranceUiState> =
    combine(dataOrNullFlow, storeUrl, loading) { insuranceModels, uri, loading ->
      InsuranceUiState(
        insuranceModels = insuranceModels,
        storeUrl = uri,
        hasError = insuranceModels == null && loading == false,
        loading = loading,
      )
    }.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      InsuranceUiState(loading = true),
    )

  fun load() {
    retryChannel.retry()
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
    storeUrl.update { Uri.parse(data.storeUrl) }
  }

  fun crossSellActionOpened() {
    storeUrl.update { null }
  }
}

data class InsuranceUiState(
  val insuranceModels: List<InsuranceModel>? = null,
  val storeUrl: Uri? = null,
  val hasError: Boolean = false,
  val loading: Boolean = false,
)
