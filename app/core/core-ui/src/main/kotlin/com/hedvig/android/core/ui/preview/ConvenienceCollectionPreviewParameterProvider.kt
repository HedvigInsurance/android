package com.hedvig.android.core.ui.preview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.theme.HedvigTheme

class BooleanCollectionPreviewParameterProvider : CollectionPreviewParameterProvider<Boolean>(
  listOf(true, false),
)

/**
 * Models 3 distinct permutations
 */
class TripleBooleanCollectionPreviewParameterProvider : CollectionPreviewParameterProvider<TripleCase>(
  listOf(TripleCase.FIRST, TripleCase.SECOND, TripleCase.THIRD),
)

enum class TripleCase { FIRST, SECOND, THIRD }

class DoubleBooleanCollectionPreviewParameterProvider : CollectionPreviewParameterProvider<Pair<Boolean, Boolean>>(
  listOf(
    true to true,
    true to false,
    false to true,
    false to false,
  ),
)

/**
 * On click changes the parameter supplied by provider (going through provider.values list one by one).
 * Useful for running previews for AnimatedContent, AnimatedVisibility etc.
 * to see the transition between different states.
 */
@Composable
fun <T> HedvigPreviewWithProvidedParametersAnimation(
  parametersList: List<T>,
  content: @Composable (parameterState: T) -> Unit,
) {
  var parameterStateIndex by remember { mutableIntStateOf(0) }
  val interactionSource = remember { MutableInteractionSource() }
  HedvigTheme {
    Surface(
      color = MaterialTheme.colorScheme.background,
      modifier = Modifier.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = { parameterStateIndex += 1 },
      ),
    ) {
      content(parametersList[parameterStateIndex % parametersList.size])
    }
  }
}
