package com.hedvig.app.feature.embark.passages.textaction

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TextActionParameter(
    val link: String,
    val hint: String,
    val messages: List<String>,
    val submitLabel: String,
    val key: String,
    val passageName: String,
    val mask: String?,
) : Parcelable {
    companion object {
        fun from(messages: List<String>, data: EmbarkStoryQuery.TextData, passageName: String) =
                TextActionParameter(
                        link = data.link.fragments.embarkLinkFragment.name,
                        hint = data.placeholder,
                        messages = messages,
                        submitLabel = data.link.fragments.embarkLinkFragment.label,
                        key = data.key,
                        passageName = passageName,
                        mask = data.mask
                )
    }
}
