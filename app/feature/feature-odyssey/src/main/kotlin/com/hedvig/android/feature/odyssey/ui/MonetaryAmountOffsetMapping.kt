package com.hedvig.android.feature.odyssey.ui

import androidx.compose.ui.text.input.OffsetMapping

/**
 * [OffsetMapping] for the visual transformation of number 1_000_000.01 into string "1 000 000.01 kr"
 * Handles the spacings between the numbers, while also ignoring any potential currency at the end of the string.
 *
 * [text] is the original number, in string format. For example "1000000.01" for number 1_000_000.01.
 */
class MonetaryAmountOffsetMapping(
  private val text: String,
  private val decimalSeparator: Char,
) : OffsetMapping {
  override fun originalToTransformed(offset: Int): Int {
    if (text.length <= 3) return offset
    val numberOfCharsBeforeFirstSpace = text.withoutDecimal.length % 3
    val spaceIndices = text.withoutDecimal.spaceIndices(numberOfCharsBeforeFirstSpace)
    return spaceIndices.count { it < offset } + offset
  }

  override fun transformedToOriginal(offset: Int): Int {
    if (text.withoutDecimal.length <= 3) return offset.coerceAtMost(text.length)
    val numberOfCharsBeforeFirstSpace = text.withoutDecimal.length % 3
    val integralLength = text.withoutDecimal.length
    val numberOfSpaces = text.withoutDecimal.spaceIndices(numberOfCharsBeforeFirstSpace).count()
    return when {
      numberOfCharsBeforeFirstSpace == 0 -> offset - (offset / 4)
      offset <= numberOfCharsBeforeFirstSpace -> offset
      else -> {
        val indexOfAfterFirstSpace = numberOfCharsBeforeFirstSpace + 1
        if (text.contains(decimalSeparator)) {
          val charactersBeforeDecimal = integralLength + numberOfSpaces
          if (offset <= charactersBeforeDecimal) {
            offset - 1 - ((offset - indexOfAfterFirstSpace) / 4)
          } else {
            val extraOffset = offset - charactersBeforeDecimal
            val tempOffset = offset - extraOffset
            val tmp = tempOffset - 1 - ((tempOffset - indexOfAfterFirstSpace) / 4)
            tmp + extraOffset
          }
        } else {
          offset - 1 - ((offset - indexOfAfterFirstSpace) / 4)
        }
      }
    }.coerceAtMost(text.length)
  }

  private val String.withoutDecimal: String
    get() = this.substringBefore(decimalSeparator)

  private fun String.spaceIndices(numberOfCharsBeforeFirstSpace: Int): List<Int> {
    return (this.indices step 3)
      .map { it + numberOfCharsBeforeFirstSpace }
      .filterNot { it == 0 }
      .filterNot { it == this.length }
  }
}
