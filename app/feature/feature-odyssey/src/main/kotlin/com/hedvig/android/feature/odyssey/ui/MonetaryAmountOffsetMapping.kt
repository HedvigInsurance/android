package com.hedvig.android.feature.odyssey.ui

import androidx.compose.ui.text.input.OffsetMapping

/**
 * [OffsetMapping] for the visual transformation of number 1_000_000 into string "1 000 000 kr"
 * Handles the spacings between the numbers, while also ignoring any potential currency at the end of the string.
 *
 * [text] is the original number, in string format. For example "1000000" for number 1_000_000.
 */
class MonetaryAmountOffsetMapping(
  private val text: String,
) : OffsetMapping {
  override fun originalToTransformed(offset: Int): Int {
    if (text.length <= 3) return offset
    val numberOfCharsBeforeFirstSpace = text.length % 3
    val spaceIndices = text.spaceIndices(numberOfCharsBeforeFirstSpace)
    return spaceIndices.count { it < offset } + offset
  }

  override fun transformedToOriginal(offset: Int): Int {
    if (text.length <= 3) return offset.coerceAtMost(text.length)
    val numberOfCharsBeforeFirstSpace = text.length % 3
    return when {
      numberOfCharsBeforeFirstSpace == 0 -> offset - (offset / 4)
      offset <= numberOfCharsBeforeFirstSpace -> offset
      else -> {
        val indexOfAfterFirstSpace = numberOfCharsBeforeFirstSpace + 1
        offset - 1 - ((offset - indexOfAfterFirstSpace) / 4)
      }
    }.coerceAtMost(text.length)
  }

  private fun String.spaceIndices(numberOfCharsBeforeFirstSpace: Int): List<Int> {
    return (this.indices step 3)
      .map { it + numberOfCharsBeforeFirstSpace }
      .filterNot { it == 0 }
      .filterNot { it == this.length }
  }
}
