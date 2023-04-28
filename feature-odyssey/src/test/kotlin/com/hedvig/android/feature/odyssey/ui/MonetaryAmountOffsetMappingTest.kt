package com.hedvig.android.feature.odyssey.ui

import com.hedvig.android.odyssey.ui.MonetaryAmountOffsetMapping
import org.junit.Assert.assertEquals
import org.junit.Test

class MonetaryAmountOffsetMappingTest {
  @Test
  fun `originalToTransformed, normal mapping`() {
    val mapping = MonetaryAmountOffsetMapping("100", '.')

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(2, mapping.originalToTransformed(2))
  }

  @Test
  fun `transformedToOriginal, normal mapping`() {
    val mapping = MonetaryAmountOffsetMapping("100", '.')

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(2, mapping.transformedToOriginal(2))
  }

  @Test
  fun `originalToTransformed, with one extra leading number mapping`() {
    val mapping = MonetaryAmountOffsetMapping(1_000.toString(), '.')
    // transformed = "1 000 kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(3, mapping.originalToTransformed(2))
    assertEquals(4, mapping.originalToTransformed(3))
    assertEquals(5, mapping.originalToTransformed(4))
  }

  @Test
  fun `transformedToOriginal, with one extra leading number mapping`() {
    val mapping = MonetaryAmountOffsetMapping(1_000.toString(), '.')
    // transformed = "1 000 kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(1, mapping.transformedToOriginal(2))
    assertEquals(2, mapping.transformedToOriginal(3))
    assertEquals(3, mapping.transformedToOriginal(4))
    assertEquals(4, mapping.transformedToOriginal(5))

    assertEquals(4, mapping.transformedToOriginal(6))
    assertEquals(4, mapping.transformedToOriginal(7))
    assertEquals(4, mapping.transformedToOriginal(8))
  }

  @Test
  fun `originalToTransformed, with two extra leading numbers mapping`() {
    val mapping = MonetaryAmountOffsetMapping(10_000.toString(), '.')
    // transformed = "10 000 kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(2, mapping.originalToTransformed(2))
    assertEquals(4, mapping.originalToTransformed(3))
    assertEquals(5, mapping.originalToTransformed(4))
    assertEquals(6, mapping.originalToTransformed(5))
  }

  @Test
  fun `transformedToOriginal, with two extra leading numbers mapping`() {
    val mapping = MonetaryAmountOffsetMapping(10_000.toString(), '.')
    // transformed = "10 000 kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(2, mapping.transformedToOriginal(2))
    assertEquals(2, mapping.transformedToOriginal(3))
    assertEquals(3, mapping.transformedToOriginal(4))
    assertEquals(4, mapping.transformedToOriginal(5))
    assertEquals(5, mapping.transformedToOriginal(6))

    assertEquals(5, mapping.transformedToOriginal(7))
    assertEquals(5, mapping.transformedToOriginal(8))
    assertEquals(5, mapping.transformedToOriginal(9))
  }

  @Test
  fun `originalToTransformed, with three extra leading numbers mapping`() {
    val mapping = MonetaryAmountOffsetMapping(100_000.toString(), '.')
    // transformed = "100 000 kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(2, mapping.originalToTransformed(2))
    assertEquals(3, mapping.originalToTransformed(3))
    assertEquals(5, mapping.originalToTransformed(4))
    assertEquals(6, mapping.originalToTransformed(5))
    assertEquals(7, mapping.originalToTransformed(6))
  }

  @Test
  fun `transformedToOriginal, with three extra leading numbers mapping`() {
    val mapping = MonetaryAmountOffsetMapping(100_000.toString(), '.')
    // transformed = "100 000 kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(2, mapping.transformedToOriginal(2))
    assertEquals(3, mapping.transformedToOriginal(3))
    assertEquals(3, mapping.transformedToOriginal(4))
    assertEquals(4, mapping.transformedToOriginal(5))
    assertEquals(5, mapping.transformedToOriginal(6))
    assertEquals(6, mapping.transformedToOriginal(7))

    assertEquals(6, mapping.transformedToOriginal(8))
    assertEquals(6, mapping.transformedToOriginal(9))
    assertEquals(6, mapping.transformedToOriginal(10))
  }

  @Test
  fun `originalToTransformed, with one extra leading number and over a million mapping`() {
    val mapping = MonetaryAmountOffsetMapping(1_000_000.toString(), '.')
    // transformed = "1 000 000 kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(3, mapping.originalToTransformed(2))
    assertEquals(4, mapping.originalToTransformed(3))
    assertEquals(5, mapping.originalToTransformed(4))
    assertEquals(7, mapping.originalToTransformed(5))
    assertEquals(8, mapping.originalToTransformed(6))
    assertEquals(9, mapping.originalToTransformed(7))
  }

  @Test
  fun `transformedToOriginal, with one extra leading number and over a million mapping`() {
    val mapping = MonetaryAmountOffsetMapping(1_000_000.toString(), '.')
    // transformed = "1 000 000 kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(1, mapping.transformedToOriginal(2))
    assertEquals(2, mapping.transformedToOriginal(3))
    assertEquals(3, mapping.transformedToOriginal(4))
    assertEquals(4, mapping.transformedToOriginal(5))
    assertEquals(4, mapping.transformedToOriginal(6))
    assertEquals(5, mapping.transformedToOriginal(7))
    assertEquals(6, mapping.transformedToOriginal(8))
    assertEquals(7, mapping.transformedToOriginal(9))

    assertEquals(7, mapping.transformedToOriginal(10))
    assertEquals(7, mapping.transformedToOriginal(11))
    assertEquals(7, mapping.transformedToOriginal(12))
  }

  @Test
  fun `originalToTransformed, with decimals`() {
    val mapping = MonetaryAmountOffsetMapping(1_000_000.11.toString(), '.')
    // transformed = "1 000 000.00 kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(3, mapping.originalToTransformed(2))
    assertEquals(4, mapping.originalToTransformed(3))
    assertEquals(5, mapping.originalToTransformed(4))
    assertEquals(7, mapping.originalToTransformed(5))
    assertEquals(8, mapping.originalToTransformed(6))
    assertEquals(9, mapping.originalToTransformed(7))
    assertEquals(10, mapping.originalToTransformed(8))
    assertEquals(11, mapping.originalToTransformed(9))
    assertEquals(12, mapping.originalToTransformed(10))
  }

  @Test
  fun `transformedToOriginal, with decimals`() {
    val mapping = MonetaryAmountOffsetMapping(1_000_000.11.toString(), '.')
    // transformed = "1 000 000.00 kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(1, mapping.transformedToOriginal(2))
    assertEquals(2, mapping.transformedToOriginal(3))
    assertEquals(3, mapping.transformedToOriginal(4))
    assertEquals(4, mapping.transformedToOriginal(5))
    assertEquals(4, mapping.transformedToOriginal(6))
    assertEquals(5, mapping.transformedToOriginal(7))
    assertEquals(6, mapping.transformedToOriginal(8))
    assertEquals(7, mapping.transformedToOriginal(9))
    assertEquals(8, mapping.transformedToOriginal(10))
    assertEquals(9, mapping.transformedToOriginal(11))
    assertEquals(10, mapping.transformedToOriginal(12))

    assertEquals(10, mapping.transformedToOriginal(13))
    assertEquals(10, mapping.transformedToOriginal(14))
    assertEquals(10, mapping.transformedToOriginal(15))
  }

  @Test
  fun `originalToTransformed, only with decimal separator`() {
    val mapping = MonetaryAmountOffsetMapping("1000.", '.')
    // transformed = "1 000. kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(3, mapping.originalToTransformed(2))
    assertEquals(4, mapping.originalToTransformed(3))
    assertEquals(5, mapping.originalToTransformed(4))
    assertEquals(6, mapping.originalToTransformed(5))
  }

  @Test
  fun `transformedToOriginal, only with decimal separator`() {
    val mapping = MonetaryAmountOffsetMapping("1000.", '.')
    // transformed = "1 000. kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(1, mapping.transformedToOriginal(2))
    assertEquals(2, mapping.transformedToOriginal(3))
    assertEquals(3, mapping.transformedToOriginal(4))
    assertEquals(4, mapping.transformedToOriginal(5))
    assertEquals(5, mapping.transformedToOriginal(6))

    assertEquals(5, mapping.transformedToOriginal(7))
    assertEquals(5, mapping.transformedToOriginal(8))
    assertEquals(5, mapping.transformedToOriginal(9))
  }

  @Test
  fun `originalToTransformed, with decimal and single digit`() {
    val mapping = MonetaryAmountOffsetMapping("1.00", '.')
    // transformed = "1.00 kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(2, mapping.originalToTransformed(2))
    assertEquals(3, mapping.originalToTransformed(3))
    assertEquals(4, mapping.originalToTransformed(4))
  }

  @Test
  fun `transformedToOriginal, with decimal and single digit`() {
    val mapping = MonetaryAmountOffsetMapping("1.00", '.')
    // transformed = "1.00 kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(2, mapping.transformedToOriginal(2))
    assertEquals(3, mapping.transformedToOriginal(3))
    assertEquals(4, mapping.transformedToOriginal(4))

    assertEquals(4, mapping.transformedToOriginal(5))
    assertEquals(4, mapping.transformedToOriginal(6))
    assertEquals(4, mapping.transformedToOriginal(7))
  }

  @Test
  fun `originalToTransformed, with empty decimal and single digit`() {
    val mapping = MonetaryAmountOffsetMapping("1.", '.')
    // transformed = "1. kr"

    assertEquals(0, mapping.originalToTransformed(0))
    assertEquals(1, mapping.originalToTransformed(1))
    assertEquals(2, mapping.originalToTransformed(2))
  }

  @Test
  fun `transformedToOriginal, with empty decimal and single digit`() {
    val mapping = MonetaryAmountOffsetMapping("1.", '.')
    // transformed = "1. kr"

    assertEquals(0, mapping.transformedToOriginal(0))
    assertEquals(1, mapping.transformedToOriginal(1))
    assertEquals(2, mapping.transformedToOriginal(2))

    assertEquals(2, mapping.transformedToOriginal(3))
    assertEquals(2, mapping.transformedToOriginal(4))
    assertEquals(2, mapping.transformedToOriginal(5))
  }
}
