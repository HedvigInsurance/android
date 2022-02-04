package com.hedvig.app.feature.embark.passages.addressautocomplete.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AddressCard(
    addressText: Pair<String, String?>?,
    placeholderText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        AddressTextColumn(
            addressText = addressText,
            placeholderText = placeholderText,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun AddressTextColumn(
    addressText: Pair<String, String?>?,
    placeholderText: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
    ) {
        if (addressText == null) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    placeholderText,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            Text(
                addressText.first,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )
            addressText.second?.let { secondaryText ->
                if (secondaryText.isBlank()) return@let
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        secondaryText,
                        style = MaterialTheme.typography.body1,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddressCardPreview() {
    HedvigTheme {
        Surface(color = MaterialTheme.colors.background) {
            AddressCard(
                "Willemoesgade 4, st. tv".repeat(3) to "2100 København Ø".repeat(1),
                "",
                {},
            )
        }
    }
}
