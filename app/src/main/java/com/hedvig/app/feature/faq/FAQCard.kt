package com.hedvig.app.feature.faq

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FAQCard(
    openSheet: (FAQItem) -> Unit,
    items: List<FAQItem>,
) {
    Card {
        Column {
            items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = { openSheet(item) })
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = item.headline,
                        style = MaterialTheme.typography.subtitle1,
                    )
                }
            }
        }
    }
}
