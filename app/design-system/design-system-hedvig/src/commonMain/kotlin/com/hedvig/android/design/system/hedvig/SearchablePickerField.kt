package com.hedvig.android.design.system.hedvig

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
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.ErrorState
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.ChevronDown
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

/**
 * A read-only field showing the chosen item. Tapping it opens a full-height bottom sheet with a search field and
 * the [items], in the order given.
 *
 * [matchesQuery] receives the trimmed, non-empty query. The default matches [itemLabel] ignoring case; pass your own
 * when an item should also be found by other names.
 */
@Composable
fun <T> HedvigSearchablePickerField(
  items: List<T>,
  selectedItem: T?,
  onItemSelected: (T) -> Unit,
  itemLabel: (T) -> String,
  labelText: String,
  noMatchesText: String,
  modifier: Modifier = Modifier,
  errorText: String? = null,
  textFieldSize: TextFieldSize = TextFieldSize.Medium,
  matchesQuery: (item: T, query: String) -> Boolean = { item, query -> itemLabel(item).contains(query, true) },
) {
  val sheetState = rememberHedvigBottomSheetState<Unit>()
  val focusManager = LocalFocusManager.current
  HedvigBottomSheet(
    hedvigBottomSheetState = sheetState,
    modifier = Modifier.fillMaxHeight(),
    style = BottomSheetStyle(transparentBackground = false, automaticallyScrollableContent = false, scrimColor = null),
  ) {
    SearchablePickerList(
      title = labelText,
      items = items,
      selectedItem = selectedItem,
      itemLabel = itemLabel,
      matchesQuery = matchesQuery,
      noMatchesText = noMatchesText,
      onItemSelected = {
        onItemSelected(it)
        sheetState.dismiss()
      },
    )
  }
  Box(modifier.fillMaxWidth()) {
    HedvigTextField(
      text = selectedItem?.let(itemLabel).orEmpty(),
      onValueChange = {},
      readOnly = true,
      labelText = labelText,
      textFieldSize = textFieldSize,
      errorState = if (errorText != null) ErrorState.Error.WithMessage(errorText) else ErrorState.NoError,
      trailingContent = { Icon(HedvigIcons.ChevronDown, null, Modifier.size(24.dp)) },
      modifier = Modifier.fillMaxWidth(),
    )
    // Covers the read-only field so a tap opens the sheet instead of focusing the text.
    Box(
      Modifier
        .matchParentSize()
        .clip(HedvigTheme.shapes.cornerLarge)
        .clearFocusOnControlTap()
        .clickable(role = Role.DropdownList) {
          focusManager.clearFocus()
          sheetState.show()
        },
    )
  }
}

@Composable
private fun <T> SearchablePickerList(
  title: String,
  items: List<T>,
  selectedItem: T?,
  itemLabel: (T) -> String,
  matchesQuery: (item: T, query: String) -> Boolean,
  noMatchesText: String,
  onItemSelected: (T) -> Unit,
) {
  var query by rememberSaveable { mutableStateOf<String?>(null) }
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  val trimmedQuery = query?.trim().orEmpty()
  val matchingItems = if (trimmedQuery.isEmpty()) items else items.filter { matchesQuery(it, trimmedQuery) }
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
    if (matchingItems.isEmpty()) {
      HedvigText(
        text = noMatchesText,
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
        items(matchingItems) { item ->
          SearchablePickerRow(
            label = itemLabel(item),
            isSelected = item == selectedItem,
            onClick = { onItemSelected(item) },
          )
        }
      }
    }
  }
}

@Composable
private fun SearchablePickerRow(label: String, isSelected: Boolean, onClick: () -> Unit) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clip(HedvigTheme.shapes.cornerMedium)
      .clickable(role = Role.Button, onClick = onClick)
      .semantics { selected = isSelected }
      .padding(vertical = 14.dp, horizontal = 4.dp),
  ) {
    HedvigText(label, modifier = Modifier.weight(1f))
    if (isSelected) {
      Icon(HedvigIcons.Checkmark, null, Modifier.size(24.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigSearchablePickerField() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(Modifier.padding(16.dp)) {
        HedvigSearchablePickerField(
          items = listOf("Sweden", "Denmark", "Norway"),
          selectedItem = "Sweden",
          onItemSelected = {},
          itemLabel = { it },
          labelText = "Country",
          noMatchesText = "No countries match your search",
        )
        Spacer(Modifier.height(4.dp))
        HedvigSearchablePickerField(
          items = listOf("Sweden", "Denmark", "Norway"),
          selectedItem = null,
          onItemSelected = {},
          itemLabel = { it },
          labelText = "Country",
          noMatchesText = "No countries match your search",
          errorText = "Fill in this field",
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewSearchablePickerList() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(Modifier.padding(16.dp)) {
        SearchablePickerList(
          title = "Country",
          items = listOf("Sweden", "Denmark", "Norway", "Finland"),
          selectedItem = "Sweden",
          itemLabel = { it },
          matchesQuery = { item, query -> item.contains(query, true) },
          noMatchesText = "No countries match your search",
          onItemSelected = {},
        )
      }
    }
  }
}
