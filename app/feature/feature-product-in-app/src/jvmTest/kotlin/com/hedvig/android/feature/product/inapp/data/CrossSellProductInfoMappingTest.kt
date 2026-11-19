package com.hedvig.android.feature.product.inapp.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.data.contract.InAppProduct
import org.junit.Test

class CrossSellProductInfoMappingTest {
  @Test
  fun `every in-app product the backend sends maps to the app's product`() {
    val mapped = octopus.type.InAppProduct.knownEntries.associateWith { it.toInAppProduct() }

    assertThat(mapped.values.toList()).isEqualTo(InAppProduct.entries.toList())
  }

  @Test
  fun `an in-app product this app version does not know maps to null`() {
    assertThat(octopus.type.InAppProduct.UNKNOWN__.toInAppProduct()).isNull()
  }

  @Test
  fun `a faq answer becomes markdown`() {
    val answer = mapOf(
      "type" to "doc",
      "content" to listOf(
        mapOf(
          "type" to "paragraph",
          "content" to listOf(
            mapOf("type" to "text", "text" to "Everyone "),
            mapOf("type" to "text", "text" to "at home", "marks" to listOf(mapOf("type" to "bold"))),
          ),
        ),
      ),
    )

    assertThat(faqItem("Who is covered?", answer))
      .isEqualTo(CrossSellProductInfo.FaqItem(question = "Who is covered?", answerMarkdown = "Everyone **at home**"))
  }
}
