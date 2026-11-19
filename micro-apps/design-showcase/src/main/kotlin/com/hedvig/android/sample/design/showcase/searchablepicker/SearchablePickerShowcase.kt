package com.hedvig.android.sample.design.showcase.searchablepicker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigSearchablePickerField
import com.hedvig.android.sample.design.showcase.util.ShowcaseLayout

@Composable
internal fun SearchablePickerShowcase() {
  ShowcaseLayout {
    var selected by remember { mutableStateOf<String?>("Sweden") }
    var selectedWithError by remember { mutableStateOf<String?>(null) }
    Column(Modifier.width(360.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      HedvigSearchablePickerField(
        items = sampleCountries,
        selectedItem = selected,
        onItemSelected = { selected = it },
        itemLabel = { it },
        labelText = "Country",
        noMatchesText = "No countries match your search",
      )
      HedvigSearchablePickerField(
        items = sampleCountries,
        selectedItem = selectedWithError,
        onItemSelected = { selectedWithError = it },
        itemLabel = { it },
        labelText = "Country",
        noMatchesText = "No countries match your search",
        errorText = if (selectedWithError == null) "Fill in this field" else null,
      )
    }
  }
}

private val sampleCountries = listOf(
  "Sweden",
  "Austria",
  "Belgium",
  "Croatia",
  "Czechia",
  "Denmark",
  "Estonia",
  "Finland",
  "France",
  "Germany",
  "Greece",
  "Hungary",
  "Iceland",
  "Ireland",
  "Italy",
  "Latvia",
  "Lithuania",
  "Luxembourg",
  "Netherlands",
  "Norway",
  "Poland",
  "Portugal",
  "Romania",
  "Slovakia",
  "Slovenia",
  "Spain",
  "Switzerland",
  "United Kingdom",
)
