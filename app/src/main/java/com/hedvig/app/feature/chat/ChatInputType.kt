package com.hedvig.app.feature.chat

import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import fragment.ChatMessageFragment
import timber.log.Timber
import type.KeyboardType

sealed class ChatInputType {
    companion object {
        fun from(message: ChatMessagesQuery.Message) =
            when (val body = message.fragments.chatMessageFragment.body?.inlineFragment) {
                is ChatMessageFragment.AsMessageBodyFile -> TextInput()
                is ChatMessageFragment.AsMessageBodyText -> TextInput(
                    body.keyboard,
                    body.placeholder,
                    message.fragments.chatMessageFragment.header.richTextChatCompatible
                )
                is ChatMessageFragment.AsMessageBodyNumber -> TextInput(
                    body.keyboard,
                    body.placeholder,
                    false
                )
                is ChatMessageFragment.AsMessageBodySingleSelect -> SingleSelect(
                    body.choices
                        ?: listOf()
                )
                is ChatMessageFragment.AsMessageBodyParagraph -> ParagraphInput
                is ChatMessageFragment.AsMessageBodyAudio -> Audio
                else -> {
                    Timber.e("Implement support for ${message::class.java.simpleName}")
                    NullInput
                }
            }
    }
}

data class TextInput(
    val keyboardType: KeyboardType? = null,
    val hint: String? = null,
    val richTextSupport: Boolean = false
) : ChatInputType()

data class SingleSelect(val options: List<ChatMessageFragment.Choice?>) : ChatInputType()
object Audio : ChatInputType()
object ParagraphInput : ChatInputType()
object NullInput : ChatInputType()
