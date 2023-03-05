package com.hedvig.app.feature.addressautocompletion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import com.hedvig.app.ui.compose.composables.appbar.CenterAlignedTopAppBar
import com.hedvig.app.util.compose.preview.previewData
import com.hedvig.app.util.compose.preview.previewList
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddressAutoCompleteScreen(
  viewState: AddressAutoCompleteViewState,
  setNewTextInput: (String) -> Unit,
  selectAddress: (DanishAddress) -> Unit,
  cancelAutoCompletion: () -> Unit,
  cantFindAddress: () -> Unit,
) {
  val focusRequester = remember { FocusRequester() }
  val keyboardController = LocalSoftwareKeyboardController.current
  LaunchedEffect(Unit) {
    delay(100.milliseconds) // A delay of 100 milliseconds ensures that the keyboard shows reliably.
    focusRequester.requestFocus()
    keyboardController?.show()
  }
  val closeKeyboard: () -> Unit = {
    keyboardController?.hide()
    focusRequester.freeFocus()
  }
  Surface {
    Column {
      TopAppBar(
        viewState = viewState,
        cancelAutoCompletion = cancelAutoCompletion,
        setNewTextInput = setNewTextInput,
        focusRequester = focusRequester,
        closeKeyboard = closeKeyboard,
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
          .asPaddingValues(),
      )
      SuggestionsList(
        viewState = viewState,
        selectAddress = { address ->
          closeKeyboard()
          selectAddress(address)
        },
        cantFindAddress = cantFindAddress,
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal.plus(WindowInsetsSides.Bottom))
          .asPaddingValues(),
        modifier = Modifier.weight(1f),
      )
    }
  }
}

@Composable
private fun TopAppBar(
  viewState: AddressAutoCompleteViewState,
  cancelAutoCompletion: () -> Unit,
  setNewTextInput: (String) -> Unit,
  focusRequester: FocusRequester,
  closeKeyboard: () -> Unit,
  contentPadding: PaddingValues,
) {
  Surface(color = MaterialTheme.colors.background) {
    Column(Modifier.padding(contentPadding)) {
      CenterAlignedTopAppBar(
        title = stringResource(hedvig.resources.R.string.EMBARK_ADDRESS_AUTOCOMPLETE_ADDRESS),
        onClick = { cancelAutoCompletion() },
        backgroundColor = MaterialTheme.colors.background,
      )
      AddressInput(
        viewState = viewState,
        setNewTextInput = setNewTextInput,
        focusRequester = focusRequester,
        closeKeyboard = closeKeyboard,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }
  }
}

@Composable
private fun AddressInput(
  viewState: AddressAutoCompleteViewState,
  setNewTextInput: (String) -> Unit,
  focusRequester: FocusRequester,
  closeKeyboard: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    border = BorderStroke(1.dp, MaterialTheme.colors.primary.copy(alpha = 0.12f)),
    modifier = modifier,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
      var textFieldValue by remember {
        val text = viewState.input.rawText
        mutableStateOf(
          TextFieldValue(
            text = text,
            selection = TextRange(text.length),
          ),
        )
      }
      LaunchedEffect(viewState.input.selectedAddress) {
        if (viewState.input.selectedAddress == null) return@LaunchedEffect
        textFieldValue = textFieldValue.copy(
          text = viewState.input.rawText,
          selection = TextRange(viewState.input.rawText.length),
        )
      }
      BasicTextField(
        value = textFieldValue,
        onValueChange = { newTextFieldValue ->
          // TextFieldValue changes on more occasions than just text change. Do this to emulate the
          //  functionality of the onValueChange of the BasicTextField that takes a normal String
          if (textFieldValue.text != newTextFieldValue.text) {
            setNewTextInput(newTextFieldValue.text)
          }
          textFieldValue = newTextFieldValue
        },
        textStyle = LocalTextStyle.current.copy(
          textAlign = TextAlign.Center,
          color = LocalContentColor.current.copy(LocalContentAlpha.current),
        ),
        keyboardActions = KeyboardActions(
          onDone = { closeKeyboard() },
        ),
        singleLine = true,
        cursorBrush = SolidColor(LocalContentColor.current),
        modifier = Modifier
          .focusRequester(focusRequester)
          .fillMaxWidth(),
      )
      val numberAndCity = viewState.input.selectedAddress?.toPresentableTextPair()?.second
      AnimatedVisibility(numberAndCity != null) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
          numberAndCity?.let {
            Text(it)
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SuggestionsList(
  viewState: AddressAutoCompleteViewState,
  selectAddress: (DanishAddress) -> Unit,
  cantFindAddress: () -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  LaunchedEffect(viewState.results) {
    if (viewState.results.isNotEmpty()) {
      lazyListState.animateScrollToItem(0)
    }
  }
  LazyColumn(
    modifier = modifier,
    state = lazyListState,
    contentPadding = contentPadding,
  ) {
    items(
      items = viewState.results,
      key = { item -> item.id ?: item.address },
      contentType = { "DanishAddressEntry" },
    ) { address: DanishAddress ->
      val (primaryText, secondaryText) = address.toPresentableTextPair()
      ListItem(
        text = { Text(text = primaryText) },
        secondaryText = secondaryText?.let { { Text(secondaryText) } },
        singleLineSecondaryText = true,
        modifier = Modifier.clickable {
          selectAddress(address)
        },
      )
    }
    if (viewState.showCantFindAddressItem) {
      item(key = "cantFindAddress") {
        ListItem(
          text = {
            Text(
              stringResource(hedvig.resources.R.string.EMBARK_ADDRESS_AUTOCOMPLETE_NO_ADDRESS),
              color = MaterialTheme.colors.error,
            )
          },
          modifier = Modifier.clickable { cantFindAddress() },
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewAddressAutoCompleteScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      val previewDanishAddress = DanishAddress.previewData()
      AddressAutoCompleteScreen(
        AddressAutoCompleteViewState(
          input = DanishAddressInput.fromDanishAddress(previewDanishAddress),
          results = DanishAddress.previewList(),
        ),
        {},
        {},
        {},
        {},
      )
    }
  }
}
