package com.hedvig.android.core.designsystem.component.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.animation.ThreeDotsLoading
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun HedvigContainedSmallButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
  ),
  elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
  HedvigContainedSmallButton(
    onClick = onClick,
    enabled = enabled,
    elevation = elevation,
    contentPadding = contentPadding,
    colors = colors,
    modifier = modifier,
  ) {
    ButtonText(text = text, textStyle = textStyle)
  }
}

@Composable
fun HedvigContainedSmallButton(
  text: String,
  onClick: () -> Unit,
  isLoading: Boolean,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
  enabled: Boolean = true,
  colors: ButtonColors = ButtonDefaults.buttonColors(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
  ),
  elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
  HedvigContainedSmallButton(
    onClick = onClick,
    enabled = enabled,
    elevation = elevation,
    contentPadding = contentPadding,
    colors = colors,
    modifier = modifier,
  ) {
    val loadingTransition = updateTransition(isLoading)
    loadingTransition.AnimatedContent(
      transitionSpec = {
        fadeIn(tween(durationMillis = 220, delayMillis = 90)) togetherWith fadeOut(tween(90))
      },
      contentAlignment = Alignment.Center,
    ) { isLoading ->
      if (isLoading) {
        Box(
          contentAlignment = Alignment.Center,
        ) {
          ButtonText(
            text = text,
            textStyle = textStyle,
            // render the text too wihtout placing it so that the same space is taken in all cases
            modifier = Modifier.layout { measurable, constraints ->
              val placeable = measurable.measure(constraints)
              layout(placeable.width, placeable.height) {}
            },
          )
          ThreeDotsLoading()
        }
      } else {
        ButtonText(text, textStyle = textStyle)
      }
    }
  }
}

@Composable
private fun HedvigContainedSmallButton(
  onClick: () -> Unit,
  enabled: Boolean,
  colors: ButtonColors,
  modifier: Modifier = Modifier,
  elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
  content: @Composable RowScope.() -> Unit,
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = MaterialTheme.shapes.squircleMedium,
    colors = colors,
    elevation = elevation,
    contentPadding = contentPadding,
  ) {
    content()
  }
}

@Composable
private fun ButtonText(
  text: String,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
  Text(
    text = text,
    style = textStyle,
    textAlign = TextAlign.Center,
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewHedvigContainedSmallButton(
  @PreviewParameter(IsLoadingProvider::class) isLoading: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      HedvigContainedSmallButton(
        text = "Hello there".repeat(5),
        onClick = {},
        isLoading = isLoading,
        modifier = Modifier.padding(24.dp),
      )
    }
  }
}

private class IsLoadingProvider : CollectionPreviewParameterProvider<Boolean>(listOf(true, false))
