package com.hedvig.android.feature.login.navigation

import android.net.Uri
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
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
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateUp
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.loginGraph(
  backStack: MutableList<HedvigNavKey>,
  appVersionName: String,
  urlBaseWeb: String,
  openUrl: (String) -> Unit,
  onOpenEmailApp: () -> Unit,
  onNavigateToLoggedIn: () -> Unit,
) {
  navdestination<LoginKey> {
    val marketingViewModel: MarketingViewModel = metroViewModel()
    val locale = getLocale()
    MarketingDestination(
      viewModel = marketingViewModel,
      appVersionName = appVersionName,
      openWebOnboarding = dropUnlessResumed {
        val baseUrl = urlBaseWeb.substringAfter("//")
        val uri = createOnboardingUri(baseUrl, Language.from(locale.toLanguageTag())).toString()
        openUrl(uri)
      },
      navigateToLoginScreen = dropUnlessResumed {
        backStack.add(SwedishLoginKey)
      },
    )
  }
  navdestination<SwedishLoginKey> {
    val swedishLoginViewModel: SwedishLoginViewModel = metroViewModel()
    SwedishLoginDestination(
      swedishLoginViewModel = swedishLoginViewModel,
      navigateUp = backStack::navigateUp,
      navigateToEmailLogin = dropUnlessResumed {
        logcat(LogPriority.INFO) { "Login with OTP clicked" }
        backStack.add(GenericAuthCredentialsInputKey)
      },
      onNavigateToLoggedIn = onNavigateToLoggedIn,
    )
  }
  navdestination<GenericAuthCredentialsInputKey> {
    val viewModel: GenericAuthViewModel = metroViewModel()
    GenericAuthDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      onStartOtpInput = { verifyUrl: String, resendUrl: String, email: String ->
        backStack.add(
          OtpInputKey(OtpInputKey.OtpInformation(verifyUrl, resendUrl, email)),
        )
      },
    )
  }
  navdestination<OtpInputKey> {
    val otpInputInformation: OtpInputKey.OtpInformation = this.otpInformation
    val viewModel: OtpInputViewModel =
      assistedMetroViewModel<OtpInputViewModel, OtpInputViewModel.Factory> {
        create(otpInputInformation.verifyUrl, otpInputInformation.resendUrl, otpInputInformation.credential)
      }
    OtpInputDestination(
      viewModel = viewModel,
      navigateUp = backStack::navigateUp,
      onNavigateToLoggedIn = onNavigateToLoggedIn,
      onOpenEmailApp = dropUnlessResumed { onOpenEmailApp() },
    )
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
