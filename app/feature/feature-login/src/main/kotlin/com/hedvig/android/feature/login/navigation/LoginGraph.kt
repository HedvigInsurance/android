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
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.add
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel

fun EntryProviderScope<HedvigNavKey>.loginGraph(
  backstack: Backstack,
  appVersionName: String,
  urlBaseWeb: String,
  openUrl: (String) -> Unit,
  onOpenEmailApp: () -> Unit,
  onNavigateToLoggedIn: () -> Unit,
) {
  entry<LoginKey> {
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
        backstack.add(SwedishLoginKey)
      },
    )
  }
  entry<SwedishLoginKey> {
    val swedishLoginViewModel: SwedishLoginViewModel = assistedMetroViewModel()
    SwedishLoginDestination(
      swedishLoginViewModel = swedishLoginViewModel,
      navigateUp = backstack::navigateUp,
      navigateToEmailLogin = dropUnlessResumed {
        logcat(LogPriority.INFO) { "Login with OTP clicked" }
        backstack.add(GenericAuthCredentialsInputKey)
      },
      onNavigateToLoggedIn = onNavigateToLoggedIn,
    )
  }
  entry<GenericAuthCredentialsInputKey> {
    val viewModel: GenericAuthViewModel = metroViewModel()
    GenericAuthDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
      onStartOtpInput = { verifyUrl: String, resendUrl: String, email: String ->
        backstack.add(
          OtpInputKey(OtpInputKey.OtpInformation(verifyUrl, resendUrl, email)),
        )
      },
    )
  }
  entry<OtpInputKey> { key ->
    val otpInputInformation: OtpInputKey.OtpInformation = key.otpInformation
    val viewModel: OtpInputViewModel =
      assistedMetroViewModel<OtpInputViewModel, OtpInputViewModel.Factory> {
        create(otpInputInformation.verifyUrl, otpInputInformation.resendUrl, otpInputInformation.credential)
      }
    OtpInputDestination(
      viewModel = viewModel,
      navigateUp = backstack::navigateUp,
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
