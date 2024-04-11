package com.hedvig.app.feature.marketing

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.datadog.android.compose.ExperimentalTrackingApi
import com.datadog.android.compose.NavigationViewTrackingEffect
import com.hedvig.android.app.ui.SafeAndroidUriHandler
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.login.navigation.LoginDestination
import com.hedvig.android.feature.login.navigation.loginGraph
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import hedvig.resources.R
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MarketingActivity : AppCompatActivity() {
  private val hedvigBuildConstants: HedvigBuildConstants by inject()
  private val activityNavigator: ActivityNavigator by inject()
  private val demoManager: DemoManager by inject()
  private val featureManager: FeatureManager by inject()

  @OptIn(ExperimentalComposeUiApi::class, ExperimentalTrackingApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
    super.onCreate(savedInstanceState)
    lifecycleScope.launch {
      launch {
        featureManager.isFeatureEnabled(Feature.UPDATE_NECESSARY).collectLatest {
          if (it) {
            applicationContext.startActivity(ForceUpgradeActivity.newInstance(applicationContext))
            finish()
            cancel()
          }
        }
      }
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        demoManager.isDemoMode().first { it == true }
        activityNavigator.navigateToLoggedInScreen(this@MarketingActivity, true)
        finish()
      }
    }
    val safeAndroidUriHandler = SafeAndroidUriHandler(this)
    val openEmailSheetTitle = getString(R.string.login_bottom_sheet_view_code)
    val openEmailApp = { openEmail(openEmailSheetTitle) }
    setContent {
      HedvigTheme {
        val navController = rememberNavController()
        NavigationViewTrackingEffect(navController = navController)
        val navigator = rememberNavigator(navController)
        NavHost(
          navController = navController,
          startDestination = createRoutePattern<LoginDestination>(),
          route = "marketing-root",
        ) {
          loginGraph(
            navigator = navigator,
            appVersionName = hedvigBuildConstants.appVersionName,
            urlBaseWeb = hedvigBuildConstants.urlBaseWeb,
            openUrl = { safeAndroidUriHandler.openUri(it) },
            onOpenEmailApp = openEmailApp,
            startLoggedInActivity = {
              activityNavigator.navigateToLoggedInScreen(this@MarketingActivity, true)
              finish()
            },
          )
        }
      }
    }
  }

  companion object {
    fun newInstance(context: Context): Intent = Intent(context, MarketingActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
  }
}

@Composable
private fun rememberNavigator(navController: NavController): Navigator {
  return remember(navController) {
    object : Navigator {
      override fun NavBackStackEntry.navigate(
        destination: Destination,
        navOptions: NavOptions?,
        navigatorExtras: androidx.navigation.Navigator.Extras?,
      ) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
          navigateUnsafe(destination, navOptions, navigatorExtras)
        }
      }

      override fun navigateUnsafe(
        destination: Destination,
        navOptions: NavOptions?,
        navigatorExtras: androidx.navigation.Navigator.Extras?,
      ) {
        navController.navigate(destination, navOptions, navigatorExtras)
      }

      override fun navigateUp() {
        navController.navigateUp()
      }

      override fun popBackStack() {
        navController.popBackStack()
      }
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
