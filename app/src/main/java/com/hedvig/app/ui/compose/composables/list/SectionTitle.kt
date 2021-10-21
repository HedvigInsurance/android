package com.hedvig.app.ui.compose.composables.list

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.ui.compose.theme.HedvigTheme

/**
 * Matches the specification of the Section Title-component from the List-components section of the design system
 * https://www.figma.com/file/tpp00CvD8ALUKdjDRzyygv/Android-UI-Kit?node-id=1711%3A3938
 */
@Composable
fun SectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    notification: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val colorError = MaterialTheme.colors.error
        if (notification) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = colorError)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.h6,
        )
    }
}

@Preview(
    name = "Section Title, Notification = False",
    group = "List",
)
@Composable
fun SectionTitlePreviewNoNotification() {
    HedvigTheme {
        SectionTitle(
            text = "Section Title"
        )
    }
}

@Preview(
    name = "Section Title, Notification = True",
    group = "List",
)
@Composable
fun SectionTitlePreviewNotification() {
    HedvigTheme {
        SectionTitle(
            text = "Section Title",
            notification = true
        )
    }
}
