package com.hedvig.android.odyssey.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.CenterAlignedTopAppBar
import com.hedvig.android.odyssey.OdysseyClaimsFlowActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SearchActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    setContent {
      val viewModel: SearchViewModel = getViewModel()
      val viewState by viewModel.viewState.collectAsState()

      val commonClaimId = viewState.selectedClaim?.id
      if (commonClaimId != null) {
        startClaimsFlow(viewModel, commonClaimId)
      }

      HedvigTheme {

        Surface(color = MaterialTheme.colors.background) {
          Column {
            CenterAlignedTopAppBar(
              title = "",
              onClick = { finish() },
              backgroundColor = MaterialTheme.colors.background,
              icon = Icons.Filled.ArrowBack,
            )

            Text(
              text = getString(hedvig.resources.R.string.home_tab_common_claims_title),
              style = MaterialTheme.typography.h5,
              modifier = Modifier.padding(22.dp),
            )

            Spacer(modifier = Modifier.padding(8.dp))

            CommonClaims(
              selectClaim = viewModel::onSelectClaim,
              commonClaims = viewState.commonClaims,
              selectOther = {
                startClaimsFlow(
                  viewModel = viewModel,
                  commonClaimId = null,
                )
              },
            )
          }
        }
      }
    }
  }

  private fun startClaimsFlow(viewModel: SearchViewModel, commonClaimId: String?) {
    val intent = OdysseyClaimsFlowActivity.newInstance(
      context = this,
      odysseyUrl = intent.getStringExtra(ODYSSEY_URL) ?: throw java.lang.IllegalArgumentException("No url found"),
      commonClaimId = commonClaimId,
    )
    startActivity(intent)
    viewModel.resetState()
  }

  companion object {
    const val ODYSSEY_URL = "ODYSSEY_URL_EXTRA"
  }
}
