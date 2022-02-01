package com.hedvig.app.feature.addressautocompletion.ui

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData
import com.hedvig.app.util.compose.preview.previewList

@OptIn(ExperimentalMaterialApi::class)
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
            TopAppBar(
                title = { Text("Address") },
                navigationIcon = {
                    IconButton(onClick = finishWithoutSelection) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.background,
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
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TextField( // TODO not just plop this down, it's ugly. Maybe integrate in the top bar
                value = viewState.input.rawText,
                onValueChange = { newText ->
                    setInput(newText)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn {
                items(
                    items = viewState.results,
                    key = { item -> item.id }
                ) { address: DanishAddress ->
                    val secondaryText: (@Composable () -> Unit)? =
                        if (address.postalCode != null && address.city != null) {
                            { Text("${address.postalCode} ${address.city}") }
                        } else {
                            null
                        }
                    ListItem(
                        text = { Text("${address.streetName} ${address.streetNumber}") },
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
