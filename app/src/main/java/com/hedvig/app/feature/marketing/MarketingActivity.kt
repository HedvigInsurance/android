package com.hedvig.app.feature.marketing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hedvig.app.BaseActivity
import com.hedvig.app.authenticate.LoginDialog
import com.hedvig.app.feature.marketing.marketpicked.MarketPickedScreen
import com.hedvig.app.feature.marketing.pickmarket.PickMarketScreen
import com.hedvig.app.feature.marketing.ui.BackgroundImage
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigBlack
import com.hedvig.app.ui.compose.theme.hedvigOffWhite
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.hanalytics.LoginMethod
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MarketingActivity : BaseActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)
    val viewModel = getViewModel<MarketingViewModel>()
    setContent {
      HedvigTheme(
        colorOverrides = {
          it.copy(
            primary = hedvigOffWhite,
            onPrimary = hedvigBlack,
            secondary = hedvigOffWhite,
            onBackground = hedvigOffWhite,
          )
        },
      ) {
        val background by viewModel.background.collectAsState()
        BackgroundImage(background) {
          val state by viewModel.state.collectAsState()

          if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
          }

          val selectedMarket = state.selectedMarket
          if (selectedMarket == null) {
            PickMarketScreen(
              onSubmit = viewModel::submitMarketAndLanguage,
              onSelectMarket = viewModel::setMarket,
              onSelectLanguage = viewModel::setLanguage,
              selectedMarket = state.market,
              selectedLanguage = state.language,
              markets = state.availableMarkets,
              enabled = state.canSetMarketAndLanguage()
            )
          } else {
            MarketPickedScreen(
              onClickMarket = viewModel::onFlagClick,
              onClickSignUp = {
                viewModel.onClickSignUp()
                selectedMarket.openOnboarding(this@MarketingActivity)
              },
              onClickLogIn = {
                viewModel.onClickLogIn()
                onClickLogin(state, selectedMarket)
              },
              flagRes = selectedMarket.flag,
            )
          }
        }
      }
    }
  }

  private fun onClickLogin(
    state: ViewState,
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
