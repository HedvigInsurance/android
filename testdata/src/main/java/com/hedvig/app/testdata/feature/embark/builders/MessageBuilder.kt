package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.fragment.ResponseExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class MessageBuilder(
    private val text: String,
    private val expressions: List<ExpressionFragment> = emptyList(),
) {
    fun build() = MessageFragment(
        expressions = expressions.map {
            MessageFragment.Expression(fragments = MessageFragment.Expression.Fragments(it))
        },
        text = text
    )

    fun buildMessageResponse() = EmbarkStoryQuery.Response(
        fragments = EmbarkStoryQuery.Response.Fragments(
            messageFragment = build(),
            responseExpressionFragment = null,
        ),
        asEmbarkGroupedResponse = null,
    )

    fun buildExpressionResponse() = EmbarkStoryQuery.Response(
        fragments = EmbarkStoryQuery.Response.Fragments(
            messageFragment = null,
            responseExpressionFragment = ResponseExpressionFragment(
                text = text,
                expressions = expressions.map {
                    ResponseExpressionFragment.Expression(
                        fragments = ResponseExpressionFragment.Expression.Fragments(it)
                    )
                }
            )
        ),
        asEmbarkGroupedResponse = null,
    )
}
