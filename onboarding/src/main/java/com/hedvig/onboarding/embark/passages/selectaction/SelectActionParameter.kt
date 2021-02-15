package com.hedvig.onboarding.embark.passages.selectaction

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SelectActionParameter(
    val messages: List<String>,
    val actions: List<SelectAction>,
    val passageName: String,
) : Parcelable {

    @Parcelize
    data class SelectAction(
        val link: String,
        val label: String,
        val keys: List<String>,
        val values: List<String>,
    ) : Parcelable

    companion object {
        fun from(
            messages: List<String>,
            data: EmbarkStoryQuery.SelectData,
            passageName: String,
        ) =
            SelectActionParameter(
                messages,
                data.options.map {
                    SelectAction(
                        it.link.fragments.embarkLinkFragment.name,
                        it.link.fragments.embarkLinkFragment.label,
                        it.keys,
                        it.values
                    )
                },
                passageName,
            )
    }
}
