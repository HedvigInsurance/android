package com.hedvig.app.feature.chat

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hedvig.android.owldroid.fragment.ChatMessageFragment
import com.hedvig.android.owldroid.graphql.ChatMessagesQuery
import com.hedvig.app.R
import com.hedvig.app.util.convertDpToPixel
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.chat_message_file_upload.view.*
import kotlinx.android.synthetic.main.chat_message_hedvig.view.*
import kotlinx.android.synthetic.main.chat_message_user.view.*
import kotlinx.android.synthetic.main.chat_message_user_giphy.view.*
import kotlinx.android.synthetic.main.chat_message_user_image.view.*

class ChatAdapter(context: Context, private val onPressEdit: () -> Unit) :
    androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    private val doubleMargin = context.resources.getDimensionPixelSize(R.dimen.base_margin_double)
    private val baseMargin = context.resources.getDimensionPixelSize(R.dimen.base_margin)
    private val roundingRadius = context.resources.getDimensionPixelSize(R.dimen.image_upload_corner_radius)

    var messages: List<ChatMessagesQuery.Message> = listOf()
        set(value) {
            val oldMessages = messages.toList()

            field = value

            val diff =
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldMessages[oldItemPosition].fragments.chatMessageFragment.globalId ==
                            value[newItemPosition].fragments.chatMessageFragment.globalId

                    override fun getOldListSize(): Int = oldMessages.size

                    override fun getNewListSize(): Int = value.size

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldMessages[oldItemPosition].fragments.chatMessageFragment ==
                            value[newItemPosition].fragments.chatMessageFragment
                })
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder =
        when (viewType) {
            FROM_HEDVIG -> HedvigMessage(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.chat_message_hedvig,
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
            else -> TODO("Handle the invalid invariant")
        }

    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int) =
        if (messages[position].fragments.chatMessageFragment.header.isFromMyself) {
            when {
                isImageUploadMessage(messages[position].fragments.chatMessageFragment.body) -> FROM_ME_IMAGE_UPLOAD
                isFileUploadMessage((messages[position].fragments.chatMessageFragment.body)) -> FROM_ME_FILE_UPLOAD
                isGiphyMessage(messages[position].fragments.chatMessageFragment.body?.text) -> FROM_ME_GIPHY
                isImageMessage(messages[position].fragments.chatMessageFragment.body?.text) -> FROM_ME_IMAGE
                else -> FROM_ME_TEXT
            }
        } else {
            FROM_HEDVIG
        }

    override fun onBindViewHolder(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            FROM_HEDVIG -> {
                (viewHolder as? HedvigMessage)?.apply { bind(messages[position].fragments.chatMessageFragment.body?.text) }
            }
            FROM_ME_TEXT -> {
                (viewHolder as? UserMessage)?.apply {
                    bind(
                        messages[position].fragments.chatMessageFragment.body?.text,
                        position,
                        messages[position].fragments.chatMessageFragment.header.statusMessage,
                        messages[position].fragments.chatMessageFragment.header.isEditAllowed
                    )
                }
            }
            FROM_ME_GIPHY -> {
                (viewHolder as? GiphyUserMessage)?.apply { bind(messages[position].fragments.chatMessageFragment.body?.text) }
            }
            FROM_ME_IMAGE -> {
                (viewHolder as? ImageUserMessage)?.apply { bind(messages[position].fragments.chatMessageFragment.body?.text) }
            }
            FROM_ME_IMAGE_UPLOAD -> {
                (viewHolder as? ImageUploadUserMessage)?.apply { bind(getFileUrl(messages[position].fragments.chatMessageFragment.body)) }
            }
            FROM_ME_FILE_UPLOAD -> {
                (viewHolder as? FileUploadUserMessage)?.apply { bind(getFileUrl(messages[position].fragments.chatMessageFragment.body)) }
            }
        }
    }

    inner class HedvigMessage(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val message: TextView = view.hedvigMessage

        fun reset() {
            message.remove()
        }

        fun bind(text: String?) {
            reset()
            if (text == "") {
                return
            }
            message.show()
            message.text = text
        }
    }

    inner class UserMessage(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val message: TextView = view.userMessage
        val edit: ImageButton = view.editMessage
        val status: TextView = view.statusMessage

        fun bind(text: String?, position: Int, statusMessage: String?, editAllowed: Boolean) {
            message.text = text
            if (statusMessage != null && position == 1) {
                status.text = statusMessage
                status.show()
            } else {
                status.text = ""
                status.remove()
            }
            if (editAllowed) {
                edit.show()
                edit.setHapticClickListener { onPressEdit() }
                message.updateMargin(end = baseMargin)
            } else {
                edit.remove()
                message.updateMargin(end = doubleMargin)
            }
        }
    }

    inner class GiphyUserMessage(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val image: ImageView = view.messageImage

        fun bind(url: String?) {
            Glide
                .with(image)
                .load(url)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
                .override(
                    com.bumptech.glide.request.target.Target.SIZE_ORIGINAL,
                    com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
                )
                .into(image)
        }
    }

    inner class ImageUserMessage(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val image: ImageView = view.uploadedImage

        fun bind(url: String?) {
            image.remove() // Not supported for now as our image resizing API does not work.
            //Glide
            //    .with(image)
            //    .load("${BuildConfig.PIG_URL}/unsafe/280/200/smart/${URLEncoder.encode(url, "utf-8")}")
            //    .apply(RequestOptions.bitmapTransform(RoundedCorners(20)))
            //    .override(
            //        com.bumptech.glide.request.target.Target.SIZE_ORIGINAL,
            //        com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
            //    )
            //    .into(image)
        }
    }

    inner class ImageUploadUserMessage(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val image: ImageView = view.uploadedImage

        fun bind(url: String?) {
            Glide
                .with(image)
                .load(url)
                .transform(RoundedCorners(roundingRadius), FitCenter())
                .override(
                    convertDpToPixel(280f),
                    convertDpToPixel(200f)
                )
                .into(image)
        }
    }

    inner class FileUploadUserMessage(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val label: TextView = view.fileUploadLabel

        fun bind(url: String?) {
            val asUri = Uri.parse(url)
            val extension = getExtension(asUri)

            label.text = interpolateTextKey(
                label.resources.getString(R.string.CHAT_FILE_UPLOADED),
                "EXTENSION" to extension
            )
            label.setHapticClickListener {
                label.context.openUri(Uri.parse(url))
            }
        }
    }

    companion object {
        private const val FROM_HEDVIG = 0
        private const val FROM_ME_TEXT = 1
        private const val FROM_ME_GIPHY = 2
        private const val FROM_ME_IMAGE = 3
        private const val FROM_ME_IMAGE_UPLOAD = 4
        private const val FROM_ME_FILE_UPLOAD = 5

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
            val asUpload = (body as? ChatMessageFragment.AsMessageBodyFile) ?: return false

            return isImageMessage(asUpload.file.signedUrl)
        }

        private fun isFileUploadMessage(body: ChatMessageFragment.Body?): Boolean {
            val asUpload = (body as? ChatMessageFragment.AsMessageBodyFile) ?: return false

            return !isImageMessage(asUpload.file.signedUrl)
        }

        private fun getFileUrl(body: ChatMessageFragment.Body?) =
            (body as? ChatMessageFragment.AsMessageBodyFile)?.file?.signedUrl
    }
}


