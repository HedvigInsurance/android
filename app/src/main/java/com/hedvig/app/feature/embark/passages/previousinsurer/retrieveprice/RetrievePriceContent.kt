package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.PrimaryTextButton
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedTextButton

@Composable
fun RetrievePriceContent(
    onRetrievePriceInfo: () -> Unit,
    onIdentityInput: (String) -> Unit,
    viewState: RetrievePriceViewModel.ViewState,
) {
    val baseMargin = dimensionResource(R.dimen.base_margin)
    val baseMarginDouble = dimensionResource(R.dimen.base_margin_double)
    val baseMarginQuadruple = dimensionResource(R.dimen.base_margin_quadruple)

    Column(
        modifier = Modifier
            .padding(baseMarginDouble)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(baseMarginDouble)
    ) {
        Text(
            modifier = Modifier.padding(top = baseMargin),
            text = stringResource(viewState.ssnTitleTextKey),
            style = MaterialTheme.typography.h6
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = viewState.input,
            onValueChange = { onIdentityInput(it) },
            singleLine = true,
            placeholder = { Text(stringResource(viewState.ssnAssistTextKey)) },
            label = {
                Text(
                    viewState.errorTextKey?.let {
                        stringResource(id = it)
                    } ?: stringResource(viewState.ssnInputLabelTextKey)
                )
            },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.background
            ),
            isError = viewState.isError
        )
        LargeContainedTextButton(
            modifier = Modifier.padding(top = baseMarginQuadruple),
            text = stringResource(R.string.insurely_ssn_continue_button_text),
            onClick = onRetrievePriceInfo
        )
    }
}
