package com.hedvig.app.feature.marketpicker

import com.hedvig.app.feature.settings.Language

sealed class MarketAndLanguageModel {
    data class MarketModel(
        val market: Market,
        val selected: Boolean = false
    ) : MarketAndLanguageModel()

    data class LanguageModel(
        val language: Language,
        var selected: Boolean = false,
        var available: Boolean = false
    ) : MarketAndLanguageModel()
}
