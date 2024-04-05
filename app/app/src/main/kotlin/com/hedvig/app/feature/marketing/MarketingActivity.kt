package com.hedvig.app.feature.marketing

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
import com.hedvig.android.feature.login.navigation.LoginGraph
import com.hedvig.android.feature.login.navigation.loginGraph
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.navigation.activity.ActivityNavigator
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.app.feature.genericauth.GenericAuthActivity
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
        activityNavigator.navigateToLoggedInScreen(this@MarketingActivity, false)
        finish()
      }
    }
    val safeAndroidUriHandler = SafeAndroidUriHandler(this)
    setContent {
      HedvigTheme {
        val navController = rememberNavController()
        NavigationViewTrackingEffect(navController = navController)
        val navigator = rememberNavigator(navController)
        NavHost(
          navController = navController,
          startDestination = LoginGraph,
          route = MarketingRootGraphDestination::class,
          modifier = Modifier.semantics { testTagsAsResourceId = true },
        ) {
          loginGraph(
            navigator = navigator,
            appVersionName = hedvigBuildConstants.appVersionName,
            urlBaseWeb = hedvigBuildConstants.urlBaseWeb,
            openUrl = { safeAndroidUriHandler.openUri(it) },
            startLoggedInActivity = {
              activityNavigator.navigateToLoggedInScreen(this@MarketingActivity, false)
              finish()
            },
            startDKLogin = {
              startActivity(GenericAuthActivity.newInstance(this@MarketingActivity))
            },
            startNOLogin = {
              startActivity(GenericAuthActivity.newInstance(this@MarketingActivity))
            },
            startOtpLogin = {
              startActivity(GenericAuthActivity.newInstance(this@MarketingActivity))
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
      override fun <T : Any> NavBackStackEntry.navigate(
        destination: T,
        navOptions: NavOptions?,
        navigatorExtras: androidx.navigation.Navigator.Extras?,
      ) {
        if (lifecycle.currentState == Lifecycle.State.RESUMED) {
          navigateUnsafe(destination, navOptions, navigatorExtras)
        }
      }

      override fun <T : Any> navigateUnsafe(
        destination: T,
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

@Serializable
@SerialName("marketing-root")
private data object MarketingRootGraphDestination
