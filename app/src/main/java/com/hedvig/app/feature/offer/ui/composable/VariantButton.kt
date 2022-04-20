package com.hedvig.app.feature.offer.ui.composable

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigBlack
import com.hedvig.app.ui.compose.theme.hedvigBlack12percent

@Composable
fun VariantButton(
    id: String,
    title: String,
    subTitle: String?,
    cost: String,
    selected: Boolean,
    onClick: (id: String) -> Unit
) {
    Card(
        border = if (selected) {
            BorderStroke(3.dp, hedvigBlack)
        } else {
            BorderStroke(1.dp, hedvigBlack12percent)
        },
        modifier = Modifier
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            .clickable {
                onClick(id)
            }
    ) {
        Row(
            Modifier
                .height(106.dp)
        ) {
            RadioButton(selected = selected, onClick = { onClick(id) }, modifier = Modifier.padding(top = 4.dp))
            Column(modifier = Modifier.padding(top = 16.dp).width(200.dp)) {
                Text(text = title, style = MaterialTheme.typography.h6)
                if (subTitle != null) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = subTitle, style = MaterialTheme.typography.subtitle1)
                    }
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = cost,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(top = 16.dp, end = 12.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Preview
@Composable
fun VariantButtonPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            VariantButton(
                id = "id",
                title = "Hemförsäkring och Olyckssf",
                subTitle = "Test subtitle",
                cost = "12923 NOK",
                selected = false,
                onClick = {}
            )
        }
    }
}
