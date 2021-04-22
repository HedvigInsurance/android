package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.fragment.MessageFragment

data class MessageBuilder(
    private val text: String,
    private val expressions: List<ExpressionFragment> = emptyList(),
) {
    fun build() = MessageFragment(
        expressions = expressions.map { MessageFragment.Expression(fragments = MessageFragment.Expression.Fragments(it)) },
        text = text
    )
}
