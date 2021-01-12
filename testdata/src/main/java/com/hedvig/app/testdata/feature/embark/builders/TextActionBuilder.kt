package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class TextActionBuilder(
    private val key: String,
    private val placeholder: String = "",
    private val mask: String? = null,
    private val link: EmbarkLinkFragment
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = EmbarkStoryQuery.AsEmbarkTextAction(
            textData = EmbarkStoryQuery.TextData(
                key = key,
                placeholder = placeholder,
                mask = mask,
                link = EmbarkStoryQuery.Link1(
                    fragments = EmbarkStoryQuery.Link1.Fragments(link)
                )
            )
        ),
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null
    )

    companion object {
        const val PERSONAL_NUMBER = "PersonalNumber"
        const val SWEDISH_POSTAL_CODE = "PostalCode"
        const val EMAIL = "Email"
        const val BIRTH_DATE = "BirthDate"
        const val BIRTH_DATE_REVERSE = "BirthDateReverse"
        const val NORWEGIAN_POSTAL_CODE = "NorwegianPostalCode"
    }
}
