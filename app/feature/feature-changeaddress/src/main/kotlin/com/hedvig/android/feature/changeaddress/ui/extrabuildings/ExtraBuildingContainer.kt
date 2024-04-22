package com.hedvig.android.feature.changeaddress.ui.extrabuildings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.lightTypeContainer
import com.hedvig.android.core.designsystem.material3.onLightTypeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.stringRes
import hedvig.resources.R

@Composable
internal fun ExtraBuildingContainer(
  extraBuildings: List<ExtraBuilding>,
  onAddExtraBuildingClicked: () -> Unit,
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
      Column {
        extraBuildings.forEach { extraBuilding: ExtraBuilding ->
          val visibleState = remember {
            MutableTransitionState(false).apply {
              targetState = true
            }
          }
          AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically),
            label = "extraBuilding",
          ) {
            Row(
              modifier = Modifier
//                .clickable { onExtraBuildingItemClicked(extraBuilding) } // TODO
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
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  style = MaterialTheme.typography.bodyMedium,
                )
                if (extraBuilding.hasWaterConnected) {
                  Text(
                    text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_LABEL),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                  )
                }
              }
              Spacer(modifier = Modifier.width(16.dp))
              Spacer(modifier = Modifier.weight(1f))
              IconButton(onClick = { onRemoveExtraBuildingClicked(extraBuilding) }) {
                Icon(
                  imageVector = Icons.Hedvig.X,
                  contentDescription = null,
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
          containerColor = MaterialTheme.colorScheme.lightTypeContainer,
          contentColor = MaterialTheme.colorScheme.onLightTypeContainer,
        ),
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewExtraBuildingContainer() {
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
        onAddExtraBuildingClicked = {},
        onRemoveExtraBuildingClicked = {},
      )
    }
  }
}
