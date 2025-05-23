package com.hedvig.android.feature.login.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.login.genericauth.GenericAuthDestination
import com.hedvig.android.feature.login.genericauth.GenericAuthViewModel
import com.hedvig.android.feature.login.marketing.MarketingDestination
import com.hedvig.android.feature.login.marketing.MarketingViewModel
import com.hedvig.android.feature.login.otpinput.OtpInputDestination
import com.hedvig.android.feature.login.otpinput.OtpInputViewModel
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginDestination
import com.hedvig.android.feature.login.swedishlogin.SwedishLoginViewModel
import com.hedvig.android.language.Language
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navgraph
import com.hedvig.android.navigation.core.Navigator
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.loginGraph(
  navigator: Navigator,
  appVersionName: String,
  urlBaseWeb: String,
  openUrl: (String) -> Unit,
  onOpenEmailApp: () -> Unit,
  onNavigateToLoggedIn: () -> Unit,
) {
  navgraph<LoginDestination>(
    startDestination = LoginDestinations.Marketing::class,
  ) {
    navdestination<LoginDestinations.Marketing> { backStackEntry ->
      val marketingViewModel: MarketingViewModel = koinViewModel()
      val locale = getLocale()
      MarketingDestination(
        viewModel = marketingViewModel,
        appVersionName = appVersionName,
        openWebOnboarding = {
          val baseUrl = urlBaseWeb.substringAfter("//")
          val uri = createOnboardingUri(baseUrl, Language.from(locale.toLanguageTag())).toString()
          openUrl(uri)
        },
        navigateToLoginScreen = {
          with(navigator) {
            backStackEntry.navigate(LoginDestinations.SwedishLogin)
          }
        },
      )
    }
    navdestination<LoginDestinations.SwedishLogin> { backStackEntry ->
      val swedishLoginViewModel: SwedishLoginViewModel = koinViewModel()
      SwedishLoginDestination(
        swedishLoginViewModel = swedishLoginViewModel,
        navigateUp = navigator::navigateUp,
        navigateToEmailLogin = {
          logcat(LogPriority.INFO) { "Login with OTP clicked" }
          with(navigator) {
            backStackEntry.navigate(LoginDestinations.GenericAuthCredentialsInput)
          }
        },
        onNavigateToLoggedIn = onNavigateToLoggedIn,
      )
    }
    navdestination<LoginDestinations.GenericAuthCredentialsInput> { backStackEntry ->
      val viewModel: GenericAuthViewModel = koinViewModel()
      GenericAuthDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onStartOtpInput = { verifyUrl: String, resendUrl: String, email: String ->
          with(navigator) {
            backStackEntry.navigate(
              LoginDestinations.OtpInput(LoginDestinations.OtpInput.OtpInformation(verifyUrl, resendUrl, email)),
            )
          }
        },
      )
    }
    navdestination<LoginDestinations.OtpInput>(
      LoginDestinations.OtpInput,
    ) {
      val otpInputInformation: LoginDestinations.OtpInput.OtpInformation = this.otpInformation
      val viewModel: OtpInputViewModel = koinViewModel { parametersOf(otpInputInformation) }
      OtpInputDestination(
        viewModel = viewModel,
        navigateUp = navigator::navigateUp,
        onNavigateToLoggedIn = onNavigateToLoggedIn,
        onOpenEmailApp = onOpenEmailApp,
      )
    }
  }
}

private fun createOnboardingUri(baseUrl: String, language: Language): Uri {
  val webPath = language.webPath()
  val builder = Uri.Builder()
    .scheme("https")
    .authority(baseUrl)
    .appendPath(webPath)
    .appendPath(
      when (language) {
        Language.SV_SE -> "forsakringar"
        Language.EN_SE -> "insurances"
      },
    )
    .appendQueryParameter("utm_source", "android")
    .appendQueryParameter("utm_medium", "hedvig-app")
    .appendQueryParameter("utm_campaign", "se")

  return builder.build()
}
