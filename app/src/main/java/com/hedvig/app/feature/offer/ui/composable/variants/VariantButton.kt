package com.hedvig.app.feature.offer.ui.composable.variants

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.hedvigBlack
import com.hedvig.app.ui.compose.theme.hedvigBlack12percent
import com.hedvig.app.util.compose.HorizontalTextsWithMaximumSpaceTaken
import com.hedvig.app.util.compose.RadioButton

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun VariantButton(
    id: String,
    title: String,
    tag: String?,
    description: String?,
    cost: String,
    selected: Boolean,
    onClick: (id: String) -> Unit,
) {
    Card(
        onClick = { onClick(id) },
        border = if (selected) {
            BorderStroke(2.dp, hedvigBlack)
        } else {
            BorderStroke(1.dp, hedvigBlack12percent)
        },
        modifier = Modifier
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
            .heightIn(min = 110.dp),
    ) {
        Row(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 16.dp)) {
            RadioButton(
                selected = selected,
                size = 24.dp,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary),
            )
            Spacer(Modifier.width(12.dp))
            Column {
                HorizontalTextsWithMaximumSpaceTaken(
                    startText = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.h6,
                        )
                    },
                    endText = { textAlign ->
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = cost,
                                style = MaterialTheme.typography.h6,
                                textAlign = textAlign,
                            )
                        }
                    },
                    spaceBetween = 10.dp,
                )
                if (tag != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = tag, style = MaterialTheme.typography.caption)
                    }
                }
                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(text = description, style = MaterialTheme.typography.subtitle1)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun VariantButtonPreview(
    @PreviewParameter(VariantButtonInputsProvider::class) inputs: Triple<String, String, Boolean>,
) {
    val (title, subtitle, selected) = inputs
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            VariantButton(
                id = "id",
                title = title,
                tag = subtitle,
                description = "Test description",
                cost = "12923 NOK",
                selected = selected,
                onClick = {},
            )
        }
    }
}

class VariantButtonInputsProvider : CollectionPreviewParameterProvider<Triple<String, String, Boolean>>(
    listOf(
        Triple("Hemförsäkring och Olyckssfall", "Test subtitle", true),
        Triple("Title".repeat(5), "Subtitle", false),
        Triple("Title", "Subtitle".repeat(5), false),
        Triple("Title".repeat(10), "Subtitle".repeat(10), true),
    ),
)
