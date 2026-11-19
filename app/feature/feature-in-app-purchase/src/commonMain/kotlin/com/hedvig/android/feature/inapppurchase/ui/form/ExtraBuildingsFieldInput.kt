package com.hedvig.android.feature.inapppurchase.ui.form

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
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseFormField
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource

/**
 * One building as sent in the EXTRA_BUILDINGS field's values.
 */
@Serializable
internal data class ExtraBuildingValue(
  val type: String,
  val area: Int,
  val hasWaterConnected: Boolean,
) {
  fun encode(): String = Json.encodeToString(serializer(), this)

  companion object {
    fun decode(value: String): ExtraBuildingValue? =
      runCatching { Json.decodeFromString(serializer(), value) }.getOrNull()
  }
}

@Composable
internal fun ExtraBuildingsFieldInput(
  field: InAppPurchaseFormField.ExtraBuildings,
  values: List<String>,
  errorMessage: String?,
  enabled: Boolean,
  onValuesChange: (List<String>) -> Unit,
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
        buildingTypes = field.buildingTypes,
        onSave = { building ->
          onValuesChange(values + building.encode())
          dialogOpen = false
        },
        dismissDialog = { dialogOpen = false },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
  val buildings = values.mapNotNull { value -> ExtraBuildingValue.decode(value)?.let { value to it } }
  HedvigCard(modifier.fillMaxWidth()) {
    Column(Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp)) {
      HedvigText(
        text = field.title,
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      if (buildings.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 12.dp)) {
          for ((index, entry) in buildings.withIndex()) {
            val (value, building) = entry
            if (index != 0) HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
              Column(Modifier.weight(1f)) {
                HedvigText(field.buildingTypes.firstOrNull { it.value == building.type }?.title ?: building.type)
                HedvigText(
                  text = buildString {
                    append(building.area)
                    // TODO: Add " m²" area suffix to Lokalise
                    append(" m²")
                    if (building.hasWaterConnected) {
                      append(" ∙ ")
                      // TODO: Add "Water connected" / "Vattenanslutet" to Lokalise
                      append("Water connected")
                    }
                  },
                  color = HedvigTheme.colorScheme.textSecondary,
                  style = HedvigTheme.typography.label,
                )
              }
              IconButton(onClick = { onValuesChange(values - value) }, enabled = enabled) {
                // TODO: Add "Remove" / "Ta bort" to Lokalise
                Icon(HedvigIcons.Close, "Remove", Modifier.size(16.dp))
              }
            }
          }
        }
      } else {
        Spacer(Modifier.height(8.dp))
      }
      if (errorMessage != null) {
        HedvigText(errorMessage, color = HedvigTheme.colorScheme.signalRedText, style = HedvigTheme.typography.label)
        Spacer(Modifier.height(8.dp))
      }
      HedvigButton(
        // TODO: Add "Add extra building" / "Lägg till extra byggnad" to Lokalise
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
  buildingTypes: List<InAppPurchaseFormField.SingleSelect.Option>,
  onSave: (ExtraBuildingValue) -> Unit,
  dismissDialog: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var chosenType: String? by remember { mutableStateOf(null) }
  var area: Int? by remember { mutableStateOf(null) }
  var hasWaterConnected by remember { mutableStateOf(false) }
  var isAreaMissing by remember { mutableStateOf(false) }
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      // TODO: Add "Add extra building" / "Lägg till extra byggnad" to Lokalise
      text = "Add extra building",
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentWidth(Alignment.CenterHorizontally)
        .semantics { heading() },
    )
    Spacer(Modifier.height(8.dp))
    Column(Modifier.verticalScroll(rememberScrollState())) {
      Spacer(Modifier.height(8.dp))
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigCard {
          RadioGroup(
            options = buildingTypes.map { RadioOption(RadioOptionId(it.value), it.title) },
            selectedOption = chosenType?.let { RadioOptionId(it) },
            onRadioOptionSelected = { chosenType = it.id },
            // TODO: Add "Type of building" / "Typ av byggnad" to Lokalise
            style = RadioGroupStyle.Labeled.VerticalWithDivider("Type of building"),
          )
        }
        HedvigTextField(
          text = area?.toString() ?: "",
          onValueChange = {
            isAreaMissing = false
            area = it.toIntOrNull()
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          // TODO: Add "Size (m²)" / "Yta (m²)" to Lokalise
          labelText = "Size (m²)",
          textFieldSize = TextFieldSize.Medium,
          errorState = if (isAreaMissing) WithoutMessage else NoError,
        )
        HedvigToggle(
          // TODO: Add "Water connected" / "Vattenanslutet" to Lokalise
          labelText = "Water connected",
          turnedOn = hasWaterConnected,
          onClick = { hasWaterConnected = it },
          enabled = true,
          toggleStyle = ToggleStyle.Default(ToggleDefaultStyleSize.Medium),
        )
      }
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        // TODO: Add "Save" / "Spara" to Lokalise
        text = "Save",
        onClick = {
          val chosenArea = area?.takeIf { it > 0 }
          if (chosenArea == null) {
            isAreaMissing = true
            return@HedvigButton
          }
          onSave(ExtraBuildingValue(type = chosenType ?: return@HedvigButton, area = chosenArea, hasWaterConnected))
        },
        enabled = chosenType != null,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.general_cancel_button),
        onClick = dismissDialog,
        enabled = true,
        buttonSize = ButtonSize.Large,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}
