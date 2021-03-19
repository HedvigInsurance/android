package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.MessageFragment

data class MessageBuilder(
    private val text: String,
    private val expressions: List<MessageFragment.Expression> = emptyList()
) {
    fun build() = MessageFragment(
        expressions = expressions,
        text = text
    )
}
