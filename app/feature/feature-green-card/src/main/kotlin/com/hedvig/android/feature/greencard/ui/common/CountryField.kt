package com.hedvig.android.feature.greencard.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.BottomSheetStyle
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize.Medium
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.SearchField
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.clearFocusOnControlTap
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
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
  val sheetState = rememberHedvigBottomSheetState<Unit>()
  val focusManager = LocalFocusManager.current
  val languageCode = getLocale().language
  // TODO: Add "Country" / "Land" to Lokalise
  val label = "Country"
  HedvigBottomSheet(
    hedvigBottomSheetState = sheetState,
    modifier = Modifier.fillMaxHeight(),
    style = BottomSheetStyle(transparentBackground = false, automaticallyScrollableContent = false, scrimColor = null),
  ) {
    CountryList(
      title = label,
      languageCode = languageCode,
      selectedCountry = selectedCountry,
      onCountrySelected = {
        onCountrySelected(it)
        sheetState.dismiss()
      },
    )
  }
  val openPicker = {
    focusManager.clearFocus()
    sheetState.show()
  }
  Box(modifier.fillMaxWidth()) {
    HedvigTextField(
      text = selectedCountry?.displayName(languageCode).orEmpty(),
      onValueChange = {},
      readOnly = true,
      labelText = label,
      textFieldSize = Medium,
      errorState = if (errorText != null) ErrorState.Error.WithMessage(errorText) else ErrorState.NoError,
      trailingContent = { Icon(HedvigIcons.ChevronDown, null, Modifier.size(24.dp)) },
      modifier = Modifier.fillMaxWidth(),
    )
    Box(
      Modifier
        .matchParentSize()
        .clip(HedvigTheme.shapes.cornerLarge)
        .clearFocusOnControlTap()
        .clickable(role = Role.DropdownList, onClick = openPicker),
    )
  }
}

@Composable
private fun CountryList(
  title: String,
  languageCode: String,
  selectedCountry: Country?,
  onCountrySelected: (Country) -> Unit,
) {
  var query by rememberSaveable { mutableStateOf<String?>(null) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val locale = getLocale()
  val sortedCountries = remember(languageCode, locale) {
    val collator = Collator.getInstance(locale)
    // Most members live in Sweden, so it comes first.
    val alphabetical = Country.entries.filter { it != Country.SWEDEN }
      .sortedWith(compareBy(collator) { it.displayName(languageCode) })
    listOf(Country.SWEDEN) + alphabetical
  }
  val matchingCountries = remember(sortedCountries, query) {
    val trimmedQuery = query?.trim().orEmpty()
    if (trimmedQuery.isEmpty()) {
      sortedCountries
    } else {
      // Matching either name lets a member find a country by the name they know, whichever language the app is in.
      sortedCountries.filter {
        it.displayNameSv.contains(trimmedQuery, ignoreCase = true) ||
          it.displayNameEn.contains(trimmedQuery, ignoreCase = true)
      }
    }
  }
  Column(Modifier.fillMaxHeight()) {
    HedvigText(title, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(16.dp))
    SearchField(
      searchQuery = query,
      focusRequester = focusRequester,
      onClearSearch = { query = null },
      onKeyboardAction = { keyboardController?.hide() },
      onSearchChange = { query = it.ifEmpty { null } },
    )
    Spacer(Modifier.height(8.dp))
    if (matchingCountries.isEmpty()) {
      // TODO: Add "No countries match your search" / "Inga länder matchar din sökning" to Lokalise
      HedvigText(
        text = "No countries match your search",
        color = HedvigTheme.colorScheme.textSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 32.dp),
      )
    } else {
      val listState = rememberLazyListState()
      LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) keyboardController?.hide()
      }
      LazyColumn(state = listState, modifier = Modifier.weight(1f)) {
        items(matchingCountries, key = { it.name }) { country ->
          CountryRow(
            name = country.displayName(languageCode),
            isSelected = country == selectedCountry,
            onClick = { onCountrySelected(country) },
          )
        }
      }
    }
  }
}

@Composable
private fun CountryRow(name: String, isSelected: Boolean, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(HedvigTheme.shapes.cornerMedium)
      .clickable(role = Role.Button, onClick = onClick)
      .semantics { selected = isSelected }
      .padding(vertical = 14.dp, horizontal = 4.dp),
  ) {
    HedvigText(name, modifier = Modifier.weight(1f))
    if (isSelected) {
      Icon(HedvigIcons.Checkmark, null, Modifier.size(24.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCountryList() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(Modifier.padding(16.dp)) {
        CountryList(
          title = "Land",
          languageCode = "sv",
          selectedCountry = Country.SWEDEN,
          onCountrySelected = {},
        )
      }
    }
  }
}
