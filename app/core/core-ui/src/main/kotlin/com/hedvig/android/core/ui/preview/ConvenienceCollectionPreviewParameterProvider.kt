package com.hedvig.android.core.ui.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
  provider: PreviewParameterProvider<T>,
  content: @Composable (parameterState: T) -> Unit,
) {
  val parameterStateList = remember {
    provider.values.toList() + provider.values.drop(1).toList()
  }
  var parameterStateIndex by remember { mutableIntStateOf(0) }
  HedvigTheme {
    Surface(
      onClick = {
        parameterStateIndex += 1
      },
      color = MaterialTheme.colorScheme.background,
    ) {
      content(parameterStateList[parameterStateIndex % parameterStateList.size])
    }
  }
}
