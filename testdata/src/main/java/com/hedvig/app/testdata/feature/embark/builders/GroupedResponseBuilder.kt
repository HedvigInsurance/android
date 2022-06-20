package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.GroupedResponseFragment
import com.hedvig.android.owldroid.graphql.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.fragment.ResponseExpressionFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkGroupedResponse

class GroupedResponseBuilder(
    private val title: String,
    private val items: List<MessageFragment> = emptyList(),
    private val each: Pair<String, MessageFragment>? = null,
) {
    fun build() = EmbarkStoryQuery.Response(
        __typename = EmbarkGroupedResponse.type.name,
        fragments = EmbarkStoryQuery.Response.Fragments(
            messageFragment = null,
            responseExpressionFragment = null,
            groupedResponseFragment = GroupedResponseFragment(
                title = GroupedResponseFragment.Title(
                    __typename = "",
                    fragments = GroupedResponseFragment.Title.Fragments(
                        ResponseExpressionFragment(
                            text = title,
                            expressions = emptyList()
                        )
                    )
                ),
                items = items.map { messageFragment ->
                    GroupedResponseFragment.Item(
                        __typename = "",
                        fragments = GroupedResponseFragment.Item.Fragments(messageFragment)
                    )
                },
                each = each?.let { (key, content) ->
                    GroupedResponseFragment.Each(
                        key = key,
                        content = GroupedResponseFragment.Content(
                            __typename = "",
                            fragments = GroupedResponseFragment.Content.Fragments(content)
                        )
                    )
                },
            )
        ),
    )
}
