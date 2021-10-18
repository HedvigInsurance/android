package com.hedvig.app.feature.crossselling.ui

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.InsuranceQuery
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
    val typeOfContract: String,
    val displayName: String,
    val about: String,
    val perils: List<Peril>,
    val terms: List<Unit>, // TODO,
    val highlights: List<Highlight>, // TODO
    val faq: List<Faq>,
    val insurableLimits: List<Unit>, // TODO
) : Parcelable {
    sealed class Action : Parcelable {
        @Parcelize
        data class Embark(val embarkStoryId: String) : Action()

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
        }
    }

    @Parcelize
    data class Faq(
        val headline: String,
        val body: String,
    ) : Parcelable {
        companion object {
            fun from(data: InsuranceQuery.Faq) = Faq(
                headline = data.headline,
                body = data.body,
            )
        }
    }

    companion object {
        fun from(data: InsuranceQuery.PotentialCrossSell) = CrossSellData(
            title = data.title,
            description = data.description,
            callToAction = data.callToAction,
            action = data.action.asCrossSellEmbark?.embarkStory?.let { story ->
                Action.Embark(story.name)
            } ?: Action.Chat,
            backgroundUrl = data.imageUrl,
            backgroundBlurHash = data.blurHash,
            typeOfContract = data.contractType.rawValue,
            displayName = data.info!!.displayName,
            about = data.info!!.aboutSection,
            perils = data.info!!.contractPerils.map { Peril.from(it.fragments.perilFragment) },
            terms = emptyList(), // TODO
            highlights = data.info!!.highlights.map(Highlight::from),
            faq = data.info!!.faq.map(Faq::from),
            insurableLimits = emptyList(), // TODO
        )
    }
}
