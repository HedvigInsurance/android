package com.hedvig.android.odyssey

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil.ImageLoader
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.odyssey.input.InputViewModel
import com.hedvig.android.odyssey.input.ui.InputRoot
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.resolution.ui.ResolutionRoot
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class ClaimsFlowActivity : ComponentActivity() {

  private val imageLoader: ImageLoader by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val commonClaimId = intent.getStringExtra(COMMON_CLAIM_ID)

    setContent {
      val inputViewModel: InputViewModel = getViewModel { parametersOf(commonClaimId) }
      val viewState by inputViewModel.viewState.collectAsState()

      HedvigTheme {
        if (viewState.resolution == Resolution.None) {
          InputRoot(
            viewState = viewState,
            inputViewModel = inputViewModel,
            audioRecorderViewModel = getViewModel(),
            onFinish = ::finish,
            imageLoader = imageLoader,
          )
        } else {
          ResolutionRoot(
            viewModel = getViewModel { parametersOf(viewState.resolution) },
            resolution = viewState.resolution,
            onFinish = ::finish,
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
