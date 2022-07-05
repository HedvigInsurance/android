package com.hedvig.app.feature.embark

import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.google.accompanist.insets.systemBarsPadding
import com.hedvig.android.owldroid.graphql.ExchangeTokenMutation
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.onboarding.BundlesResult
import com.hedvig.app.feature.onboarding.GetBundlesUseCase
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.apollo.safeQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class EmbarkStoryTesterActivity : AppCompatActivity() {

  val model: EmbarkStoryTesterViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    loadKoinModules(embarkStoryTesterModule)

    setContent {
      val viewState by model.viewState.collectAsState()

      LaunchedEffect(viewState.selectedStoryName) {
        val selectedStoryName = viewState.selectedStoryName ?: return@LaunchedEffect
        model.onStorySelected(null)
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
          modifier = Modifier.fillMaxSize(),
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
            modifier = Modifier.systemBarsPadding(top = true),
          )
          Text(text = "Optional auth token or payments url")
          TextField(value = viewState.authTokenInput ?: "", onValueChange = {
            model.onAuthToken(it)
          },)
          Button(
            onClick = {
              viewState.authTokenInput?.let {
                model.setAuthToken(it)
              }
            },
          ) {
            Text("Set token")
          }
          Button(
            onClick = {
              viewState.authTokenInput?.let {
                model.generateAuthTokenFromPaymentsLink(it)
              }
            },
          ) {
            Text("Generate and set token from payments url")
          }
          Text(text = "Custom Story")
          TextField(value = viewState.storyNameInput ?: "", onValueChange = {
            model.onStoryName(it)
          },)
          Button(
            onClick = {
              viewState.storyNameInput?.let {
                model.onStorySelected(it)
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
                model.onMarketClick(market)
              }
            }
          }
          Text(text = "Stories")
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .fillMaxWidth(),
          ) {
            items(viewState.bundles) { bundle ->
              EmbarkStoryItem(bundle) {
                model.onStorySelected(bundle.storyName)
              }
            }
          }
        }
      }
    }
  }

  @Composable
  fun EmbarkStoryItem(bundle: BundlesResult.Success.Bundle, onClick: () -> Unit) {
    Surface(
      shape = MaterialTheme.shapes.medium,
      modifier = Modifier.clickable { onClick() },
    ) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .padding(12.dp)
          .fillMaxWidth(),
      ) {
        Column {
          Text(
            text = bundle.storyName,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
              .padding(bottom = 8.dp)
              .width(300.dp),
          )
        }
      }
    }
  }

  @Composable
  fun MarketItem(market: Market, onClick: () -> Unit) {
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

  override fun onDestroy() {
    super.onDestroy()
    unloadKoinModules(embarkStoryTesterModule)
  }
}

val embarkStoryTesterModule = module {
  viewModel { EmbarkStoryTesterViewModel(get(), get(), get(), get(), get(), get()) }
}

class EmbarkStoryTesterViewModel(
  private val getBundlesUseCase: GetBundlesUseCase,
  private val marketManager: MarketManager,
  private val authenticationTokenService: AuthenticationTokenService,
  private val apolloClient: ApolloClient,
  private val context: Context,
  private val createQuoteCartUseCase: CreateQuoteCartUseCase,
) : ViewModel() {

  data class ViewState(
    val bundles: List<BundlesResult.Success.Bundle> = emptyList(),
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
      fetchEmbarkStories()
      _viewState.value = viewState.value.copy(availableMarkets = marketManager.enabledMarkets)
      createQuoteCartUseCase.invoke().tap { quoteCartId = it }
    }
  }

  private suspend fun fetchEmbarkStories() {
    _viewState.value = when (val bundleResult = getBundlesUseCase.invoke(null)) {
      BundlesResult.Error -> viewState.value.copy(errorMessage = "Could not fetch embark stories")
      is BundlesResult.Success -> viewState.value.copy(bundles = bundleResult.bundles)
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
    Language.persist(context, language)
    viewModelScope.launch {
      fetchEmbarkStories()
    }
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
          .safeQuery()
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
