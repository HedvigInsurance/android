package com.hedvig.app.feature.crossselling.ui

import android.os.Parcelable
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.perils.Peril
import giraffe.fragment.CrossSellFragment
import kotlinx.parcelize.Parcelize
import octopus.CrossSalesQuery

@Parcelize
data class CrossSellData(
  val id: String,
  val title: String,
  val description: String,
  val storeUrl: String,
  val backgroundUrl: String,
  val backgroundBlurHash: String,
  val about: String,
  val perils: List<Peril>,
  val terms: List<DocumentItems.Document>,
  val highlights: List<Highlight>,
  val faq: List<FAQItem>,
  val insurableLimits: List<InsurableLimitItem.InsurableLimit>,
) : Parcelable {
  sealed class Action : Parcelable {

    @Parcelize
    data class Embark(val embarkStoryId: String, val title: String) : Action()

    @Parcelize
    object Chat : Action()

    @Parcelize
    data class Web(val url: String) : Action()
  }

  @Parcelize
  data class Highlight(
    val title: String,
    val description: String,
  ) : Parcelable {
    companion object {

      fun from(data: CrossSellFragment.Highlight) = Highlight(
        title = data.title,
        description = data.description,
      )

      fun from(data: CrossSalesQuery.Data.CurrentMember.CrossSell.ProductVariant.Highlight) = Highlight(
        title = data.title,
        description = data.description,
      )
    }
  }

  companion object {
    fun from(data: CrossSalesQuery.Data.CurrentMember.CrossSell) = CrossSellData(
      id = data.id,
      title = data.title,
      description = data.description,
      about = data.about,
      storeUrl = data.storeUrl,
      backgroundUrl = data.imageUrl,
      backgroundBlurHash = data.blurHash,
      perils = data.productVariants.firstOrNull()?.perils?.map {
        Peril.from(it)
      } ?: emptyList(),
      terms = data.productVariants.firstOrNull()?.documents?.map {
        DocumentItems.Document.from(it)
      } ?: emptyList(),
      highlights = data.productVariants.firstOrNull()?.highlights?.map {
        Highlight.from(it)
      } ?: emptyList(),
      faq = data.productVariants.firstOrNull()?.faq?.map {
        FAQItem.from(it)
      } ?: emptyList(),
      insurableLimits = data.productVariants.firstOrNull()?.insurableLimits?.map {
        InsurableLimitItem.InsurableLimit.from(it)
      } ?: emptyList(),
    )
  }
}
