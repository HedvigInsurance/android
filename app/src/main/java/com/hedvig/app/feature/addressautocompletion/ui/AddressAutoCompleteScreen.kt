package com.hedvig.app.feature.addressautocompletion.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData
import com.hedvig.app.util.compose.preview.previewList

@Composable
fun AddressAutoCompleteScreen(
    viewState: AddressAutoCompleteViewState,
    setInput: (String) -> Unit,
    selectAddress: (DanishAddress) -> Unit,
    finishWithSelection: (DanishAddress) -> Unit,
    finishWithoutSelection: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    viewState = viewState,
                    finishWithoutSelection = finishWithoutSelection,
                    finishWithSelection = finishWithSelection,
                    contentPadding = rememberInsetsPaddingValues(
                        insets = LocalWindowInsets.current.statusBars,
                        applyBottom = false
                    )
                )
                AddressInput(viewState, setInput)
            }
        },
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyTop = false
        )
    ) { paddingValues ->
        SuggestionsList(
            viewState = viewState,
            selectAddress = selectAddress,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun TopAppBar(
    viewState: AddressAutoCompleteViewState,
    finishWithoutSelection: () -> Unit,
    finishWithSelection: (DanishAddress) -> Unit,
    contentPadding: PaddingValues,
) {
    TopAppBar(
        title = { Text("Address") },
        backgroundColor = MaterialTheme.colors.surface,
        navigationIcon = {
            IconButton(onClick = finishWithoutSelection) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null
                )
            }
        },
        actions = { // TODO temp "Check" action before auto selection is done
            IconButton(
                onClick = {
                    if (viewState.input.selectedDanishAddress != null) {
                        finishWithSelection(viewState.input.selectedDanishAddress)
                    } else {
                        finishWithoutSelection()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
                )
            }
        },
        elevation = 0.dp,
        contentPadding = contentPadding,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun AddressInput(
    viewState: AddressAutoCompleteViewState,
    setInput: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Surface(
        color = MaterialTheme.colors.surface,
        modifier = modifier
    ) {
        BasicTextField(
            value = viewState.input.rawText,
            onValueChange = { newText ->
                setInput(newText)
            },
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                color = LocalContentColor.current.copy(LocalContentAlpha.current)
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusRequester.freeFocus()
                }
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 24.dp,
                    horizontal = 16.dp
                )
                .focusRequester(focusRequester)
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SuggestionsList(
    viewState: AddressAutoCompleteViewState,
    selectAddress: (DanishAddress) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier) {
        items(
            items = viewState.results,
            key = { item -> item.id ?: item.address }
        ) { address: DanishAddress ->
            val primaryText: String =
                if (address.streetName != null && address.streetNumber != null) {
                    "${address.streetName} ${address.streetNumber}"
                } else {
                    address.address
                }
            val secondaryText: (@Composable () -> Unit)? =
                if (address.postalCode != null && address.city != null) {
                    { Text("${address.postalCode} ${address.city}") }
                } else {
                    null
                }
            ListItem(
                text = { Text(text = primaryText) },
                secondaryText = secondaryText,
                singleLineSecondaryText = true,
                modifier = Modifier.clickable {
                    selectAddress(address)
                    // todo figure out when this should also automatically submit the selection
                }
            )
        }
        // TODO add "Can't find my address red item"
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddressAutoCompleteScreenPreview() {
    HedvigTheme {
        Surface(color = MaterialTheme.colors.background) {
            val previewDanishAddress = DanishAddress.previewData()
            AddressAutoCompleteScreen(
                AddressAutoCompleteViewState(
                    input = DanishAddressInput(previewDanishAddress.toFlatQueryString(), previewDanishAddress),
                    results = DanishAddress.previewList(),
                ),
                {}, {}, {}, {}
            )
        }
    }
}
