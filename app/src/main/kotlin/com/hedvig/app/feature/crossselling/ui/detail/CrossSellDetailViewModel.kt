package com.hedvig.app.feature.crossselling.ui.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CrossSellDetailViewModel(
  private val storeUrl: String,
  hAnalytics: HAnalytics,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ViewState())
  val viewState = _viewState.asStateFlow()

  init {
    hAnalytics.screenView(AppScreen.CROSS_SELL_DETAIL)
  }

  data class ViewState(
    val storeUrl: Uri? = null,
    val errorMessage: String? = null,
    val loading: Boolean = false,
  )

  fun onCtaClick() {
    viewModelScope.launch {
      _viewState.value = ViewState(storeUrl = Uri.parse(storeUrl))
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
