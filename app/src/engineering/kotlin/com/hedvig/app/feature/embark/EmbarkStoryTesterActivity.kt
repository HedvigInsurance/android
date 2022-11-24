package com.hedvig.app.feature.embark

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.ExchangeTokenMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.auth.AuthenticationTokenService
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class EmbarkStoryTesterActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.compatSetDecorFitsSystemWindows(false)
    loadKoinModules(embarkStoryTesterModule)

    val viewModel: EmbarkStoryTesterViewModel = getViewModel()
    setContent {
      val viewState by viewModel.viewState.collectAsState()

      LaunchedEffect(viewState.selectedStoryName) {
        val selectedStoryName = viewState.selectedStoryName ?: return@LaunchedEffect
        viewModel.onStorySelected(null)
        startActivity(
          EmbarkActivity.newInstance(
            this@EmbarkStoryTesterActivity,
            selectedStoryName,
            selectedStoryName,
          ),
        )
      }

      HedvigTheme {
        Column(
          modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        ) {
          TopAppBar(
            title = { Text(text = "Embark tester") },
            navigationIcon = {
              IconButton(onClick = ::finish) {
                Icon(
                  imageVector = Icons.Filled.ArrowBack,
                  contentDescription = null,
                )
              }
            },
            backgroundColor = MaterialTheme.colors.background,
            elevation = 0.dp,
          )
          Column(
            Modifier.verticalScroll(rememberScrollState()),
          ) {
            Text(text = "Optional auth token or payments url")
            TextField(
              value = viewState.authTokenInput ?: "",
              onValueChange = {
                viewModel.onAuthToken(it)
              },
            )
            Button(
              onClick = {
                viewState.authTokenInput?.let {
                  viewModel.setAuthToken(it)
                }
              },
            ) {
              Text("Set token")
            }
            Button(
              onClick = {
                viewState.authTokenInput?.let {
                  viewModel.generateAuthTokenFromPaymentsLink(it)
                }
              },
            ) {
              Text("Generate and set token from payments url")
            }
            Text(text = "Custom Story")
            TextField(
              value = viewState.storyNameInput ?: "",
              onValueChange = {
                viewModel.onStoryName(it)
              },
            )
            Button(
              onClick = {
                viewState.storyNameInput?.let {
                  viewModel.onStorySelected(it)
                }
              },
            ) {
              Text("Start story")
            }
            Text(text = "Markets")
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            ) {
              items(viewState.availableMarkets) { market ->
                MarketItem(market) {
                  viewModel.onMarketClick(market)
                }
              }
            }
          }
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    unloadKoinModules(embarkStoryTesterModule)
  }
}

@Composable
private fun MarketItem(market: Market, onClick: () -> Unit) {
  Surface(
    shape = MaterialTheme.shapes.medium,
    modifier = Modifier.clickable { onClick() },
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .padding(12.dp),
    ) {
      Column {
        Text(
          text = market.name,
          style = MaterialTheme.typography.body1,
          modifier = Modifier
            .padding(bottom = 8.dp),
        )
      }
    }
  }
}

val embarkStoryTesterModule = module {
  viewModel { EmbarkStoryTesterViewModel(get(), get(), get(), get(), get()) }
}

class EmbarkStoryTesterViewModel(
  private val marketManager: MarketManager,
  private val authenticationTokenService: AuthenticationTokenService,
  private val apolloClient: ApolloClient,
  private val createQuoteCartUseCase: CreateQuoteCartUseCase,
  private val languageService: LanguageService,
) : ViewModel() {

  data class ViewState(
    val selectedStoryName: String? = null,
    val storyNameInput: String? = null,
    val authTokenInput: String? = null,
    val availableMarkets: List<Market> = emptyList(),
    val authorization: String? = null,
    val errorMessage: String? = null,
  )

  private val _viewState = MutableStateFlow(ViewState())
  val viewState: StateFlow<ViewState> = _viewState

  private var quoteCartId: QuoteCartId? = null

  init {
    viewModelScope.launch {
      _viewState.value = viewState.value.copy(availableMarkets = marketManager.enabledMarkets)
      createQuoteCartUseCase.invoke().tap { quoteCartId = it }
    }
  }

  fun onStorySelected(storyName: String?) {
    if (storyName == null) {
      return
    }

    _viewState.update {
      it.copy(selectedStoryName = appendQuoteCartId(storyName, quoteCartId!!.id))
    }
  }

  fun onMarketClick(market: Market) {
    marketManager.market = market
    val language = Language.getAvailableLanguages(market).first()
    languageService.setLanguage(language)
  }

  fun onStoryName(storyName: String) {
    _viewState.update {
      it.copy(storyNameInput = storyName)
    }
  }

  fun onAuthToken(authToken: String) {
    _viewState.update {
      it.copy(authTokenInput = authToken)
    }
  }

  fun setAuthToken(authToken: String) {
    authenticationTokenService.authenticationToken = authToken
  }

  fun generateAuthTokenFromPaymentsLink(paymentsUrl: String) {
    val exchangeToken = paymentsUrl.split("=")[1]

    viewModelScope.launch {
      if (authenticationTokenService.authenticationToken == null) {
        authenticationTokenService.authenticationToken = "123"
      }
      when (
        val result = apolloClient
          .mutation(ExchangeTokenMutation(exchangeToken))
          .safeExecute()
          .toEither()
      ) {
        is Either.Left -> _viewState.update {
          it.copy(errorMessage = result.value.message)
        }
        is Either.Right -> {
          val newToken = result.value.exchangeToken.asExchangeTokenSuccessResponse?.token
          if (newToken == null) {
            _viewState.update {
              it.copy(errorMessage = "Did not receive token")
            }
          }
          authenticationTokenService.authenticationToken = newToken
        }
      }
    }
  }
}
