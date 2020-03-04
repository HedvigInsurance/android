package com.hedvig.app.feature.chat

import com.hedvig.android.owldroid.fragment.ChatMessageFragment
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.android.owldroid.type.KeyboardType
import timber.log.Timber

sealed class ChatInputType {
    companion object {
        fun from(message: ChatMessagesQuery.Message): ChatInputType {
            val body = message.fragments.chatMessageFragment.body
            body.asMessageBodyFile?.let {
                return TextInput()
            }
            body.asMessageBodyText?.let { messageBodyText ->
                return TextInput(
                    messageBodyText.keyboard,
                    messageBodyText.placeholder,
                    message.fragments.chatMessageFragment.header.richTextChatCompatible
                )
            }
            body.asMessageBodyNumber?.let { messageBodyNumber ->
                return TextInput(
                    messageBodyNumber.keyboard,
                    messageBodyNumber.placeholder,
                    false
                )
            }
            body.asMessageBodySingleSelect?.let { messageBodySingleSelect ->
                SingleSelect(messageBodySingleSelect.choices?.filterNotNull() ?: emptyList())
            }
            body.asMessageBodyParagraph?.let {
                return ParagraphInput
            }
            body.asMessageBodyAudio?.let {
                return Audio
            }

            Timber.e("Implement support for ${message::class.java.simpleName}")
            return NullInput
        }
    }
}

data class TextInput(
    val keyboardType: KeyboardType? = null,
    val hint: String? = null,
    val richTextSupport: Boolean = false
) : ChatInputType()

data class SingleSelect(val options: List<ChatMessageFragment.Choice>) : ChatInputType()
object Audio : ChatInputType()
object ParagraphInput : ChatInputType()
object NullInput : ChatInputType()
