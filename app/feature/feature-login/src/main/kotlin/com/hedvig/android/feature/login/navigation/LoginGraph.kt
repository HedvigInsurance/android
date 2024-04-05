package com.hedvig.android.feature.login.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.feature.login.marketing.MarketingDestination
import com.hedvig.android.feature.login.marketing.MarketingViewModel
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginDestination
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginViewModel
import com.hedvig.android.language.Language
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.loginGraph(
  navigator: Navigator,
  appVersionName: String,
  urlBaseWeb: String,
  openUrl: (String) -> Unit,
  startLoggedInActivity: () -> Unit,
  startDKLogin: () -> Unit,
  startNOLogin: () -> Unit,
  startOtpLogin: () -> Unit,
) {
  navigation<LoginGraph>(
    startDestination = LoginDestination.Marketing::class,
  ) {
    composable<LoginDestination.Marketing> { backStackEntry ->
      val marketingViewModel: MarketingViewModel = koinViewModel()
      val locale = getLocale()
      MarketingDestination(
        viewModel = marketingViewModel,
        appVersionName = appVersionName,
        openWebOnboarding = { market ->
          val baseUrl = urlBaseWeb.substringAfter("//")
          val uri = market.createOnboardingUri(baseUrl, Language.from(locale.toLanguageTag())).toString()
          openUrl(uri)
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
    composable<LoginDestination.SwedishLogin> { _ ->
      val swedishLoginViewModel: SwedishLoginViewModel = koinViewModel()
      SwedishLoginDestination(
        swedishLoginViewModel = swedishLoginViewModel,
        navigateUp = navigator::navigateUp,
        navigateToEmailLogin = {
          logcat(LogPriority.INFO) { "Login with OTP clicked" }
          startOtpLogin()
        },
        startLoggedInActivity = startLoggedInActivity,
      )
    }
  }
}

private fun Market.createOnboardingUri(baseUrl: String, language: Language): Uri {
  val webPath = language.webPath()
  val builder = Uri.Builder()
    .scheme("https")
    .authority(baseUrl)
    .appendPath(webPath)
    .appendPath(
      when (language) {
        Language.SV_SE -> "forsakringar"
        Language.EN_SE,
        Language.NB_NO,
        Language.EN_NO,
        Language.DA_DK,
        Language.EN_DK,
        -> "insurances"
      },
    )
    .appendQueryParameter("utm_source", "android")
    .appendQueryParameter("utm_medium", "hedvig-app")

  if (this == Market.SE) {
    builder.appendQueryParameter("utm_campaign", "se")
  }

  return builder.build()
}
