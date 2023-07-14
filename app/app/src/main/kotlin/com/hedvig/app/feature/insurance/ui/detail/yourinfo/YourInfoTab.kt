package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun YourInfoTab(
  coverageRowItems: ImmutableList<Pair<String, String>>,
  onEditInfoClick: () -> Unit,
  onCancelInsuranceClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    CoverageRows(coverageRowItems, Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
    HedvigContainedButton(
      text = stringResource(R.string.CONTRACT_EDIT_INFO_LABEL),
      onClick = onEditInfoClick,
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.TERMINATION_BUTTON),
      onClick = onCancelInsuranceClick,
      colors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.error,
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun CoverageRows(
  coverageRowItems: ImmutableList<Pair<String, String>>,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    coverageRowItems.forEachIndexed { index, (firstText, secondText) ->
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(firstText)
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(
              text = secondText,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.End,
            )
          }
        },
      )
      if (index != coverageRowItems.lastIndex) {
        Divider()
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewYourInfoTab() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      YourInfoTab(
        coverageRowItems = persistentListOf(
          "Address".repeat(4) to "Bellmansgatan 19A",
          "Postal code" to "118 47".repeat(6),
          "Type" to "Homeowner",
          "Size" to "56 m2",
          "Co-insured".repeat(4) to "You +1".repeat(5),
        ),
        onEditInfoClick = {},
        onCancelInsuranceClick = {},
      )
    }
  }
}
