package com.hedvig.android.odyssey.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.CenterAlignedTopAppBar
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.odyssey.ClaimFlowActivity
import com.hedvig.android.odyssey.sdui.OdysseyClaimsFlowActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SearchActivity : ComponentActivity() {

  private val featureManager: FeatureManager by inject()

  private val odysseyUrl by lazy {
    intent.getStringExtra(ODYSSEY_URL) ?: throw IllegalArgumentException("No url found")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    setContent {
      val viewModel: SearchViewModel = getViewModel()
      val viewState by viewModel.viewState.collectAsState()

      val commonClaimId = viewState.selectedClaim?.id
      LaunchedEffect(commonClaimId) {
        if (commonClaimId != null) {
          startClaimsFlow(viewModel, commonClaimId)
        }
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

            val coroutineScope = rememberCoroutineScope()

            CommonClaims(
              selectClaim = viewModel::onSelectClaim,
              commonClaims = viewState.commonClaims,
              selectOther = {
                coroutineScope.launch {
                  startClaimsFlow(
                    viewModel = viewModel,
                    commonClaimId = null,
                  )
                }
              },
            )
          }
        }
      }
    }
  }

  private suspend fun startClaimsFlow(viewModel: SearchViewModel, commonClaimId: String?) {
    val intent = createClaimsFlowIntent(commonClaimId)
    startActivity(intent)
    viewModel.resetState()
  }

  private suspend fun createClaimsFlowIntent(commonClaimId: String?) =
    if (featureManager.isFeatureEnabled(Feature.USE_NATIVE_CLAIMS_FLOW)) {
      ClaimFlowActivity.newInstance(this, commonClaimId)
    } else {
      OdysseyClaimsFlowActivity.newInstance(
        context = this,
        odysseyUrl = odysseyUrl,
        commonClaimId = commonClaimId,
      )
    }

  companion object {
    const val ODYSSEY_URL = "ODYSSEY_URL_EXTRA"

    fun newInstance(context: Context, url: String) = Intent(context, SearchActivity::class.java)
      .putExtra(ODYSSEY_URL, url)
  }
}
