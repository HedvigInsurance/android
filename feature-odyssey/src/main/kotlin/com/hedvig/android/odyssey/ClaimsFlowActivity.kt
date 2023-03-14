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
import coil.ImageLoader
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.navigation.activity.Navigator
import com.hedvig.android.odyssey.input.InputViewModel
import com.hedvig.android.odyssey.navigation.ClaimFlowNavHost
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class ClaimsFlowActivity : AppCompatActivity() {

  private val imageLoader: ImageLoader by inject()
  private val activityNavigator: Navigator by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val commonClaimId = intent.getStringExtra(COMMON_CLAIM_ID)

    setContent {
      val inputViewModel: InputViewModel = getViewModel { parametersOf(commonClaimId) }
      HedvigTheme {
        Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
          ClaimFlowNavHost(
            windowSizeClass = calculateWindowSizeClass(this@ClaimsFlowActivity),
            navController = rememberAnimatedNavController(),
            imageLoader = imageLoader,
            openChat = {
              onSupportNavigateUp()
              activityNavigator.navigateToChat(this@ClaimsFlowActivity)
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
      return Intent(context, ClaimsFlowActivity::class.java)
        .putExtra(COMMON_CLAIM_ID, commonClaimId)
    }
  }
}
