package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.fragment.ResponseExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

class GroupedResponseBuilder(
    private val title: String,
    private val items: List<MessageFragment> = emptyList(),
    private val each: Pair<String, MessageFragment>? = null,
) {
    fun build() = EmbarkStoryQuery.Response(
        fragments = EmbarkStoryQuery.Response.Fragments(
            messageFragment = null,
            responseExpressionFragment = null
        ),
        asEmbarkGroupedResponse = EmbarkStoryQuery.AsEmbarkGroupedResponse(
            title = EmbarkStoryQuery.Title(
                fragments = EmbarkStoryQuery.Title.Fragments(
                    ResponseExpressionFragment(
                        text = title,
                        expressions = emptyList()
                    )
                )
            ),
            items = items.map { messageFragment ->
                EmbarkStoryQuery.Item(fragments = EmbarkStoryQuery.Item.Fragments(messageFragment))
            },
            each = each?.let { (key, content) ->
                EmbarkStoryQuery.Each(
                    key = key,
                    content = EmbarkStoryQuery.Content(
                        fragments = EmbarkStoryQuery.Content.Fragments(content)
                    )
                )
            },
        )
    )
}
