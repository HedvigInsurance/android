package com.hedvig.app.feature.marketing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.createOnboardingUri
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.authenticate.LoginDialog
import com.hedvig.app.feature.marketing.data.MarketingBackground
import com.hedvig.app.feature.marketing.marketpicked.MarketPickedScreen
import com.hedvig.app.feature.marketing.pickmarket.PickMarketScreen
import com.hedvig.app.feature.marketing.ui.BackgroundImage
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity
import com.hedvig.android.core.designsystem.theme.hedvigBlack
import com.hedvig.android.core.designsystem.theme.hedvigOffWhite
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.hanalytics.LoginMethod
import e
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MarketingActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)
    val viewModel = getViewModel<MarketingViewModel>()
    setContent {
      HedvigTheme(
        colorOverrides = { colors ->
          colors.copy(
            primary = hedvigOffWhite,
            onPrimary = hedvigBlack,
            secondary = hedvigOffWhite,
            onBackground = hedvigOffWhite,
          )
        },
      ) {
        val marketingBackground by viewModel.marketingBackground.collectAsState()
        val state by viewModel.state.collectAsState()
        MarketingScreen(
          marketingBackground,
          state,
          viewModel::submitMarketAndLanguage,
          viewModel::setMarket,
          viewModel::setLanguage,
          viewModel::onFlagClick,
          viewModel::onClickSignUp,
          viewModel::onClickLogIn,
        )
      }
    }
  }

  @Composable
  private fun MarketingScreen(
    marketingBackground: MarketingBackground?,
    state: MarketingViewState,
    submitMarketAndLanguage: () -> Unit,
    setMarket: (Market) -> Unit,
    setLanguage: (Language) -> Unit,
    onFlagClick: () -> Unit,
    onClickSignUp: () -> Unit,
    onClickLogIn: () -> Unit,
  ) {
    Box(Modifier.fillMaxSize()) {
      BackgroundImage(marketingBackground)
      val selectedMarket = state.selectedMarket
      if (selectedMarket == null) {
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
          onClickSignUp = {
            onClickSignUp()
            openOnboarding(selectedMarket)
          },
          onClickLogIn = {
            onClickLogIn()
            onClickLogin(state, selectedMarket)
          },
          flagRes = selectedMarket.flag,
        )
      }
      if (state.isLoading) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
      }
    }
  }

  private fun openOnboarding(market: Market) {
    val baseUrl = getString(R.string.WEB_BASE_URL).substringAfter("//")
    val uri = market.createOnboardingUri(this, baseUrl)
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)

    if (browserIntent.resolveActivity(packageManager) != null) {
      startActivity(browserIntent)
    } else {
      e { "Tried to launch $uri but the phone has nothing to support such an intent." }
      makeToast(hedvig.resources.R.string.general_unknown_error)
    }
  }

  private fun onClickLogin(
    state: MarketingViewState,
    market: Market,
  ) = when (state.loginMethod) {
    LoginMethod.BANK_ID_SWEDEN -> LoginDialog().show(
      supportFragmentManager,
      LoginDialog.TAG,
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
    fun newInstance(context: Context, withoutHistory: Boolean = false) =
      Intent(context, MarketingActivity::class.java).apply {
        if (withoutHistory) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
      }
  }
}
