package com.hedvig.android.feature.login.marketing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.launch

internal class MarketingPresenter(
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) : MoleculePresenter<MarketingEvent, MarketingUiState> {
  @Composable
  override fun MoleculePresenterScope<MarketingEvent>.present(lastState: MarketingUiState): MarketingUiState {
    val market: Market = marketManager.market.collectAsState().value
    var language: Language? by remember { mutableStateOf((lastState as? MarketingUiState.Success)?.language) }

    var fetchLanguageCounter by remember { mutableIntStateOf(0) }
    LaunchedEffect(fetchLanguageCounter) {
      // There are no observable APIs to listen in on the language changes, so we need to manually ask again ourselves.
      language = languageService.getLanguage()
    }

    CollectEvents { event ->
      when (event) {
        is MarketingEvent.SelectLanguage -> {
          languageService.setLanguage(event.language)
          fetchLanguageCounter++
        }
        is MarketingEvent.SelectMarket -> {
          launch { marketManager.setMarket(event.market) }
        }
      }
    }

    val languageValue = language
    if (languageValue == null) {
      return MarketingUiState.Loading
    }
    return MarketingUiState.Success(market, languageValue)
  }
}

internal sealed interface MarketingUiState {
  data object Loading : MarketingUiState
  data class Success(
    val market: Market,
    val language: Language,
  ) : MarketingUiState
}

internal sealed interface MarketingEvent {
  data class SelectMarket(val market: Market) : MarketingEvent
  data class SelectLanguage(val language: Language) : MarketingEvent
}
