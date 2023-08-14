package com.hedvig.android.core.designsystem.component.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

/**
 * The card which looks like a TextField, but functions as a button which has the text in the positions of a button.
 * https://www.figma.com/file/qUhLjrKl98PAzHov9ilaDH/New-Web-UI-Kit?type=design&node-id=114-2605&t=NMbwHBp5OhuKjgZ4-4
 */
@Composable
fun HedvigBigCard(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = MaterialTheme.shapes.squircleMedium,
  content: @Composable () -> Unit,
) {
  HedvigCard(
    onClick = onClick,
    enabled = enabled,
    shape = shape,
    modifier = modifier.heightIn(min = 72.dp),
  ) {
    content()
  }
}

@Composable
fun HedvigBigCard(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.squircleMedium,
  content: @Composable () -> Unit,
) {
  HedvigCard(
    shape = shape,
    modifier = modifier.heightIn(min = 72.dp),
  ) {
    content()
  }
}

@Composable
fun HedvigBigCard(
  onClick: () -> Unit,
  hintText: String,
  inputText: String?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = MaterialTheme.shapes.squircleMedium,
) {
  HedvigBigCard(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
    shape = shape,
  ) {
    Box(
      contentAlignment = Alignment.CenterStart,
      modifier = Modifier
        .heightIn(min = 72.dp)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      if (inputText == null) {
        Text(
          text = hintText,
          style = MaterialTheme.typography.headlineSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
        )
      } else {
        Column {
          Text(
            text = hintText,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
          )
          Text(
            text = inputText,
            style = MaterialTheme.typography.headlineSmall,
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigCardButtonWithInput() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(Modifier.padding(16.dp)) {
        HedvigBigCard(
          onClick = {},
          hintText = "Hint",
          inputText = "Input text",
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigCardButton() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(Modifier.padding(16.dp)) {
        HedvigBigCard(
          onClick = {},
          hintText = "Hint",
          inputText = null,
        )
      }
    }
  }
}
