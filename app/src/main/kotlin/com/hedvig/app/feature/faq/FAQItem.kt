package com.hedvig.app.feature.faq

import android.os.Parcelable
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.util.safeLet
import giraffe.fragment.CrossSellFragment
import giraffe.fragment.QuoteBundleFragment
import kotlinx.parcelize.Parcelize
import octopus.CrossSalesQuery

@Parcelize
data class FAQItem(
  val headline: String,
  val body: String,
) : Parcelable {

  companion object {
    fun from(data: CrossSellFragment.Faq) = FAQItem(
      headline = data.headline,
      body = data.body,
    )

    fun from(data: CrossSalesQuery.Data.CurrentMember.CrossSell.ProductVariant.Faq) = FAQItem(
      headline = data.headline,
      body = data.body ?: "",
    )

    fun from(data: QuoteBundleFragment.FrequentlyAskedQuestion) =
      safeLet(data.headline, data.body) { headline, body ->
        FAQItem(
          headline = headline,
          body = body,
        )
      }

    fun from(data: QuoteBundle.FrequentlyAskedQuestion) = safeLet(data.title, data.description) { headline, body ->
      FAQItem(
        headline = headline,
        body = body,
      )
    }
  }
}
