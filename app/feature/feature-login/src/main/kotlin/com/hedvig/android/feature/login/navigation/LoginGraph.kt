package com.hedvig.android.feature.login.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.feature.login.marketing.MarketingDestination
import com.hedvig.android.feature.login.marketing.MarketingViewModel
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginDestination
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginViewModel
import com.hedvig.android.language.Language
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.market.createOnboardingUri
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.loginGraph(
  navigator: Navigator,
  appVersionName: String,
  urlBaseWeb: String,
  openWebsite: (Uri) -> Unit,
  startLoggedInActivity: () -> Unit,
  startDKLogin: () -> Unit,
  startNOLogin: () -> Unit,
) {
  navigation<AppDestination.Login>(
    startDestination = createRoutePattern<LoginDestination.Marketing>(),
  ) {
    composable<LoginDestination.Marketing> { backStackEntry ->
      val marketingViewModel: MarketingViewModel = koinViewModel()
      val locale = getLocale()
      MarketingDestination(
        viewModel = marketingViewModel,
        appVersionName = appVersionName,
        openWebOnboarding = { market ->
          val baseUrl = urlBaseWeb.substringAfter("//")
          val uri = market.createOnboardingUri(baseUrl, Language.from(locale.toLanguageTag()))
          openWebsite(uri)
        },
        navigateToLoginScreen = { market ->
          logcat { "Navigating to login screen for market market:$market" }
          with(navigator) {
            when (market) {
              Market.SE -> backStackEntry.navigate(LoginDestination.SwedishLogin)
              Market.NO -> startNOLogin()
              Market.DK -> startDKLogin()
            }
          }
        },
      )
    }
    composable<LoginDestination.SwedishLogin> { backStackEntry ->
      val swedishLoginViewModel: SwedishLoginViewModel = koinViewModel()
      SwedishLoginDestination(
        swedishLoginViewModel = swedishLoginViewModel,
        navigateUp = navigator::navigateUp,
        navigateToEmailLogin = { with(navigator) { backStackEntry.navigate(LoginDestination.OtpLogin) } },
        startLoggedInActivity = startLoggedInActivity,
      )
    }
    composable<LoginDestination.OtpLogin>() {
      Text("TODO")
    }
  }
}
