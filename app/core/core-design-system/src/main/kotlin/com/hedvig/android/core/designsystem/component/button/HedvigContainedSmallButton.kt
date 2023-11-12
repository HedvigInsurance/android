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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.animation.ThreeDotsLoading
import com.hedvig.android.core.designsystem.material3.squircleMedium

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
          // render the text too so that the same space is taken in all cases
          ButtonText(text, Modifier.alpha(0f), textStyle = textStyle)
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
    modifier = modifier,
  )
}
