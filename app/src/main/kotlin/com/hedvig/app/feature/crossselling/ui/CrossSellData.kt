package com.hedvig.app.feature.crossselling.ui

import android.os.Parcelable
import com.hedvig.android.apollo.graphql.fragment.CrossSellFragment
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.faq.FAQItem
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.perils.Peril
import kotlinx.parcelize.Parcelize

@Parcelize
data class CrossSellData(
  val title: String,
  val description: String,
  val callToAction: String,
  val action: Action,
  val backgroundUrl: String,
  val backgroundBlurHash: String,
  val crossSellType: String,
  val typeOfContract: String,
  val displayName: String,
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
    data class Web(val url: String): Action() // TODO Query from backend when available
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
    }
  }

  companion object {
    fun from(data: CrossSellFragment) = CrossSellData(
      title = data.title,
      description = data.description,
      callToAction = data.callToAction,
      action = data.action.asCrossSellEmbark?.embarkStoryV2?.name?.let { storyId ->
        Action.Embark(storyId, data.title)
      } ?: Action.Chat,
      backgroundUrl = data.imageUrl,
      backgroundBlurHash = data.blurHash,
      crossSellType = data.type.rawValue,
      typeOfContract = data.contractType.rawValue,
      displayName = data.info.displayName,
      about = data.info.aboutSection,
      perils = data.info.contractPerils.map { Peril.from(it.fragments.perilFragment) },
      terms = data.info.insuranceTerms.map { DocumentItems.Document.from(it.fragments.insuranceTermFragment) },
      highlights = data.info.highlights.map(Highlight::from),
      faq = data.info.faq.map(FAQItem::from),
      insurableLimits = data.info.insurableLimits.map {
        InsurableLimitItem.InsurableLimit.from(it.fragments.insurableLimitsFragment)
      },
    )
  }
}
