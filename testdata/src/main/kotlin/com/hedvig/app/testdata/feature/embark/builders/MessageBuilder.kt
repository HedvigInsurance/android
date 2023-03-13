package com.hedvig.app.testdata.feature.embark.builders

import giraffe.EmbarkStoryQuery
import giraffe.fragment.ExpressionFragment
import giraffe.fragment.MessageFragment
import giraffe.fragment.ResponseExpressionFragment
import giraffe.type.EmbarkMessage
import giraffe.type.EmbarkResponseExpression

data class MessageBuilder(
  private val text: String,
  private val expressions: List<ExpressionFragment> = emptyList(),
) {
  fun build() = MessageFragment(
    expressions = expressions.map {
      MessageFragment.Expression(
        __typename = it.__typename,
        fragments = MessageFragment.Expression.Fragments(it),
      )
    },
    text = text,
  )

  fun buildMessageResponse() = EmbarkStoryQuery.Response(
    __typename = EmbarkMessage.type.name,
    fragments = EmbarkStoryQuery.Response.Fragments(
      messageFragment = build(),
      responseExpressionFragment = null,
      groupedResponseFragment = null,
    ),
  )

  fun buildExpressionResponse() = EmbarkStoryQuery.Response(
    __typename = EmbarkResponseExpression.type.name,
    fragments = EmbarkStoryQuery.Response.Fragments(
      messageFragment = null,
      responseExpressionFragment = ResponseExpressionFragment(
        text = text,
        expressions = expressions.map {
          ResponseExpressionFragment.Expression(
            __typename = EmbarkResponseExpression.type.name,
            fragments = ResponseExpressionFragment.Expression.Fragments(it),
          )
        },
      ),
      groupedResponseFragment = null,
    ),
  )
}
