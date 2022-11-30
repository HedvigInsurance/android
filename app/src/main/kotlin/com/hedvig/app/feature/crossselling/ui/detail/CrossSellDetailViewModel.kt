package com.hedvig.app.feature.crossselling.ui.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.crossselling.model.NavigateChat
import com.hedvig.app.feature.crossselling.model.NavigateEmbark
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import java.net.URI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CrossSellDetailViewModel(
  private val crossSellAction: CrossSellData.Action,
  hAnalytics: HAnalytics,
  private val createQuoteCartUseCase: CreateQuoteCartUseCase,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ViewState())
  val viewState = _viewState.asStateFlow()

  init {
    hAnalytics.screenView(AppScreen.CROSS_SELL_DETAIL)
  }

  data class ViewState(
    val navigateEmbark: NavigateEmbark? = null,
    val navigateChat: NavigateChat? = null,
    val navigateWeb: Uri? = null,
    val errorMessage: String? = null,
    val loading: Boolean = false,
  )

  fun onCtaClick() {
    viewModelScope.launch {
      when (val action = crossSellAction) {
        CrossSellData.Action.Chat -> _viewState.value = ViewState(navigateChat = NavigateChat)
        is CrossSellData.Action.Embark -> _viewState.value = action.toViewState()
        is CrossSellData.Action.Web -> _viewState.value = ViewState(navigateWeb = Uri.parse(action.url))
      }
    }
  }

  private suspend fun CrossSellData.Action.Embark.toViewState(): ViewState {
    return when (val result = createQuoteCartUseCase.invoke()) {
      is Either.Left -> ViewState(errorMessage = result.value.message)
      is Either.Right -> {
        val embarkStoryId = appendQuoteCartId(embarkStoryId, result.value.id)
        val navigateEmbark = NavigateEmbark(embarkStoryId, title)
        ViewState(navigateEmbark = navigateEmbark)
      }
    }
  }

  fun actionOpened() {
    _viewState.value = ViewState()
  }

  fun dismissError() {
    _viewState.update {
      it.copy(errorMessage = null)
    }
  }
}
