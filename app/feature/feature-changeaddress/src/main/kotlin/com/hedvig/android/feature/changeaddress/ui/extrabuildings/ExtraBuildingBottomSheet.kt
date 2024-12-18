package com.hedvig.android.feature.changeaddress.ui.extrabuildings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType.CARPORT
import com.hedvig.android.feature.changeaddress.ui.ChangeAddressSwitch
import com.hedvig.android.feature.changeaddress.ui.InputTextField
import hedvig.resources.R

@Composable
internal fun ExtraBuildingBottomSheet(
  extraBuildingTypes: List<ExtraBuildingType>,
  extraBuilding: ExtraBuilding? = null,
  onDismiss: () -> Unit,
  onSave: (ExtraBuilding) -> Unit,
  isVisible: Boolean,
  onVisibleChange: (Boolean) -> Unit,
) {
  var sizeInput by rememberSaveable { mutableStateOf(extraBuilding?.size?.toString()) }
  var hasWaterConnected by rememberSaveable { mutableStateOf(extraBuilding?.hasWaterConnected ?: false) }
  var selectedType by rememberSaveable { mutableStateOf(extraBuilding?.type) }
  var sizeErrorText by rememberSaveable { mutableStateOf<Int?>(null) }
  HedvigBottomSheet(
    isVisible = isVisible,
    onVisibleChange = onVisibleChange,
  ) {
    Column(
      modifier = Modifier.clearFocusOnTap(),
    ) {
      Text(
        text = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_BOTTOM_SHEET_TITLE),
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(24.dp))

      ExtraBuildingTypeContainer(
        types = extraBuildingTypes,
        selectedType = selectedType,
        onSelected = { selectedType = it },
      )
      Spacer(Modifier.height(8.dp))
      InputTextField(
        value = sizeInput,
        errorMessageRes = sizeErrorText,
        label = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDING_SIZE_LABEL),
        onValueChange = {
          sizeErrorText = null
          sizeInput = it
        },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
        ),
        modifier = Modifier
          .fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      ChangeAddressSwitch(
        label = stringResource(id = R.string.CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_INPUT_LABEL),
        checked = hasWaterConnected,
        onCheckedChange = { hasWaterConnected = it },
        onClick = { hasWaterConnected = !hasWaterConnected },
      )
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(id = R.string.general_save_button),
        onClick = {
          if (isInputValid(sizeInput, selectedType)) {
            val newExtraBuilding = ExtraBuilding(
              size = sizeInput?.toIntOrNull() ?: 0,
              type = selectedType ?: CARPORT,
              hasWaterConnected = hasWaterConnected,
            )
            onSave(newExtraBuilding)
          } else {
            if (sizeInput == null) {
              sizeErrorText = R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
            }
          }
        },
      )
      HedvigButton(
        onClick = onDismiss,
        text = stringResource(R.string.general_cancel_button),
        enabled = true,
        buttonStyle = Ghost,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

private fun isInputValid(sizeInput: String?, selectedType: ExtraBuildingType?): Boolean {
  return sizeInput != null && selectedType != null
}

@HedvigPreview
@Composable
private fun PreviewExtraBuildingBottomSheet() {
  HedvigTheme {
    Surface {
      ExtraBuildingBottomSheet(
        extraBuildingTypes = listOf(ExtraBuildingType.BARN, CARPORT),
        onDismiss = {},
        onSave = {},
        isVisible = true,
        onVisibleChange = {},
      )
    }
  }
}
