package com.hedvig.android.feature.insurances.insurancedetail.yourinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import hedvig.resources.R

@Composable
internal fun UpcomingChangesBottomSheetContent(
  infoText: String,
  sections: List<Pair<String, String>>,
  onOpenChat: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
  ) {
    Text(
      text = stringResource(id = R.string.insurance_details_update_details_sheet_title),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    )
    Spacer(modifier = Modifier.height(32.dp))
    CoverageRows(coverageRowItems = sections)
    Spacer(modifier = Modifier.height(16.dp))
    VectorInfoCard(
      text = infoText,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(id = R.string.open_chat),
      onClick = onOpenChat,
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      onClick = onDismiss,
    )
    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
@HedvigPreview
private fun PreviewUpcomingChangesBottomSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      UpcomingChangesBottomSheetContent(
        infoText = "Test",
        sections = listOf(
          "1" to "2",
        ),
        onDismiss = {},
        onOpenChat = {},
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}
