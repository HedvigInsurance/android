package com.hedvig.app.feature.embark.passages.previousinsurer.askforprice

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
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.PrimaryTextButton
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedTextButton

@Composable
fun RetrievePriceContent(
    onRetrievePriceInfo: () -> Unit,
    onIdentityInput: (String) -> Unit,
    label: String,
    input: String,
    isError: Boolean,
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
            text = "Enter your personal identity number to retrieve the price info.",
            style = MaterialTheme.typography.h6
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = input,
            onValueChange = { onIdentityInput(it) },
            singleLine = true,
            placeholder = { Text("YYMMDD-XXXX") },
            label = { Text(label) },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.background
            ),
            isError = isError
        )
        LargeContainedTextButton(
            modifier = Modifier.padding(top = baseMarginQuadruple),
            text = "Retrieve info",
            onClick = onRetrievePriceInfo
        )
    }
}
