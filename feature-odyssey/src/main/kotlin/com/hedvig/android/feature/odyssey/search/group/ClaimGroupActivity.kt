package com.hedvig.android.feature.odyssey.search.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.CenterAlignedTopAppBar
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.feature.odyssey.ClaimFlowActivity
import com.hedvig.android.feature.odyssey.search.group.ui.ClaimGroupSelector
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import slimber.log.e

class ClaimGroupActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    setContent {
      val viewModel = koinViewModel<ClaimGroupViewModel> { parametersOf(intent.getStringExtra("groupId")) }
      val uiState by viewModel.uiState.collectAsState()

      HedvigTheme {
        Box(modifier = Modifier.fillMaxHeight()) {
          if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
          }

          Column {
            CenterAlignedTopAppBar(
              title = "",
              onClick = { finish() },
              backgroundColor = MaterialTheme.colorScheme.background,
              icon = Icons.Filled.ArrowBack,
            )

            val errorMessage = uiState.errorMessage
            if (errorMessage != null) {
              LaunchedEffect(Unit) { e { "SearchActivity: errorMessage$errorMessage" } }
              GenericErrorScreen(
                onRetryButtonClick = { viewModel.loadClaimGroup() },
                description = errorMessage,
                modifier = Modifier.padding(16.dp),
              )
            } else {
              AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn(),
              ) {
                ClaimGroupSelector(
                  uiState = uiState,
                  viewModel = viewModel,
                  onContinueClicked = {
                    startActivity(ClaimFlowActivity.newInstance(this@ClaimGroupActivity, it))
                  },
                )
              }
            }
          }
        }
      }
    }
  }

  companion object {
    fun newInstance(context: Context, groupId: String) =
      Intent(context, ClaimGroupActivity::class.java).putExtra("groupId", groupId)
  }
}
