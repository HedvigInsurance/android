package com.hedvig.android.feature.changeaddress.ui.extrabuildings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import hedvig.resources.R

@Composable
internal fun ExtraBuildingBottomSheet(
  extraBuildingTypes: List<ExtraBuildingType>,
  extraBuilding: ExtraBuilding? = null,
  onDismiss: () -> Unit,
  onSave: (ExtraBuilding) -> Unit,
  sheetState: SheetState,
) {

  var sizeInput by rememberSaveable { mutableStateOf(extraBuilding?.size?.toString()) }
  var hasWaterConnected by rememberSaveable { mutableStateOf(extraBuilding?.hasWaterConnected ?: false) }
  var selectedType by rememberSaveable { mutableStateOf(extraBuilding?.type) }
  var sizeErrorText by rememberSaveable { mutableStateOf<Int?>(null) }

  ModalBottomSheet(
    containerColor = MaterialTheme.colorScheme.background,
    onDismissRequest = {
      onDismiss()
    },
    shape = MaterialTheme.shapes.squircleLargeTop,
    sheetState = sheetState,
    tonalElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
      Text(
        text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_BOTTOM_SHEET_TITLE),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(24.dp))
      ExtraBuildingTypeContainer(
        types = extraBuildingTypes,
        selectedType = selectedType,
        onSelected = { selectedType = it },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextField(
        value = sizeInput ?: "",
        label = {
          Text(stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDING_SIZE_LABEL))
        },
        onValueChange = {
          sizeErrorText = null
          sizeInput = it
        },
        errorText = sizeErrorText?.let { stringResource(id = it) },
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .clickable { hasWaterConnected = !hasWaterConnected },
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 16.dp),
        ) {
          Text(text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_INPUT_LABEL))
          Spacer(modifier = Modifier.weight(1f))
          Switch(
            checked = hasWaterConnected,
            onCheckedChange = { hasWaterConnected = it },
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(id = R.string.general_save_button),
        onClick = {
          if (isInputValid(sizeInput, selectedType)) {
            val extraBuilding = ExtraBuilding(
              size = sizeInput?.toInt() ?: 0,
              type = selectedType ?: ExtraBuildingType.CARPORT,
              hasWaterConnected = hasWaterConnected,
            )
            onSave(extraBuilding)
          } else {
            if (sizeInput == null) {
              sizeErrorText = R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
            }
          }
        },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(id = R.string.general_cancel_button),
        onClick = { onDismiss() },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

private fun isInputValid(sizeInput: String?, selectedType: ExtraBuildingType?): Boolean {
  return sizeInput != null && selectedType != null
}

@HedvigPreview
@Composable
internal fun PreviewExtraBuildingBottomSheet() {
  HedvigTheme {
    Surface {
      ExtraBuildingBottomSheet(
        extraBuildingTypes = listOf(ExtraBuildingType.BARN, ExtraBuildingType.CARPORT),
        onDismiss = {},
        onSave = {},
        sheetState = rememberModalBottomSheetState(),
      )
    }
  }
}
