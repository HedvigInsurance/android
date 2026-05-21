package com.hedvig.android.feature.insurances.insurance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@Composable
internal fun HomePickerDialog(
  onDismiss: () -> Unit,
  onSelectApartmentRent: () -> Unit,
  onSelectApartmentBrf: () -> Unit,
  onSelectVilla: () -> Unit,
) {
  HedvigDialog(onDismissRequest = onDismiss) {
    Column(modifier = Modifier.fillMaxWidth()) {
      HedvigText(
        // TODO: Add "Which type of home insurance?" / "Vilken typ av hemförsäkring?" to Lokalise
        text = "Which type of home insurance?",
        style = HedvigTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
      )
      // TODO: Add "Rental apartment" / "Hyresrätt" to Lokalise
      HomePickerRow(text = "Rental apartment (Hyresrätt)", onClick = onSelectApartmentRent)
      // TODO: Add "Owned apartment" / "Bostadsrätt" to Lokalise
      HomePickerRow(text = "Owned apartment (Bostadsrätt)", onClick = onSelectApartmentBrf)
      // TODO: Add "House" / "Villa" to Lokalise
      HomePickerRow(text = "House (Villa)", onClick = onSelectVilla)
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        // TODO: Add "Cancel" / "Avbryt" to Lokalise
        text = "Cancel",
        onClick = onDismiss,
        enabled = true,
        buttonStyle = Ghost,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun HomePickerRow(text: String, onClick: () -> Unit) {
  HedvigText(
    text = text,
    style = HedvigTheme.typography.bodyMedium,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(role = Role.Button) { onClick() }
      .padding(horizontal = 16.dp, vertical = 14.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewHomePickerDialog() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HomePickerDialog(
        onDismiss = {},
        onSelectApartmentRent = {},
        onSelectApartmentBrf = {},
        onSelectVilla = {},
      )
    }
  }
}
