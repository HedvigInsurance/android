package com.hedvig.app.feature.genericauth.otpinput

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import hedvig.resources.R
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class OtpInputActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
    super.onCreate(savedInstanceState)

    val viewModel: OtpInputViewModel = getViewModel {
      parametersOf(
        intent.getStringExtra(VERIFY_URL_EXTRA)
          ?: error("Programmer error: Missing verifyUrl in ${this.javaClass.name}"),
        intent.getStringExtra(RESEND_URL_EXTRA)
          ?: error("Programmer error: Missing resendUrl in ${this.javaClass.name}"),
        intent.getStringExtra(CREDENTIAL_EXTRA)
          ?: error("Programmer error: Missing CREDENTIAL in ${this.javaClass.name}"),
      )
    }

    setContent {
      val snackbarHostState = remember { SnackbarHostState() }
      LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
          when (event) {
            is OtpInputViewModel.Event.Success -> startLoggedIn()
            OtpInputViewModel.Event.CodeResent -> {
              delay(1.seconds)
              val message = getString(hedvig.resources.R.string.login_snackbar_code_resent)
              snackbarHostState.showSnackbar(message)
            }
          }
        }
      }
      HedvigTheme {
        val viewState by viewModel.viewState.collectAsStateWithLifecycle()
        Surface(
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier.fillMaxSize(),
        ) {
          OtpInputScreen(
            onInputChanged = viewModel::setInput,
            onOpenEmailApp = { openEmail(getString(R.string.login_bottom_sheet_view_code)) },
            onSubmitCode = viewModel::submitCode,
            onResendCode = viewModel::resendCode,
            onBackPressed = { onBackPressedDispatcher.onBackPressed() },
            inputValue = viewState.input,
            credential = viewState.credential,
            networkErrorMessage = viewState.networkErrorMessage,
            loadingResend = viewState.loadingResend,
            loadingCode = viewState.loadingCode,
            snackbarHostState = snackbarHostState,
          )
        }
      }
    }
  }

  private fun startLoggedIn() {
    val intent = LoggedInActivity.newInstance(this, withoutHistory = true)
    startActivity(intent)
  }

  companion object {
    private const val VERIFY_URL_EXTRA = "VERIFY_URL_EXTRA"
    private const val RESEND_URL_EXTRA = "RESEND_URL_EXTRA"
    private const val CREDENTIAL_EXTRA = "CREDENTIAL_EXTRA"

    fun newInstance(context: Context, verifyUrl: String, resendUrl: String, credential: String) =
      Intent(context, OtpInputActivity::class.java).apply {
        putExtra(VERIFY_URL_EXTRA, verifyUrl)
        putExtra(RESEND_URL_EXTRA, resendUrl)
        putExtra(CREDENTIAL_EXTRA, credential)
      }
  }
}

private fun Activity.openEmail(title: String) {
  val emailIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"))

  val resInfo = packageManager.queryIntentActivities(emailIntent, 0)
  if (resInfo.isNotEmpty()) {
    // First create an intent with only the package name of the first registered email app
    // and build a picked based on it
    val intentChooser = packageManager.getLaunchIntentForPackage(
      resInfo.first().activityInfo.packageName,
    )
    val openInChooser = Intent.createChooser(intentChooser, title)

    try {
      // Then create a list of LabeledIntent for the rest of the registered email apps and add to the picker selection
      val emailApps = resInfo.toLabeledIntentArray(packageManager)
      openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, emailApps)
    } catch (_: NullPointerException) {
      // OnePlus crash prevention. Simply go with the initial email app found, don't give more options.
      // console.firebase.google.com/u/0/project/hedvig-app/crashlytics/app/android:com.hedvig.app/issues/06823149a4ff8a411f4508e0cbfae9f4
    }

    startActivity(openInChooser)
  } else {
    logcat(LogPriority.ERROR) { "No email app found" }
  }
}

private fun List<ResolveInfo>.toLabeledIntentArray(packageManager: PackageManager): Array<LabeledIntent> = map {
  val packageName = it.activityInfo.packageName
  val intent = packageManager.getLaunchIntentForPackage(packageName)
  LabeledIntent(intent, packageName, it.loadLabel(packageManager), it.icon)
}.toTypedArray()
