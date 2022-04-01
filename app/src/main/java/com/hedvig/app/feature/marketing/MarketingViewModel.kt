package com.hedvig.app.feature.marketing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.owldroid.type.UserInterfaceStyle
import com.hedvig.app.feature.marketing.data.GetInitialMarketPickerValuesUseCase
import com.hedvig.app.feature.marketing.data.GetMarketingBackgroundUseCase
import com.hedvig.app.feature.marketing.data.SubmitMarketAndLanguagePreferencesUseCase
import com.hedvig.app.feature.marketing.data.UpdateApplicationLanguageUseCase
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.safeLet
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.LoginMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class MarketingViewModel(
    private val marketManager: MarketManager,
    private val hAnalytics: HAnalytics,
    private val submitMarketAndLanguagePreferencesUseCase: SubmitMarketAndLanguagePreferencesUseCase,
    private val getMarketingBackgroundUseCase: GetMarketingBackgroundUseCase,
    private val updateApplicationLanguageUseCase: UpdateApplicationLanguageUseCase,
    private val getInitialMarketPickerValuesUseCase: GetInitialMarketPickerValuesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(
        if (marketManager.hasSelectedMarket) {
            MarketPicked.Loading
        } else {
            PICK_MARKET_INITIAL
        }
    )
    val state = _state.asStateFlow()

    private val _background = MutableStateFlow<Background>(Background.Loading)
    val background = _background.asStateFlow()

    init {
        viewModelScope.launch {
            if (marketManager.hasSelectedMarket) {
                loadMarketPicked()
            } else {
                loadMarketPicker()
            }

            getMarketingBackgroundUseCase.invoke().tap {
                safeLet(it?.blurhash, it?.image?.url, it?.userInterfaceStyle) { blurHash, url, userInterfaceStyle ->
                    _background.value = Background.Loaded(
                        url = url,
                        blurHash = blurHash,
                        theme = when (userInterfaceStyle) {
                            UserInterfaceStyle.LIGHT -> Background.Theme.LIGHT
                            UserInterfaceStyle.DARK -> Background.Theme.DARK
                            else -> Background.Theme.DARK
                        }
                    )
                }
            }
        }
    }

    private suspend fun loadMarketPicked() {
        val market = marketManager.market ?: throw IllegalStateException("Market cannot be null when loaded")
        _state.value = MarketPicked.Loaded(
            selectedMarket = market,
            loginMethod = hAnalytics.loginMethod(),
        )
    }

    private suspend fun loadMarketPicker() {
        val availableMarkets = listOfNotNull(
            Market.SE,
            Market.NO,
            Market.DK,
            if (hAnalytics.frenchMarket()) {
                Market.FR
            } else {
                null
            }
        )

        when (val initialValues = getInitialMarketPickerValuesUseCase.invoke()) {
            is Either.Left -> updateMarketPickerState {
                it.copy(isLoading = false, availableMarkets = availableMarkets)
            }
            is Either.Right -> updateMarketPickerState {
                it.copy(
                    isLoading = false,
                    market = initialValues.value.first,
                    language = initialValues.value.second,
                    availableMarkets = availableMarkets,
                )
            }
        }
    }

    fun setMarket(market: Market) {
        val newState = updateMarketPickerState { it.copy(market = market) }
        safeLet((newState as? PickMarket)?.market, (newState as? PickMarket)?.language) { m, l ->
            updateApplicationLanguageUseCase.invoke(m, l)
        }
    }

    fun setLanguage(language: Language) {
        val newState = updateMarketPickerState { it.copy(language = language) }
        safeLet((newState as? PickMarket)?.market, (newState as? PickMarket)?.language) { m, l ->
            updateApplicationLanguageUseCase.invoke(m, l)
        }
    }

    private fun updateMarketPickerState(transform: (oldState: PickMarket) -> PickMarket): ViewState {
        val oldState = state.value as? PickMarket
            ?: throw IllegalStateException("Cannot update market picker state when not on market picker")
        val newState = transform(oldState)
        val newStateWithLanguage = newState.copy(
            language = languageFromState(newState)
        )
        return _state.updateAndGet {
            newStateWithLanguage.copy(
                isValid = isValid(newStateWithLanguage),
            )
        }
    }

    private fun isValid(state: PickMarket): Boolean {
        return (state.market != null && state.language != null)
    }

    private fun languageFromState(state: PickMarket): Language? {
        val market = state.market ?: return state.language
        val language = state.language ?: return defaultLanguage(market)

        if (!isCompatibleLanguage(language, market)) {
            return defaultLanguage(market)
        }

        return language
    }

    private fun isCompatibleLanguage(language: Language, market: Market) = when (market) {
        Market.SE -> language == Language.EN_SE || language == Language.SV_SE
        Market.NO -> language == Language.EN_NO || language == Language.NB_NO
        Market.DK -> language == Language.EN_DK || language == Language.DA_DK
        Market.FR -> language == Language.EN_FR || language == Language.FR_FR
    }

    private fun defaultLanguage(market: Market) = when (market) {
        Market.SE -> Language.EN_SE
        Market.NO -> Language.EN_NO
        Market.DK -> Language.EN_DK
        Market.FR -> Language.EN_FR
    }

    fun submitMarketAndLanguage() {
        val currentState = state.value as? PickMarket ?: throw IllegalStateException("Can't submit, not in PickMarket")
        if (!currentState.isValid) {
            throw IllegalStateException("Can't submit, PickMarket is not valid")
        }
        val market = currentState.market ?: throw IllegalStateException("Can't submit, market is null")
        val language = currentState.language ?: throw IllegalStateException("Can't submit, language is null")
        updateMarketPickerState { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = submitMarketAndLanguagePreferencesUseCase
                .invoke(language, market)
            if (result.isLeft()) {
                updateMarketPickerState { it.copy(isLoading = false) }
                return@launch
            }
            updateApplicationLanguageUseCase.invoke(market, language)
            hAnalytics.invalidateExperiments()
            _state.value = MarketPicked.Loading
            loadMarketPicked()
        }
    }

    fun goToMarketPicker() {
        _state.value = PICK_MARKET_INITIAL
        viewModelScope.launch { loadMarketPicker() }
    }

    fun onClickSignUp() {
        hAnalytics.buttonClickMarketingOnboard()
    }

    fun onClickLogIn() {
        hAnalytics.buttonClickMarketingLogin()
    }

    companion object {
        private val PICK_MARKET_INITIAL = PickMarket(
            isLoading = true,
            isValid = false,
            market = null,
            language = null,
            availableMarkets = emptyList(),
        )
    }
}

sealed interface ViewState

object Loading : ViewState

data class PickMarket(
    val isLoading: Boolean,
    val isValid: Boolean,
    val market: Market?,
    val language: Language?,
    val availableMarkets: List<Market>,
) : ViewState

sealed interface MarketPicked : ViewState {
    object Loading : MarketPicked

    data class Loaded(
        val selectedMarket: Market,
        val loginMethod: LoginMethod,
    ) : MarketPicked
}

sealed interface Background {
    object Loading : Background
    data class Loaded(
        val url: String,
        val blurHash: String,
        val theme: Theme,
    ) : Background

    enum class Theme {
        LIGHT,
        DARK;
    }
}
