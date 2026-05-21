package com.hedvig.android.feature.purchase.pet.ui.picker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.purchase.pet.data.PRODUCT_NAME_CAT
import com.hedvig.android.feature.purchase.pet.data.PRODUCT_NAME_DOG

@Composable
internal fun PetSpeciesPickerDestination(
  navigateUp: () -> Unit,
  onSpeciesSelected: (productName: String) -> Unit,
) {
  HedvigScaffold(navigateUp = navigateUp) {
    PetSpeciesPickerContent(onSpeciesSelected = onSpeciesSelected)
  }
}

@Composable
private fun PetSpeciesPickerContent(onSpeciesSelected: (productName: String) -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Spacer(Modifier.height(16.dp))
    HedvigText(
      // TODO: Add "What kind of pet?" / "Vilken sorts husdjur?" to Lokalise
      text = "What kind of pet?",
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    HedvigText(
      // TODO: Add "Choose your pet to get started" / "Välj ditt husdjur för att komma igång" to Lokalise
      text = "Choose your pet to get started",
      style = HedvigTheme.typography.bodyMedium,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      // TODO: Add "Dog" / "Hund" to Lokalise
      text = "Dog",
      onClick = { onSpeciesSelected(PRODUCT_NAME_DOG) },
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
    HedvigButton(
      // TODO: Add "Cat" / "Katt" to Lokalise
      text = "Cat",
      onClick = { onSpeciesSelected(PRODUCT_NAME_CAT) },
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewPetSpeciesPicker() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PetSpeciesPickerContent(onSpeciesSelected = {})
    }
  }
}
