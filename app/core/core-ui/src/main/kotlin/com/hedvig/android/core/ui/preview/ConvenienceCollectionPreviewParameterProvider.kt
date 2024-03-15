package com.hedvig.android.core.ui.preview

import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider

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
