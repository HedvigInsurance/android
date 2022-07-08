package com.hedvig.app.feature.marketing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.MarketingBackground
import com.hedvig.app.feature.marketing.data.SubmitMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.LoginMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MarketingViewModel(
  market: Market?,
  private val hAnalytics: HAnalytics,
  private val submitMarketAndLanguagePreferencesUseCase: SubmitMarketAndLanguagePreferencesUseCase,
  private val getMarketingBackgroundUseCase: GetMarketingBackgroundUseCase,
  private val updateApplicationLanguageUseCase: UpdateApplicationLanguageUseCase,
  private val getInitialMarketPickerValuesUseCase: GetInitialMarketPickerValuesUseCase,
  private val featureManager: FeatureManager,
) : ViewModel() {

  private val _state = MutableStateFlow(ViewState(selectedMarket = market))
  val state = _state.asStateFlow()

  private val _background = MutableStateFlow(Background(data = null))
  val background = _background.asStateFlow()

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

      getMarketingBackgroundUseCase.invoke().tap { bg ->
        _background.value = Background(data = bg)
      }
    }
  }

  private suspend fun getAvailableMarkets() = listOfNotNull(
    Market.SE,
    Market.NO,
    Market.DK,
    if (featureManager.isFeatureEnabled(Feature.FRANCE_MARKET)) {
      Market.FR
    } else {
      null
    },
  )

  fun setMarket(market: Market) {
    updateApplicationLanguageUseCase.invoke(market, market.defaultLanguage())

    _state.update {
      it.copy(
        market = market,
        language = market.defaultLanguage(),
      )
    }
  }

  fun setLanguage(language: Language) {
    state.value.market?.let {
      updateApplicationLanguageUseCase.invoke(it, language)
    }

    _state.update { it.copy(language = language) }
  }

  fun submitMarketAndLanguage() {
    viewModelScope.launch {
      val market = _state.value.market ?: throw IllegalArgumentException("Market null")
      val language = _state.value.language ?: throw IllegalArgumentException("Language null")

      when (submitMarketAndLanguagePreferencesUseCase.invoke(language, market)) {
        is Either.Left -> {
          _state.update { it.copy(isLoading = false) }
        }
        is Either.Right -> {
          updateApplicationLanguageUseCase.invoke(market, language)
          featureManager.invalidateExperiments()
          _state.update {
            it.copy(
              selectedMarket = market,
              isLoading = false,
            )
          }
        }
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

data class ViewState(
  val market: Market? = null,
  val language: Language? = null,
  val availableMarkets: List<Market> = emptyList(),
  val selectedMarket: Market? = null,
  val loginMethod: LoginMethod? = null,
  val isLoading: Boolean = true,
) {
  fun canSetMarketAndLanguage() = market != null && language != null
}

data class Background(
  val data: MarketingBackground?,
)
