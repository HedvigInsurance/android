package com.hedvig.app.feature.embark.passages.textaction

import android.os.Parcelable
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.util.MaskType
import com.hedvig.app.feature.embark.util.maskTypeFromString
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextActionParameter(
    val link: String,
    val placeholders: List<String?>,
    val hints: List<String?>,
    val keys: List<String?>,
    val messages: List<String>,
    val submitLabel: String,
    val passageName: String,
    val masks: List<MaskType?>,
) : Parcelable {
    companion object {
        fun from(
            messages: List<String>,
            data: EmbarkStoryQuery.TextSetData,
            passageName: String,
        ) =
            TextActionParameter(
                link = data.link.fragments.embarkLinkFragment.name,
                placeholders = data.textActions.map { it.data?.placeholder },
                hints = data.textActions.map { it.data?.title },
                keys = data.textActions.map { it.data?.key },
                messages = messages,
                submitLabel = data.link.fragments.embarkLinkFragment.label,
                passageName = passageName,
                masks = data.textActions.map {
                    it.data?.mask?.let(::maskTypeFromString)
                }
            )

        fun from(
            messages: List<String>,
            data: EmbarkStoryQuery.TextData,
            passageName: String,
        ) =
            TextActionParameter(
                link = data.link.fragments.embarkLinkFragment.name,
                placeholders = listOf(data.placeholder),
                hints = emptyList(),
                keys = listOf(data.key),
                messages = messages,
                submitLabel = data.link.fragments.embarkLinkFragment.label,
                passageName = passageName,
                masks = listOf(data.mask?.let(::maskTypeFromString))
            )
    }
}
