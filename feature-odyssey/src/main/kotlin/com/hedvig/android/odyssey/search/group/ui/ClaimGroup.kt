package com.hedvig.android.odyssey.search.group.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.odyssey.search.group.ClaimGroupUiState
import com.hedvig.android.odyssey.search.group.ClaimGroupViewModel
import hedvig.resources.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ClaimGroupSelector(
  uiState: ClaimGroupUiState,
  viewModel: ClaimGroupViewModel,
  onContinueClicked: (String) -> Unit,
) {
  Box(modifier = Modifier.fillMaxHeight()) {
    Column {
      Spacer(modifier = Modifier.padding(top = 98.dp))

      Text(
        text = stringResource(id = R.string.CLAIM_TRIAGING_TITLE),
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
    Box(modifier = Modifier.align(Alignment.Center)) {
      Column {
        FlowRow(modifier = Modifier.padding(horizontal = 16.dp)) {
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
              onContinueClicked(it.entryPointId)
            }
          },
          modifier = Modifier.padding(horizontal = 16.dp),
        ) {
          Text(text = stringResource(id = R.string.general_continue_button))
        }
      }
    }
  }
}
