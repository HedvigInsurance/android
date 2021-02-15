package com.hedvig.onboarding.embark.passages.textactionset

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextActionSetParameter(
    val link: String,
    val placeholders: List<String?>,
    val keys: List<String?>,
    val messages: List<String>,
    val submitLabel: String,
    val passageName: String,
    val mask: List<String?>,
) : Parcelable {
    companion object {
        fun from(
            messages: List<String>,
            data: EmbarkStoryQuery.TextSetData,
            passageName: String,
        ) =
            TextActionSetParameter(
                link = data.link.fragments.embarkLinkFragment.name,
                placeholders = data.textActions.map { it.data?.placeholder },
                keys = data.textActions.map { it.data?.key },
                messages = messages,
                submitLabel = data.link.fragments.embarkLinkFragment.label,
                passageName = passageName,
                mask = data.textActions.map { it.data?.mask }
            )
    }
}
