package com.hedvig.app.feature.marketing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.createOnboardingUri
import com.hedvig.app.R
import com.hedvig.app.authenticate.BankIdLoginDialog
import com.hedvig.app.feature.marketing.data.MarketingBackground
import com.hedvig.app.feature.marketing.marketpicked.MarketPickedScreen
import com.hedvig.app.feature.marketing.pickmarket.PickMarketScreen
import com.hedvig.app.feature.marketing.ui.BackgroundImage
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.openWebBrowser
import com.hedvig.hanalytics.LoginMethod
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MarketingActivity : AppCompatActivity() {
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)
    val viewModel = getViewModel<MarketingViewModel>()
    val imageLoader: ImageLoader = get()
    setContent {
      HedvigTheme(darkTheme = true) { // Force dark theme as the background is dark
        val marketingBackground by viewModel.marketingBackground.collectAsStateWithLifecycle()
        val state by viewModel.state.collectAsStateWithLifecycle()
        MarketingScreen(
          marketingBackground = marketingBackground,
          state = state,
          imageLoader = imageLoader,
          submitMarketAndLanguage = viewModel::submitMarketAndLanguage,
          setMarket = viewModel::setMarket,
          setLanguage = viewModel::setLanguage,
          onFlagClick = viewModel::onFlagClick,
          onClickSignUp = { market ->
            viewModel.onClickSignUp()
            openOnboarding(market)
          },
          onClickLogIn = { market ->
            viewModel.onClickLogIn()
            onClickLogin(state, market)
          },
        )
      }
    }
  }

  private fun openOnboarding(market: Market) {
    val baseUrl = getString(R.string.WEB_BASE_URL).substringAfter("//")
    val uri = market.createOnboardingUri(baseUrl, languageService.getLanguage())
    openWebBrowser(uri)
  }

  private fun onClickLogin(
    state: MarketingViewState,
    market: Market,
  ) = when (state.loginMethod) {
    LoginMethod.BANK_ID_SWEDEN -> BankIdLoginDialog().show(
      supportFragmentManager,
      BankIdLoginDialog.TAG,
    )

    LoginMethod.NEM_ID, LoginMethod.BANK_ID_NORWAY -> {
      startActivity(
        SimpleSignAuthenticationActivity.newInstance(
          this@MarketingActivity,
          market,
        ),
      )
    }

    LoginMethod.OTP -> {
      // Not implemented
    }

    null -> {}
  }

  companion object {
    fun newInstance(context: Context): Intent =
      Intent(context, MarketingActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      }
  }
}

@Composable
private fun MarketingScreen(
  marketingBackground: MarketingBackground?,
  state: MarketingViewState,
  imageLoader: ImageLoader,
  submitMarketAndLanguage: () -> Unit,
  setMarket: (Market) -> Unit,
  setLanguage: (Language) -> Unit,
  onFlagClick: () -> Unit,
  onClickSignUp: (market: Market) -> Unit,
  onClickLogIn: (market: Market) -> Unit,
) {
  Box(Modifier.fillMaxSize()) {
    BackgroundImage(marketingBackground, imageLoader)
    val selectedMarket = state.selectedMarket
    Crossfade(selectedMarket) { market ->
      if (market == null) {
        PickMarketScreen(
          onSubmit = submitMarketAndLanguage,
          onSelectMarket = setMarket,
          onSelectLanguage = setLanguage,
          selectedMarket = state.market,
          selectedLanguage = state.language,
          markets = state.availableMarkets,
          enabled = state.canSetMarketAndLanguage(),
        )
      } else {
        MarketPickedScreen(
          onClickMarket = onFlagClick,
          onClickSignUp = { onClickSignUp(market) },
          onClickLogIn = { onClickLogIn(market) },
          flagRes = market.flag,
        )
      }
    }
    if (state.isLoading) {
      CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
  }
}
