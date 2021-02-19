package com.hedvig.onboarding.createoffer.passages.previousinsurer

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PreviousInsurerParameter(
    val messages: List<String>,
    val next: String,
    val skip: String,
    val storeKey: String,
    val previousInsurers: List<PreviousInsurer>
) : Parcelable {
    @Parcelize
    data class PreviousInsurer(
        val name: String,
        val icon: String
    ) : Parcelable

    companion object {
        fun from(messages: List<String>,
                 previousInsuranceAction: EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction) =
            PreviousInsurerParameter(
                messages = messages,
                next = previousInsuranceAction.data.next.fragments.embarkLinkFragment.name,
                skip = previousInsuranceAction.data.skip.fragments.embarkLinkFragment.name,
                previousInsurers = previousInsuranceAction
                    .data
                    .insuranceProviders
                    .map {
                        PreviousInsurer(
                            it.name,
                            it.logo.variants.fragments.iconVariantsFragment.light.svgUrl
                        )
                    },
                storeKey = previousInsuranceAction.data.storeKey,
            )
    }
}
