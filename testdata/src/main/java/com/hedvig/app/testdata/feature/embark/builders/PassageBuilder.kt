package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class PassageBuilder(
    private val name: String,
    private val id: String,
    private val messages: List<MessageFragment> = emptyList(),
    private val response: MessageFragment = MessageBuilder(text = "").build(),
    private val redirects: List<EmbarkStoryQuery.Redirect> = emptyList(),
    private val action: EmbarkStoryQuery.Action,
    private val api: EmbarkStoryQuery.Api? = null,
    private val tooltip: List<EmbarkStoryQuery.Tooltip> = emptyList()
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
        api = api
    )
}
