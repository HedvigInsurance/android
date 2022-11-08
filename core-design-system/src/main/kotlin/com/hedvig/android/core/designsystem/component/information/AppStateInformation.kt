package com.hedvig.android.core.designsystem.component.information

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.R
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
) {
  AppStateInformation(
    iconPainter = type.icon(),
    title = title,
    description = description,
    modifier = modifier,
  )
}

enum class AppStateInformationType {
  Success, Information, Failure
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
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Icon(painter = iconPainter, contentDescription = null, Modifier.size(24.dp))
    Text(text = title, style = MaterialTheme.typography.h5)
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
      Text(text = description, style = MaterialTheme.typography.body1)
    }
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SuccessPreview() {
  HedvigTheme {
    AppStateInformation(AppStateInformationType.Success, "Title", "Description")
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FailurePreview() {
  HedvigTheme {
    AppStateInformation(AppStateInformationType.Failure, "Title", "Description")
  }
}
