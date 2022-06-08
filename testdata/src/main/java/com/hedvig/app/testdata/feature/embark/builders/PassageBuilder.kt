package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.ApiFragment
import com.hedvig.android.owldroid.graphql.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkExternalRedirectLocation
import com.hedvig.android.owldroid.graphql.type.EmbarkMessage

data class PassageBuilder(
    private val name: String,
    private val id: String,
    private val messages: List<MessageFragment> = emptyList(),
    private val response: EmbarkStoryQuery.Response = MessageBuilder("").buildMessageResponse(),
    private val redirects: List<EmbarkStoryQuery.Redirect> = emptyList(),
    private val action: EmbarkStoryQuery.Action,
    private val api: ApiFragment? = null,
    private val tooltip: List<EmbarkStoryQuery.Tooltip> = emptyList(),
    private val links: List<EmbarkLinkFragment> = emptyList(),
    private val tracks: List<EmbarkStoryQuery.Track> = emptyList(),
    private val externalRedirect: EmbarkExternalRedirectLocation? = null,
    private val offerRedirectKeys: List<String>? = null,
) {
    fun build() = EmbarkStoryQuery.Passage(
        name = name,
        id = id,
        messages = messages.map {
            EmbarkStoryQuery.Message(
                __typename = EmbarkMessage.type.name,
                fragments = EmbarkStoryQuery.Message.Fragments(
                    it
                )
            )
        },
        response = response,
        tooltips = tooltip,
        redirects = redirects,
        action = action,
        api = api?.let {
            EmbarkStoryQuery.Api4(
                __typename = it.__typename,
                fragments = EmbarkStoryQuery.Api4.Fragments(it)
            )
        },
        allLinks = links.map {
            EmbarkStoryQuery.AllLink(
                __typename = "",
                fragments = EmbarkStoryQuery.AllLink.Fragments(it)
            )
        },
        tracks = tracks,
        externalRedirect = externalRedirect?.let {
            EmbarkStoryQuery.ExternalRedirect(data = EmbarkStoryQuery.Data1(location = it))
        },
        offerRedirect = offerRedirectKeys?.let { ork ->
            EmbarkStoryQuery.OfferRedirect(
                data = EmbarkStoryQuery.Data2(
                    keys = ork
                )
            )
        },
        variantedOfferRedirects = listOf(),
        quoteCartOfferRedirects = listOf(),
    )
}
