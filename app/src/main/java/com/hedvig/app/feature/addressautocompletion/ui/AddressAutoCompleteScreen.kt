package com.hedvig.app.feature.addressautocompletion.ui

import androidx.compose.runtime.Composable
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress

@Composable
fun AddressAutoCompleteScreen(
    viewState: AddressAutoCompleteViewState,
    selectAddress: (DanishAddress) -> Unit,
    setInput: (String) -> Unit,
    finishWithSelection: (DanishAddress) -> Unit,
    finishWithoutSelection: () -> Unit,
) {
}
