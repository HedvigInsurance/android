package com.hedvig.android.core.designsystem.component.information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.design.system.R
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

/**
 * https://www.figma.com/file/tpp00CvD8ALUKdjDRzyygv/Android%E2%80%A8-UI-Kit?node-id=1863%3A4046
 */
@Composable
fun AppStateInformation(
  type: AppStateInformationType,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  textAlign: TextAlign = TextAlign.Start,
) {
  AppStateInformation(
    iconPainter = type.icon(),
    title = title,
    description = description,
    modifier = modifier,
    horizontalAlignment = horizontalAlignment,
    textAlign = textAlign,
  )
}

enum class AppStateInformationType {
  Success,
  Information,
  Failure,
}

@Composable
private fun AppStateInformationType.icon(): Painter {
  return painterResource(
    when (this) {
      AppStateInformationType.Success -> R.drawable.ic_checkmark_in_circle
      AppStateInformationType.Information -> R.drawable.ic_checkmark_in_circle
      AppStateInformationType.Failure -> R.drawable.ic_warning_triangle
    },
  )
}

@Composable
private fun AppStateInformation(
  iconPainter: Painter,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  horizontalAlignment: Alignment.Horizontal,
  textAlign: TextAlign = TextAlign.Start,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = horizontalAlignment,
  ) {
    Icon(painter = iconPainter, contentDescription = null, Modifier.size(24.dp))
    Text(text = title, style = MaterialTheme.typography.headlineSmall, textAlign = textAlign)
    Text(
      text = description,
      style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
      textAlign = textAlign,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewSuccess() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AppStateInformation(AppStateInformationType.Success, "Title", "Description")
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFailure() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AppStateInformation(AppStateInformationType.Failure, "Title", "Description")
    }
  }
}
