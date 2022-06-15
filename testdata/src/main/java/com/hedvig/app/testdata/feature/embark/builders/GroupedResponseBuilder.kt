package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.fragment.ResponseExpressionFragment

class GroupedResponseBuilder(
    private val title: String,
    private val items: List<MessageFragment> = emptyList(),
    private val each: Pair<String, MessageFragment>? = null,
) {
    fun build() = EmbarkStoryQuery.Response(
        __typename = "",
        fragments = EmbarkStoryQuery.Response.Fragments(
            messageFragment = null,
            responseExpressionFragment = null
        ),
        asEmbarkGroupedResponse = EmbarkStoryQuery.AsEmbarkGroupedResponse(
            __typename = "",
            title = EmbarkStoryQuery.Title(
                __typename = "",
                fragments = EmbarkStoryQuery.Title.Fragments(
                    ResponseExpressionFragment(
                        text = title,
                        expressions = emptyList()
                    )
                )
            ),
            items = items.map { messageFragment ->
                EmbarkStoryQuery.Item(
                    __typename = "",
                    fragments = EmbarkStoryQuery.Item.Fragments(messageFragment)
                )
            },
            each = each?.let { (key, content) ->
                EmbarkStoryQuery.Each(
                    key = key,
                    content = EmbarkStoryQuery.Content(
                        __typename = "",
                        fragments = EmbarkStoryQuery.Content.Fragments(content)
                    )
                )
            },
        )
    )
}
