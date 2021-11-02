package com.hedvig.app.feature.crossselling.ui

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.CrossSellsQuery
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.CrossSellType
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
    val crossSellType: CrossSellType,
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
    }

    @Parcelize
    data class Highlight(
        val title: String,
        val description: String,
    ) : Parcelable {
        companion object {
            fun from(data: InsuranceQuery.Highlight) = Highlight(
                title = data.title,
                description = data.description,
            )

            fun from(data: CrossSellsQuery.Highlight) = Highlight(
                title = data.title,
                description = data.description,
            )
        }
    }

    enum class CrossSellType {
        HOME_CONTENT, ACCIDENT, TRAVEL, UNKNOWN
    }

    companion object {
        fun from(data: InsuranceQuery.PotentialCrossSell) = CrossSellData(
            title = data.title,
            description = data.description,
            callToAction = data.callToAction,
            action = data.action.asCrossSellEmbark?.embarkStory?.let { story ->
                Action.Embark(story.name, data.title)
            } ?: Action.Chat,
            backgroundUrl = data.imageUrl,
            backgroundBlurHash = data.blurHash,
            crossSellType = data.type.toCrossSellType(),
            typeOfContract = data.contractType.rawValue,
            displayName = data.info.displayName,
            about = data.info.aboutSection,
            perils = data.info.contractPerils.map { Peril.from(it.fragments.perilFragment) },
            terms = data.info.insuranceTerms.map { DocumentItems.Document.from(it.fragments.insuranceTermFragment) },
            highlights = data.info.highlights.map(Highlight::from),
            faq = data.info.faq.map(FAQItem::from),
            insurableLimits = data.info.insurableLimits.map {
                InsurableLimitItem.InsurableLimit.from(it.fragments.insurableLimitsFragment)
            }
        )

        fun from(data: CrossSellsQuery.PotentialCrossSell) = CrossSellData(
            title = data.title,
            description = data.description,
            callToAction = data.callToAction,
            action = data.action.asCrossSellEmbark?.embarkStory?.let { story ->
                Action.Embark(story.name, data.title)
            } ?: Action.Chat,
            backgroundUrl = data.imageUrl,
            backgroundBlurHash = data.blurHash,
            crossSellType = data.type.toCrossSellType(),
            typeOfContract = data.contractType.rawValue,
            displayName = data.info.displayName,
            about = data.info.aboutSection,
            perils = data.info.contractPerils.map { Peril.from(it.fragments.perilFragment) },
            terms = data.info.insuranceTerms.map { DocumentItems.Document.from(it.fragments.insuranceTermFragment) },
            highlights = data.info.highlights.map(Highlight::from),
            faq = data.info.faq.map(FAQItem::from),
            insurableLimits = data.info.insurableLimits.map {
                InsurableLimitItem.InsurableLimit.from(it.fragments.insurableLimitsFragment)
            }
        )
    }
}

fun CrossSellType.toCrossSellType() = when (this) {
    CrossSellType.ACCIDENT -> CrossSellData.CrossSellType.ACCIDENT
    CrossSellType.TRAVEL -> CrossSellData.CrossSellType.TRAVEL
    CrossSellType.HOME_CONTENT -> CrossSellData.CrossSellType.HOME_CONTENT
    CrossSellType.UNKNOWN__ -> CrossSellData.CrossSellType.UNKNOWN
}
