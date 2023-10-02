package com.hedvig.android.feature.changeaddress.ui.extrabuildings

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.stringRes
import hedvig.resources.R

@Composable
fun ExtraBuildingContainer(
  extraBuildings: List<ExtraBuilding>,
  onAddExtraBuildingClicked: () -> Unit,
  onExtraBuildingItemClicked: (ExtraBuilding) -> Unit,
  onRemoveExtraBuildingClicked: (ExtraBuilding) -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier) {
    Column {
      Text(
        text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_LABEL),
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
      )
      AnimatedContent(targetState = extraBuildings, label = "extraBuildings") {
        Column {
          it.forEach { extraBuilding ->
            Row(
              modifier = Modifier
                // .clickable { onExtraBuildingItemClicked(extraBuilding) } // TODO
                .padding(horizontal = 16.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = stringResource(id = extraBuilding.type.stringRes()),
                  style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                  text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_SIZE_LABEL, extraBuilding.size),
                  color = MaterialTheme.colorScheme.secondary,
                  style = MaterialTheme.typography.bodyMedium,
                )
                if (extraBuilding.hasWaterConnected) {
                  Text(
                    text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_LABEL),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium,
                  )
                }
              }
              Spacer(modifier = Modifier.height(16.dp))
              Spacer(modifier = Modifier.weight(1f))
              IconButton(onClick = { onRemoveExtraBuildingClicked(extraBuilding) }) {
                Image(
                  imageVector = Icons.Filled.Clear,
                  contentDescription = stringResource(
                    R.string.login_text_input_email_address_icon_description_clear_all,
                  ),
                )
              }
            }
          }
        }
      }

      HedvigContainedSmallButton(
        text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_BOTTOM_SHEET_TITLE),
        onClick = onAddExtraBuildingClicked,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.typeContainer,
          contentColor = LocalContentColor.current,
        ),
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
fun PreviewExtraBuildingContainer() {
  HedvigTheme {
    Surface {
      ExtraBuildingContainer(
        extraBuildings = listOf(
          ExtraBuilding(
            size = 30,
            type = ExtraBuildingType.ATTEFALL,
            hasWaterConnected = false,
          ),
          ExtraBuilding(
            size = 122,
            type = ExtraBuildingType.CARPORT,
            hasWaterConnected = true,
          ),
        ),
        onExtraBuildingItemClicked = {},
        onAddExtraBuildingClicked = {},
        onRemoveExtraBuildingClicked = {},
      )
    }
  }
}
