package com.hedvig.android.core.designsystem.component.success

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark

@Composable
fun HedvigSuccessSection(
  title: String,
  modifier: Modifier = Modifier,
  subTitle: String? = null,
  contentPadding: PaddingValues = WindowInsets.safeDrawing.asPaddingValues(),
  withDefaultVerticalSpacing: Boolean = true,
  iconImageVector: ImageVector = Icons.Hedvig.Checkmark,
  iconModifier: Modifier = Modifier.size(24.dp),
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .padding(contentPadding)
      .padding(horizontal = 16.dp),
  ) {
    if (withDefaultVerticalSpacing) {
      Spacer(Modifier.height(32.dp))
    }
    Icon(
      imageVector = iconImageVector,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.typeElement,
      modifier = iconModifier,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = title,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(2.dp))
    if (subTitle != null) {
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Text(
          text = subTitle,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
          style = MaterialTheme.typography.bodyLarge.copy(
            lineBreak = LineBreak.Heading,
          ),
        )
      }
    }
    if (withDefaultVerticalSpacing) {
      Spacer(Modifier.height(32.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigSuccessSection() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigSuccessSection(
        title = "Address updated",
        subTitle = "Your new home will be insured starting from 2023.03.12",
      )
    }
  }
}
