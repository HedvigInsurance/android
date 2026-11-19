package com.hedvig.android.feature.greencard.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigSearchablePickerField
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.feature.greencard.data.Country
import java.text.Collator

/**
 * A read-only field showing the chosen country. Tapping it opens a searchable list of every [Country].
 */
@Composable
internal fun CountryField(
  selectedCountry: Country?,
  onCountrySelected: (Country) -> Unit,
  errorText: String?,
  modifier: Modifier = Modifier,
) {
  val locale = getLocale()
  val languageCode = locale.language
  val sortedCountries = remember(languageCode, locale) {
    val collator = Collator.getInstance(locale)
    // Most members live in Sweden, so it comes first.
    val alphabetical = Country.entries.filter { it != Country.SWEDEN }
      .sortedWith(compareBy(collator) { it.displayName(languageCode) })
    listOf(Country.SWEDEN) + alphabetical
  }
  HedvigSearchablePickerField(
    items = sortedCountries,
    selectedItem = selectedCountry,
    onItemSelected = onCountrySelected,
    itemLabel = { it.displayName(languageCode) },
    // TODO: Add "Country" / "Land" to Lokalise
    labelText = "Country",
    // TODO: Add "No countries match your search" / "Inga länder matchar din sökning" to Lokalise
    noMatchesText = "No countries match your search",
    errorText = errorText,
    // Matching either name lets a member find a country by the name they know, whichever language the app is in.
    matchesQuery = { country, query ->
      country.displayNameSv.contains(query, ignoreCase = true) ||
        country.displayNameEn.contains(query, ignoreCase = true)
    },
    modifier = modifier,
  )
}
