package com.hedvig.app.feature.chat.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.hedvig.android.owldroid.fragment.ChatMessageFragment
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.ChatMessageFileUploadBinding
import com.hedvig.app.databinding.ChatMessageHedvigBinding
import com.hedvig.app.databinding.ChatMessageUserBinding
import com.hedvig.app.databinding.ChatMessageUserGiphyBinding
import com.hedvig.app.databinding.ChatMessageUserImageBinding
import com.hedvig.app.feature.chat.service.ChatTracker
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.viewBinding
import e

class ChatAdapter(
    private val context: Context,
    private val onPressEdit: () -> Unit,
    private val tracker: ChatTracker,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val doubleMargin = context.resources.getDimensionPixelSize(R.dimen.base_margin_double)
    private val baseMargin = context.resources.getDimensionPixelSize(R.dimen.base_margin)

    val recyclerViewPreloader =
        RecyclerViewPreloader(
            Glide.with(context),
            ChatPreloadModelProvider(),
            ViewPreloadSizeProvider(),
            10
        )

    var messages: List<ChatMessagesQuery.Message> = listOf()
        set(value) {
            val oldMessages = messages.toList()

            field = value

            val diff =
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun areItemsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int,
                    ): Boolean =
                        oldMessages.getOrNull(oldItemPosition)?.fragments?.chatMessageFragment?.globalId ==
                            value.getOrNull(newItemPosition)?.fragments?.chatMessageFragment?.globalId

                    override fun getOldListSize(): Int = oldMessages.size

                    override fun getNewListSize(): Int = value.size

                    override fun areContentsTheSame(
                        oldItemPosition: Int,
                        newItemPosition: Int,
                    ): Boolean =
                        oldMessages.getOrNull(oldItemPosition)?.fragments?.chatMessageFragment ==
                            value.getOrNull(newItemPosition)?.fragments?.chatMessageFragment
                })
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        when (viewType) {
            FROM_HEDVIG -> HedvigMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_hedvig,
                    parent,
                    false
                )
            )
            FROM_HEDVIG_GIPHY -> HedvigGiphyMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_hedvig_giphy,
                    parent,
                    false
                )
            )
            FROM_ME_TEXT -> UserMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_user,
                    parent,
                    false
                )
            )
            FROM_ME_GIPHY -> GiphyUserMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_user_giphy,
                    parent,
                    false
                )
            )
            FROM_ME_IMAGE -> ImageUserMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_user_image,
                    parent,
                    false
                )
            )
            FROM_ME_IMAGE_UPLOAD -> ImageUploadUserMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_user_image,
                    parent,
                    false
                )
            )
            FROM_ME_FILE_UPLOAD -> FileUploadUserMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_file_upload,
                    parent,
                    false
                )
            )
            NULL_RENDER -> NullMessage(View(parent.context))
            else -> TODO("Handle the invalid invariant")
        }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int) =
        messages.getOrNull(position)?.fragments?.chatMessageFragment?.header?.fromMyself?.let { isFromMyself ->
            if (isFromMyself) {
                when {
                    isImageUploadMessage(
                        messages[position].fragments.chatMessageFragment.body
                    ) -> FROM_ME_IMAGE_UPLOAD
                    isFileUploadMessage(
                        messages[position].fragments.chatMessageFragment.body
                    ) -> FROM_ME_FILE_UPLOAD
                    isGiphyMessage(
                        messages[position].fragments.chatMessageFragment.body.asMessageBodyCore?.text
                    ) -> FROM_ME_GIPHY
                    isImageMessage(
                        messages[position].fragments.chatMessageFragment.body.asMessageBodyCore?.text
                    ) -> FROM_ME_IMAGE
                    else -> FROM_ME_TEXT
                }
            } else {
                when {
                    isGiphyMessage(
                        messages[position].fragments.chatMessageFragment.body.asMessageBodyCore?.text
                    ) -> FROM_HEDVIG_GIPHY
                    isAudioMessage(
                        messages[position].fragments.chatMessageFragment.body
                    ) -> NULL_RENDER // This message sucks. Lets kill it
                    else -> FROM_HEDVIG
                }
            }
        } ?: run {
            e { "Found no message to render with position: %d $position" }
            NULL_RENDER
        }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            FROM_HEDVIG -> {
                (viewHolder as? HedvigMessage)?.apply {
                    bind(messages[position].fragments.chatMessageFragment.body.asMessageBodyCore?.text)
                }
            }
            FROM_HEDVIG_GIPHY -> {
                (viewHolder as? HedvigGiphyMessage)?.apply {
                    bind(messages[position].fragments.chatMessageFragment.body.asMessageBodyCore?.text)
                }
            }
            FROM_ME_TEXT -> {
                (viewHolder as? UserMessage)?.apply {
                    bind(
                        messages[position].fragments.chatMessageFragment.body.asMessageBodyCore?.text,
                        position,
                        messages[position].fragments.chatMessageFragment.header.statusMessage,
                        messages[position].fragments.chatMessageFragment.header.editAllowed
                    )
                }
            }
            FROM_ME_GIPHY -> {
                (viewHolder as? GiphyUserMessage)?.apply {
                    bind(messages[position].fragments.chatMessageFragment.body.asMessageBodyCore?.text)
                }
            }
            FROM_ME_IMAGE -> {
                (viewHolder as? ImageUserMessage)?.apply { bind() }
            }
            FROM_ME_IMAGE_UPLOAD -> {
                (viewHolder as? ImageUploadUserMessage)?.apply {
                    bind(
                        getFileUrl(
                            messages[position].fragments.chatMessageFragment.body
                        )
                    )
                }
            }
            FROM_ME_FILE_UPLOAD -> {
                (viewHolder as? FileUploadUserMessage)?.apply {
                    bind(
                        getFileUrl(
                            messages[position].fragments.chatMessageFragment.body
                        )
                    )
                }
            }
            NULL_RENDER -> {
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        when (holder) {
            is HedvigGiphyMessage -> {
                Glide
                    .with(holder.binding.messageImage)
                    .clear(holder.binding.messageImage)
            }
            is GiphyUserMessage -> {
                Glide
                    .with(holder.binding.messageImage)
                    .clear(holder.binding.messageImage)
            }
            is ImageUploadUserMessage -> {
                Glide
                    .with(holder.binding.uploadedImage)
                    .clear(holder.binding.uploadedImage)
            }
        }
    }

    override fun getItemId(position: Int) =
        messages.getOrNull(position)?.fragments?.chatMessageFragment?.globalId?.toLong()
            ?: position.toLong()

    inner class HedvigMessage(view: View) : RecyclerView.ViewHolder(view) {
        private val binding by viewBinding(ChatMessageHedvigBinding::bind)
        fun reset() {
            binding.hedvigMessage.remove()
        }

        fun bind(text: String?) {
            reset()
            if (text == "") {
                return
            }
            binding.hedvigMessage.apply {
                show()
                this.text = text
            }
        }
    }

    inner class HedvigGiphyMessage(view: View) : RecyclerView.ViewHolder(view) {
        val binding by viewBinding(ChatMessageUserGiphyBinding::bind)
        fun bind(url: String?) {
            binding.apply {
                Glide
                    .with(messageImage)
                    .load(url)
                    .transform(FitCenter(), RoundedCorners(40))
                    .into(messageImage)
                    .clearOnDetach()
            }
        }
    }

    inner class UserMessage(view: View) : RecyclerView.ViewHolder(view) {
        private val binding by viewBinding(ChatMessageUserBinding::bind)
        fun bind(text: String?, position: Int, statusText: String?, editAllowed: Boolean) {
            binding.apply {
                userMessage.text = text
                if (statusText != null && position == 1) {
                    statusMessage.text = statusText
                    statusMessage.show()
                } else {
                    statusMessage.text = ""
                    statusMessage.remove()
                }
                if (editAllowed) {
                    editMessage.show()
                    editMessage.setHapticClickListener {
                        tracker.editMessage()
                        onPressEdit()
                    }
                    userMessage.updateMargin(end = baseMargin)
                } else {
                    editMessage.remove()
                    userMessage.updateMargin(end = doubleMargin)
                }
            }
        }
    }

    inner class GiphyUserMessage(view: View) : RecyclerView.ViewHolder(view) {
        val binding by viewBinding(ChatMessageUserGiphyBinding::bind)
        fun bind(url: String?) {
            binding.apply {
                Glide
                    .with(messageImage)
                    .load(url)
                    .transform(FitCenter(), RoundedCorners(40))
                    .into(messageImage)
                    .clearOnDetach()
            }
        }
    }

    inner class ImageUserMessage(view: View) : RecyclerView.ViewHolder(view) {
        private val binding by viewBinding(ChatMessageUserImageBinding::bind)

        fun bind() {
            binding.uploadedImage.remove()
        }
    }

    inner class ImageUploadUserMessage(view: View) : RecyclerView.ViewHolder(view) {
        val binding by viewBinding(ChatMessageUserImageBinding::bind)

        fun bind(url: String?) {
            binding.apply {
                Glide
                    .with(uploadedImage)
                    .load(url)
                    .transform(FitCenter(), RoundedCorners(40))
                    .into(uploadedImage)
                    .clearOnDetach()
            }
        }
    }

    inner class FileUploadUserMessage(view: View) : RecyclerView.ViewHolder(view) {
        private val binding by viewBinding(ChatMessageFileUploadBinding::bind)
        fun bind(url: String?) {
            val asUri = Uri.parse(url)
            val extension = getExtension(asUri)

            binding.apply {
                fileUploadLabel.text =
                    fileUploadLabel.resources.getString(R.string.CHAT_FILE_UPLOADED, extension)
                fileUploadLabel.setHapticClickListener {
                    tracker.openUploadedFile()
                    fileUploadLabel.context.openUri(Uri.parse(url))
                }
            }
        }
    }

    inner class NullMessage(view: View) : RecyclerView.ViewHolder(view)

    inner class ChatPreloadModelProvider :
        ListPreloader.PreloadModelProvider<ChatMessagesQuery.Message> {
        override fun getPreloadItems(position: Int): List<ChatMessagesQuery.Message> =
            messages.getOrNull(position)?.let { message ->
                when {
                    isGiphyMessage(message.fragments.chatMessageFragment.body.asMessageBodyCore?.text) -> listOf(
                        message
                    )
                    isImageUploadMessage(message.fragments.chatMessageFragment.body) -> listOf(
                        message
                    )
                    else -> emptyList()
                }
            } ?: emptyList()

        override fun getPreloadRequestBuilder(item: ChatMessagesQuery.Message): RequestBuilder<*>? {
            val url = when {
                isGiphyMessage(
                    item.fragments.chatMessageFragment.body.asMessageBodyCore?.text
                ) -> item.fragments.chatMessageFragment.body.asMessageBodyCore?.text
                isImageUploadMessage(
                    item.fragments.chatMessageFragment.body
                ) -> item.fragments.chatMessageFragment.body.asMessageBodyFile?.file?.signedUrl
                else -> null
            }
            return Glide
                .with(context)
                .load(url)
                .transform(FitCenter(), RoundedCorners(40))
        }
    }

    companion object {
        private const val FROM_HEDVIG = 0
        private const val FROM_HEDVIG_GIPHY = 7
        private const val FROM_ME_TEXT = 1
        private const val FROM_ME_GIPHY = 2
        private const val FROM_ME_IMAGE = 3
        private const val FROM_ME_IMAGE_UPLOAD = 4
        private const val FROM_ME_FILE_UPLOAD = 5
        private const val NULL_RENDER = 6

        private val imageExtensions = listOf(
            "jpg", "png", "gif", "jpeg"
        )

        private fun isGiphyMessage(text: String?) = text?.contains("giphy.com") ?: false
        private fun getExtension(uri: Uri) = uri.lastPathSegment?.substringAfterLast('.', "")
        private fun isImageMessage(text: String?): Boolean {
            val asUri = Uri.parse(text)

            return imageExtensions.contains(asUri.lastPathSegment?.substringAfterLast('.', ""))
        }

        private fun isImageUploadMessage(body: ChatMessageFragment.Body?): Boolean {
            val asUpload = body?.asMessageBodyFile ?: return false

            return isImageMessage(asUpload.file.signedUrl)
        }

        private fun isFileUploadMessage(body: ChatMessageFragment.Body?): Boolean {
            val asUpload = body?.asMessageBodyFile ?: return false

            return !isImageMessage(asUpload.file.signedUrl)
        }

        private fun isAudioMessage(body: ChatMessageFragment.Body?) =
            body?.asMessageBodyAudio != null

        private fun getFileUrl(body: ChatMessageFragment.Body?) =
            body?.asMessageBodyFile?.file?.signedUrl
    }
}
