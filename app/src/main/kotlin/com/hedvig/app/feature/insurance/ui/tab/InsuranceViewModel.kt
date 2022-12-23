package com.hedvig.app.feature.insurance.ui.tab

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.apollo.graphql.InsuranceQuery
import com.hedvig.android.notification.badge.data.crosssell.card.CrossSellCardNotificationBadgeService
import com.hedvig.app.feature.crossselling.model.NavigateChat
import com.hedvig.app.feature.crossselling.model.NavigateEmbark
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsuranceViewModel(
  private val getContractsUseCase: GetContractsUseCase,
  private val crossSellCardNotificationBadgeService: CrossSellCardNotificationBadgeService,
  private val createQuoteCartUseCase: CreateQuoteCartUseCase,
  private val hAnalytics: HAnalytics,
) : ViewModel() {

  data class ViewState(
    val items: List<InsuranceModel>? = null,
    val navigateEmbark: NavigateEmbark? = null,
    val navigateChat: NavigateChat? = null,
    val navigateWeb: Uri? = null,
    val hasError: Boolean = false,
    val loading: Boolean = false,
  )

  private val _viewState = MutableStateFlow(ViewState(loading = true))
  val viewState = _viewState.asStateFlow()

  fun load() {
    viewModelScope.launch {
      _viewState.value = ViewState(loading = true)
      when (val result = getContractsUseCase.invoke()) {
        is Either.Left -> _viewState.value = ViewState(hasError = true, items = null)
        is Either.Right -> _viewState.value = ViewState(items = createInsuranceItems(result))
      }
    }
  }

  private suspend fun createInsuranceItems(result: Either.Right<InsuranceQuery.Data>): List<InsuranceModel> {
    val showNotificationBadge = crossSellCardNotificationBadgeService.showNotification().first()

    return items(
      data = result.value,
      showCrossSellNotificationBadge = showNotificationBadge,
    )
  }

  fun markCardCrossSellsAsSeen() {
    viewModelScope.launch {
      crossSellCardNotificationBadgeService.markAsSeen()
    }
  }

  fun onClickCrossSellCard(data: CrossSellData) {
    hAnalytics.cardClickCrossSellDetail(id = data.typeOfContract)
  }

  fun onClickCrossSellAction(data: CrossSellData) {
    viewModelScope.launch {
      when (val action = data.action) {
        CrossSellData.Action.Chat -> _viewState.update {
          it.copy(navigateChat = NavigateChat)
        }
        is CrossSellData.Action.Embark -> {
          hAnalytics.cardClickCrossSellDetail(
            id = data.typeOfContract,
          )
          _viewState.value = action.toViewState()
        }
        is CrossSellData.Action.Web -> {
          hAnalytics.cardClickCrossSellDetail(
            id = data.typeOfContract,
          )
          _viewState.value = ViewState(navigateWeb = Uri.parse(action.url))
        }
      }
    }
  }

  private suspend fun CrossSellData.Action.Embark.toViewState(): ViewState {
    return when (val result = createQuoteCartUseCase.invoke()) {
      is Either.Left -> _viewState.value.copy(hasError = true)
      is Either.Right -> {
        val embarkStoryId = appendQuoteCartId(embarkStoryId, result.value.id)
        val navigateEmbark = NavigateEmbark(embarkStoryId, title)
        _viewState.value.copy(navigateEmbark = navigateEmbark)
      }
    }
  }

  fun crossSellActionOpened() {
    _viewState.update {
      it.copy(navigateChat = null, navigateEmbark = null, navigateWeb = null)
    }
  }
}
