package com.hedvig.app.feature.marketing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.MarketingBackground
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.LoginMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MarketingViewModel(
  market: Market?,
  private val hAnalytics: HAnalytics,
  private val getMarketingBackgroundUseCase: GetMarketingBackgroundUseCase,
  private val updateApplicationLanguageUseCase: UpdateApplicationLanguageUseCase,
  private val getInitialMarketPickerValuesUseCase: GetInitialMarketPickerValuesUseCase,
  private val featureManager: FeatureManager,
) : ViewModel() {

  private val _state = MutableStateFlow(MarketingViewState(selectedMarket = market))
  val state = _state.asStateFlow()

  private val _marketingBackground = MutableStateFlow<MarketingBackground?>(null)
  val marketingBackground = _marketingBackground.asStateFlow()

  init {
    viewModelScope.launch {
      val initialValues = getInitialMarketPickerValuesUseCase.invoke()
      _state.update {
        it.copy(
          loginMethod = featureManager.getLoginMethod(),
          availableMarkets = getAvailableMarkets(),
          market = initialValues.first,
          language = initialValues.second,
          isLoading = false,
        )
      }
    }
    viewModelScope.launch {
      getMarketingBackgroundUseCase.invoke().onRight { marketingBackground ->
        _marketingBackground.value = marketingBackground
      }
    }
  }

  private suspend fun getAvailableMarkets() = buildList {
    add(Market.SE)
    add(Market.NO)
    add(Market.DK)
    if (featureManager.isFeatureEnabled(Feature.FRANCE_MARKET)) {
      add(Market.FR)
    }
  }

  fun setMarket(market: Market) {
    viewModelScope.launch {
      updateApplicationLanguageUseCase.invoke(market, market.defaultLanguage())
    }

    _state.update {
      it.copy(
        market = market,
        language = market.defaultLanguage(),
      )
    }
  }

  fun setLanguage(language: Language) {
    state.value.market?.let {
      viewModelScope.launch {
        updateApplicationLanguageUseCase.invoke(it, language)
      }
    }

    _state.update { it.copy(language = language) }
  }

  fun submitMarketAndLanguage() {
    viewModelScope.launch {
      val market = _state.value.market ?: error("Market null")
      val language = _state.value.language ?: error("Language null")

      _state.update { it.copy(isLoading = true) }
      updateApplicationLanguageUseCase.invoke(market, language)
      featureManager.invalidateExperiments()
      _state.update {
        it.copy(
          selectedMarket = market,
          loginMethod = featureManager.getLoginMethod(),
          isLoading = false,
        )
      }
    }
  }

  fun onFlagClick() {
    _state.update { it.copy(selectedMarket = null) }
  }

  fun onClickSignUp() {
    hAnalytics.buttonClickMarketingOnboard()
  }

  fun onClickLogIn() {
    hAnalytics.buttonClickMarketingLogin()
  }
}

data class MarketingViewState(
  val market: Market? = null,
  val language: Language? = null,
  val availableMarkets: List<Market> = emptyList(),
  val selectedMarket: Market? = null,
  val loginMethod: LoginMethod? = null,
  val isLoading: Boolean = true,
) {
  fun canSetMarketAndLanguage() = market != null && language != null
}
