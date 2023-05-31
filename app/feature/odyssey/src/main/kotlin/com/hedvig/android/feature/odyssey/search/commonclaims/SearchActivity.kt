package com.hedvig.android.feature.odyssey.search.commonclaims

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.CenterAlignedTopAppBar
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.feature.odyssey.ClaimFlowActivity
import com.hedvig.android.feature.odyssey.search.commonclaims.ui.CommonClaims
import hedvig.resources.R
import org.koin.androidx.viewmodel.ext.android.getViewModel

class SearchActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    setContent {
      val viewModel: SearchViewModel = getViewModel()
      val viewState by viewModel.viewState.collectAsState()

      val entryPointId = viewState.selectedClaim?.entryPointId
      LaunchedEffect(entryPointId) {
        if (entryPointId != null) {
          viewModel.resetState()
          startActivity(
            ClaimFlowActivity.newInstance(this@SearchActivity, entryPointId),
          )
        }
      }

      HedvigTheme {
        Surface(color = MaterialTheme.colors.background) {
          Box(modifier = Modifier.fillMaxHeight()) {
            Column {
              CenterAlignedTopAppBar(
                title = "",
                onClick = { finish() },
                backgroundColor = MaterialTheme.colors.background,
                icon = Icons.Filled.ArrowBack,
              )

              Text(
                text = getString(R.string.CLAIM_TRIAGING_TITLE),
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                  fontFamily = FontFamily(
                    Font(R.font.hedvig_letters_small),
                  ),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(22.dp),
              )

              Spacer(modifier = Modifier.padding(8.dp))

              val errorMessage = viewState.errorMessage
              if (errorMessage != null) {
                GenericErrorScreen(
                  onRetryButtonClick = { viewModel.loadSearchableClaims() },
                  modifier = Modifier.padding(16.dp),
                )
              } else {
                CommonClaims(
                  selectClaim = viewModel::onSelectClaim,
                  commonClaims = viewState.commonClaims,
                )
              }
            }

            if (viewState.isLoading) {
              CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
          }
        }
      }
    }
  }

  companion object {
    fun newInstance(context: Context) = Intent(context, SearchActivity::class.java)
  }
}
