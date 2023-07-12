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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import hedvig.resources.R

@Composable
fun YourInfoTab(
  modifier: Modifier = Modifier,
  coverageRowItems: List<Pair<String, String>>,
  onEditInfoClick: () -> Unit,
  onCancelInsuranceClick: () -> Unit,
) {
  Column(modifier) {
    CoverageRows(coverageRowItems)
    Spacer(modifier = Modifier.height(32.dp))
    LargeContainedButton(
      onClick = onEditInfoClick,
      shape = MaterialTheme.shapes.squircle,
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
      ),
    ) {
      Text(text = stringResource(id = R.string.CONTRACT_EDIT_INFO_LABEL))
    }
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = R.string.TERMINATION_BUTTON),
      onClick = onCancelInsuranceClick,
      colors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.error,
      ),
    )
  }
}

@Composable
private fun CoverageRows(coverageRowItems: List<Pair<String, String>>) {
  coverageRowItems.forEachIndexed { index, (firstText, secondText) ->
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Text(firstText)
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
        ) {
          CompositionLocalProvider(LocalContentColor.provides(MaterialTheme.colorScheme.onSurfaceVariant)) {
            Text(secondText)
          }
        }
      },
    )
    if (index != coverageRowItems.lastIndex) {
      Spacer(Modifier.height(16.dp))
      Divider()
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
@HedvigPreview
fun PreviewYourInfoTab() {
  HedvigTheme(useNewColorScheme = true) {
    Surface {
      YourInfoTab(
        modifier = Modifier.padding(16.dp),
        coverageRowItems = listOf(
          "Address" to "Bellmansgatan 19A",
          "Postal code" to "118 47",
          "Type" to "Homeowner",
          "Size" to "56 m2",
          "Co-insured" to "You +1",
        ),
        onEditInfoClick = {},
        onCancelInsuranceClick = {},
      )
    }
  }
}


