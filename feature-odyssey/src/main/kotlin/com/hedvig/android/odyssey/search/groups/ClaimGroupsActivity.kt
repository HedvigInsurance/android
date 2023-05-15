package com.hedvig.android.odyssey.search.groups

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.CenterAlignedTopAppBar
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.odyssey.search.group.ClaimGroupActivity
import com.hedvig.android.odyssey.search.groups.ui.ClaimGroups
import hedvig.resources.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import slimber.log.e

class ClaimGroupsActivity : ComponentActivity() {

  private val imageLoader: ImageLoader by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    setContent {
      val viewModel: ClaimGroupsViewModel = getViewModel()
      val uiState by viewModel.uiState.collectAsState()

      val entryPointId = uiState.selectedClaim?.id
      LaunchedEffect(entryPointId) {
        if (entryPointId != null) {
          viewModel.resetState()
          startActivity(ClaimGroupActivity.newInstance(this@ClaimGroupsActivity, entryPointId))
        }
      }

      HedvigTheme {
        Box(modifier = Modifier.fillMaxHeight()) {
          if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
          }

          Column {
            CenterAlignedTopAppBar(
              title = "",
              onClick = { finish() },
              backgroundColor = MaterialTheme.colors.background,
              icon = Icons.Filled.ArrowBack,
            )

            Column(Modifier.verticalScroll(rememberScrollState())) {
              AnimatedVisibility(
                visible = uiState.memberName != null,
                enter = fadeIn(),
              ) {
                Text(
                  text = stringResource(id = R.string.CLAIM_TRIAGING_ABOUT_TITILE, uiState.memberName!!),
                  style = androidx.compose.material3.MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily(
                      Font(R.font.hedvig_letters_small),
                    ),
                  ),
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(22.dp),
                )
              }

              Spacer(modifier = Modifier.padding(top = 28.dp))

              val errorMessage = uiState.errorMessage
              if (errorMessage != null) {
                LaunchedEffect(Unit) { e { "SearchActivity: errorMessage$errorMessage" } }
                GenericErrorScreen(
                  description = errorMessage,
                  onRetryButtonClick = { viewModel.loadClaimGroups() },
                  modifier = Modifier.padding(16.dp),
                )
              } else {
                ClaimGroups(
                  selectGroup = viewModel::onSelectClaimGroup,
                  claimGroups = uiState.claimGroups,
                  imageLoader = imageLoader,
                )
              }
            }
          }
        }
      }
    }
  }

  companion object {
    fun newInstance(context: Context) = Intent(context, ClaimGroupsActivity::class.java)
  }
}
