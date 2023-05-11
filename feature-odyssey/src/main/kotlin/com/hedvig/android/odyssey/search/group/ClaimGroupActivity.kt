package com.hedvig.android.odyssey.search.group

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.CenterAlignedTopAppBar
import com.hedvig.android.core.ui.error.ErrorDialog
import com.hedvig.android.odyssey.ClaimFlowActivity
import hedvig.resources.R
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalLayoutApi::class)
class ClaimGroupActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    setContent {
      val viewModel = koinViewModel<ClaimGroupViewModel> { parametersOf(intent.getStringExtra("groupId")) }
      val uiState by viewModel.uiState.collectAsState()

      HedvigTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
          Box(modifier = Modifier.fillMaxHeight()) {
            Column {
              CenterAlignedTopAppBar(
                title = "",
                onClick = { finish() },
                backgroundColor = MaterialTheme.colorScheme.background,
                icon = Icons.Filled.ArrowBack,
              )

              Spacer(modifier = Modifier.padding(top = 28.dp))

              val errorMessage = uiState.errorMessage
              if (errorMessage != null) {
                ErrorDialog(message = errorMessage, onDismiss = { viewModel.resetState() })
              }
              Text(
                text = "Vad har hänt?",
                style = MaterialTheme.typography.displaySmall.copy(
                  fontFamily = FontFamily(
                    Font(R.font.hedvig_letters_small),
                  ),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                  .padding(22.dp)
                  .fillMaxWidth(),
              )

              Spacer(modifier = Modifier.padding(top = 28.dp))

            }
            Column(
              modifier = Modifier
                .align(Alignment.Center),
            ) {

              FlowRow(
                modifier = Modifier
                  .padding(horizontal = 16.dp),
              ) {
                uiState.searchableClaims.map {
                  Text(
                    text = it.displayName,
                    modifier = Modifier
                      .padding(4.dp)
                      .clip(SquircleShape)
                      .background(
                        shape = RoundedCornerShape(corner = CornerSize(12.dp)),
                        color = if (uiState.selectedClaim == it) {
                          Color(0xFFE9FFC8)
                        } else {
                          Color(0xFFF0F0F0)
                        },
                      )
                      .clickable { viewModel.onSelectSearchableClaim(it) }
                      .padding(8.dp),
                    textAlign = TextAlign.Center,
                  )
                }
              }
              Spacer(modifier = Modifier.padding(top = 8.dp))
              LargeContainedButton(
                onClick = {
                  uiState.selectedClaim?.let {
                    viewModel.resetState()
                    startActivity(ClaimFlowActivity.newInstance(this@ClaimGroupActivity, it.entryPointId))
                  }
                },
                modifier = Modifier.padding(horizontal = 16.dp),
              ) {
                Text(text = stringResource(id = R.string.general_continue_button))
              }
            }

            if (uiState.isLoading) {
              CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
