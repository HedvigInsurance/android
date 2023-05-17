package com.hedvig.android.feature.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import coil.ImageLoader
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.tryOpenPlayStore
import com.hedvig.android.core.designsystem.theme.ConfigureTransparentSystemBars
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.odyssey.navigation.ClaimFlowNavHost
import com.hedvig.android.navigation.activity.Navigator
import org.koin.android.ext.android.inject

class ClaimFlowActivity : AppCompatActivity() {

  private val imageLoader: ImageLoader by inject()
  private val activityNavigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val commonClaimId: String? = intent.getStringExtra(
      com.hedvig.android.feature.odyssey.ClaimFlowActivity.Companion.COMMON_CLAIM_ID,
    )

    setContent {
      HedvigTheme {
        ConfigureTransparentSystemBars()
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          ClaimFlowNavHost(
            windowSizeClass = calculateWindowSizeClass(this@ClaimFlowActivity),
            navController = rememberAnimatedNavController(),
            imageLoader = imageLoader,
            entryPointId = commonClaimId,
            openAppSettings = {
              activityNavigator.openAppSettings(this@ClaimFlowActivity)
            },
            openPlayStore = { tryOpenPlayStore() },
            shouldShowRequestPermissionRationale = ::shouldShowRequestPermissionRationale,
            openChat = {
              onSupportNavigateUp()
              activityNavigator.navigateToChat(this@ClaimFlowActivity)
            },
            navigateUp = ::onSupportNavigateUp,
          )
        }
      }
    }
  }

  companion object {
    private const val COMMON_CLAIM_ID = "COMMON_CLAIM_ID"

    fun newInstance(
      context: Context,
      commonClaimId: String?,
    ): Intent {
      return Intent(context, com.hedvig.android.feature.odyssey.ClaimFlowActivity::class.java)
        .putExtra(com.hedvig.android.feature.odyssey.ClaimFlowActivity.Companion.COMMON_CLAIM_ID, commonClaimId)
    }
  }
}
