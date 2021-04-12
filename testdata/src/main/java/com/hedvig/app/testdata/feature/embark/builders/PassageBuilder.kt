package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExternalRedirectLocation

data class PassageBuilder(
    private val name: String,
    private val id: String,
    private val messages: List<MessageFragment> = emptyList(),
    private val response: MessageFragment = MessageBuilder(text = "").build(),
    private val redirects: List<EmbarkStoryQuery.Redirect> = emptyList(),
    private val action: EmbarkStoryQuery.Action,
    private val api: EmbarkStoryQuery.Api? = null,
    private val tooltip: List<EmbarkStoryQuery.Tooltip> = emptyList(),
    private val links: List<EmbarkLinkFragment> = emptyList(),
    private val tracks: List<EmbarkStoryQuery.Track> = emptyList(),
    private val externalRedirect: EmbarkExternalRedirectLocation? = null,
) {
    fun build() = EmbarkStoryQuery.Passage(
        name = name,
        id = id,
        messages = messages.map {
            EmbarkStoryQuery.Message(
                fragments = EmbarkStoryQuery.Message.Fragments(
                    it
                )
            )
        },
        response = EmbarkStoryQuery.Response(
            fragments = EmbarkStoryQuery.Response.Fragments(
                response
            )
        ),
        tooltips = tooltip,
        redirects = redirects,
        action = action,
        api = api,
        allLinks = links.map { EmbarkStoryQuery.AllLink(fragments = EmbarkStoryQuery.AllLink.Fragments(it)) },
        tracks = tracks,
        externalRedirect = externalRedirect?.let {
            EmbarkStoryQuery.ExternalRedirect(data = EmbarkStoryQuery.Data1(location = it))
        },
        offerRedirect = EmbarkStoryQuery.OfferRedirect(
            data = EmbarkStoryQuery.Data2(
                keys = listOf()
            )
        )
    )
}
