package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import coil.ImageLoader
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.navigation.activity.Navigator
import com.hedvig.android.odyssey.navigation.ClaimFlowNavHost
import org.koin.android.ext.android.inject

class ClaimFlowActivity : AppCompatActivity() {

  private val imageLoader: ImageLoader by inject()
  private val activityNavigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val commonClaimId: String? = intent.getStringExtra(COMMON_CLAIM_ID)

    setContent {
      HedvigTheme {
        Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
          ClaimFlowNavHost(
            windowSizeClass = calculateWindowSizeClass(this@ClaimFlowActivity),
            navController = rememberAnimatedNavController(),
            imageLoader = imageLoader,
            entryPointId = commonClaimId,
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
      return Intent(context, ClaimFlowActivity::class.java)
        .putExtra(COMMON_CLAIM_ID, commonClaimId)
    }
  }
}
