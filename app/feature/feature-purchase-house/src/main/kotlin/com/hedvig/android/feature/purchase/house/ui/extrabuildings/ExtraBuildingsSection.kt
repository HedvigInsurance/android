package com.hedvig.android.feature.purchase.house.ui.extrabuildings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.PrimaryAlt
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.NoButtons
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState.Error.WithoutMessage
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState.NoError
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigToggle
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioGroupStyle
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleDefaultStyleSize
import com.hedvig.android.design.system.hedvig.ToggleDefaults.ToggleStyle
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

internal data class ExtraBuildingInfo(
  val area: Int,
  val type: String,
  val displayName: String,
  val hasWaterConnected: Boolean,
)

internal data class MoveExtraBuildingType(
  val type: String,
  val displayName: String,
)

// TODO: Replace these English strings with stringResource(...) lookups when Lokalise keys
//  exist for each building type's display name. Today the move flow gets these from the
//  backend (extraBuildingTypesV2.displayName), but PriceIntent does not expose that field.
internal val allExtraBuildingTypes: List<MoveExtraBuildingType> = listOf(
  MoveExtraBuildingType("GARAGE", "Garage"),
  MoveExtraBuildingType("CARPORT", "Carport"),
  MoveExtraBuildingType("SHED", "Shed"),
  MoveExtraBuildingType("STOREHOUSE", "Storehouse"),
  MoveExtraBuildingType("FRIGGEBOD", "Friggebod"),
  MoveExtraBuildingType("ATTEFALL", "Attefallshus"),
  MoveExtraBuildingType("OUTHOUSE", "Outhouse"),
  MoveExtraBuildingType("GUESTHOUSE", "Guesthouse"),
  MoveExtraBuildingType("GAZEBO", "Gazebo"),
  MoveExtraBuildingType("GREENHOUSE", "Greenhouse"),
  MoveExtraBuildingType("SAUNA", "Sauna"),
  MoveExtraBuildingType("BARN", "Barn"),
  MoveExtraBuildingType("BOATHOUSE", "Boathouse"),
  MoveExtraBuildingType("OTHER", "Other"),
)

@Composable
internal fun ExtraBuildingsSection(
  extraBuildings: List<ExtraBuildingInfo>,
  allowedExtraBuildings: List<MoveExtraBuildingType>,
  onAddBuilding: (ExtraBuildingInfo) -> Unit,
  onRemoveBuilding: (ExtraBuildingInfo) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  var dialogOpen by rememberSaveable { mutableStateOf(false) }
  if (dialogOpen) {
    HedvigDialog(
      contentPadding = PaddingValues(0.dp),
      dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
      onDismissRequest = { dialogOpen = false },
      style = NoButtons,
    ) {
      AddExtraBuildingDialogContent(
        allowedExtraBuildings = allowedExtraBuildings,
        onSaveBuilding = { building ->
          onAddBuilding(building)
          dialogOpen = false
        },
        dismissDialog = { dialogOpen = false },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
  HedvigCard(modifier.fillMaxWidth()) {
    Column(
      Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
    ) {
      HedvigText(
        // TODO: Add "Extra buildings" / "Extra byggnader" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_LABEL).
        text = "Extra buildings",
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      if (extraBuildings.isNotEmpty()) {
        Column(
          verticalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.padding(vertical = 12.dp),
        ) {
          for ((index, extraBuilding) in extraBuildings.withIndex()) {
            if (index != 0) {
              HorizontalDivider()
            }
            key(extraBuilding) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                  HedvigText(extraBuilding.displayName)
                  HedvigText(
                    text = buildString {
                      append(extraBuilding.area)
                      // TODO: Add area suffix " m²" to Lokalise (or reuse CHANGE_ADDRESS_SIZE_SUFFIX).
                      append(" m²")
                      if (extraBuilding.hasWaterConnected) {
                        append(" ∙ ")
                        // TODO: Add "Water connected" / "Vattenanslutet" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_LABEL).
                        append("Water connected")
                      }
                    },
                    color = HedvigTheme.colorScheme.textSecondary,
                    style = HedvigTheme.typography.label,
                  )
                }
                IconButton(
                  onClick = { onRemoveBuilding(extraBuilding) },
                  enabled = enabled,
                ) {
                  // TODO: Add "Remove" / "Ta bort" content description to Lokalise (or reuse GENERAL_REMOVE).
                  Icon(HedvigIcons.Close, "Remove", Modifier.size(16.dp))
                }
              }
            }
          }
        }
      } else {
        Spacer(Modifier.height(8.dp))
      }
      HedvigButton(
        // TODO: Add "Add extra building" / "Lägg till extra byggnad" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_BOTTOM_SHEET_TITLE).
        text = "Add extra building",
        onClick = { dialogOpen = true },
        enabled = enabled,
        buttonStyle = PrimaryAlt,
        buttonSize = ButtonSize.Medium,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun AddExtraBuildingDialogContent(
  allowedExtraBuildings: List<MoveExtraBuildingType>,
  onSaveBuilding: (ExtraBuildingInfo) -> Unit,
  dismissDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var chosenBuilding: MoveExtraBuildingType? by remember { mutableStateOf(null) }
  var size: Int? by remember { mutableStateOf(null) }
  var isConnectedToWater: Boolean by remember { mutableStateOf(false) }
  var isSizeMissing by remember { mutableStateOf(false) }
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      // TODO: Add "Add extra building" / "Lägg till extra byggnad" to Lokalise.
      text = "Add extra building",
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigCard {
          RadioGroup(
            options = allowedExtraBuildings.map { buildingType ->
              RadioOption(
                RadioOptionId(buildingType.type),
                buildingType.displayName,
              )
            },
            selectedOption = chosenBuilding?.type?.let { RadioOptionId(it) },
            onRadioOptionSelected = { selected ->
              chosenBuilding = allowedExtraBuildings.firstOrNull { it.type == selected.id }
            },
            // TODO: Add "Type of building" / "Typ av byggnad" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDING_CONTAINER_TITLE).
            style = RadioGroupStyle.Labeled.VerticalWithDivider("Type of building"),
          )
        }
        HedvigTextField(
          text = size?.toString() ?: "",
          onValueChange = {
            isSizeMissing = false
            size = it.toIntOrNull()
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          // TODO: Add "Size (m²)" / "Yta (m²)" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDING_SIZE_LABEL).
          labelText = "Size (m²)",
          textFieldSize = TextFieldSize.Medium,
          errorState = if (isSizeMissing) WithoutMessage else NoError,
        )
        HedvigToggle(
          // TODO: Add "Water connected" / "Vattenanslutet" to Lokalise (or reuse CHANGE_ADDRESS_EXTRA_BUILDINGS_WATER_INPUT_LABEL).
          labelText = "Water connected",
          turnedOn = isConnectedToWater,
          onClick = { isConnectedToWater = it },
          enabled = true,
          toggleStyle = ToggleStyle.Default(ToggleDefaultStyleSize.Medium),
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        // TODO: Add "Save" / "Spara" to Lokalise (or reuse general_save_button).
        text = "Save",
        onClick = {
          if (size == null) {
            isSizeMissing = true
          }
          val area = size ?: return@HedvigButton
          val type = chosenBuilding?.type ?: return@HedvigButton
          val displayName = chosenBuilding?.displayName ?: return@HedvigButton
          onSaveBuilding(ExtraBuildingInfo(area, type, displayName, isConnectedToWater))
        },
        enabled = chosenBuilding != null,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        // TODO: Add "Cancel" / "Avbryt" to Lokalise (or reuse general_cancel_button).
        text = "Cancel",
        onClick = dismissDialog,
        enabled = true,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}
