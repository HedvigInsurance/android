package com.hedvig.app.feature.chat.ui

import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.util.ViewPreloadSizeProvider
import com.hedvig.app.R
import com.hedvig.app.feature.chat.AttachImageData
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.fadeOut
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import e
import kotlinx.android.synthetic.main.attach_file_image_item.view.*
import kotlinx.android.synthetic.main.camera_and_misc_item.view.*
import kotlinx.android.synthetic.main.loading_spinner.view.*

class AttachFileAdapter(
    private val context: Context,
    private val attachImageData: List<AttachImageData>,
    private val pickerHeight: Int,
    private val takePhoto: () -> Unit,
    private val showUploadFileDialog: () -> Unit,
    private val uploadFile: (Uri) -> Unit
) : RecyclerView.Adapter<AttachFileAdapter.ViewHolder>() {

    private val roundedCornersRadius =
        context.resources.getDimensionPixelSize(R.dimen.attach_file_rounded_corners_radius)

    val recyclerViewPreloader =
        RecyclerViewPreloader(
            Glide.with(context),
            AttachFilePreloadModelProvider(),
            ViewPreloadSizeProvider(),
            10
        )

    var isUploadingTakenPicture: Boolean = false
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        if (viewType == CAMERA_AND_MISC_VIEW_TYPE) {
            ViewHolder.CameraAndMiscViewHolder(parent)
        } else {
            ViewHolder.ImageViewHolder(parent)
        }

    override fun getItemCount() = attachImageData.size + 1

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        when (viewHolder) {
            is ViewHolder.CameraAndMiscViewHolder -> {
                if (isUploadingTakenPicture) {
                    viewHolder.loadingSpinner.show()
                    viewHolder.cameraIcon.remove()
                } else {
                    viewHolder.loadingSpinner.remove()
                    viewHolder.cameraIcon.show()
                }
                viewHolder.cameraButton.setHapticClickListener {
                    takePhoto()
                }
                viewHolder.miscButton.setHapticClickListener {
                    showUploadFileDialog()
                }
            }
            is ViewHolder.ImageViewHolder -> {
                viewHolder.apply {
                    val image = attachImageData[position - 1]
                    val params = attachFileImage.layoutParams
                    val margin =
                        attachFileImage.context.resources.getDimensionPixelSize(R.dimen.base_margin_double) * 2
                    params.height = pickerHeight - margin
                    params.width = pickerHeight - margin
                    attachFileImage.layoutParams = params
                    Glide
                        .with(attachFileImage.context)
                        .load(image.path)
                        .transform(
                            MultiTransformation(
                                CenterCrop(),
                                RoundedCorners(roundedCornersRadius)
                            )
                        )
                        .into(attachFileImage)
                        .clearOnDetach()
                    attachFileSendButton.remove()
                    attachFileSendButton.setHapticClickListener {
                        image.isLoading = true
                        uploadFile(Uri.parse(image.path))
                        attachFileSendButton.fadeOut()
                        loadingSpinner.fadeIn()
                    }
                    if (image.isLoading) {
                        loadingSpinner.show()
                    } else {
                        loadingSpinner.remove()
                    }
                    val outValue = TypedValue()
                    attachFileImageContainer.context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackgroundBorderless,
                        outValue,
                        true
                    )
                    attachFileImageContainer.foreground =
                        attachFileImageContainer.context.compatDrawable(outValue.resourceId)

                    attachFileImageContainer.setHapticClickListener {

                        attachFileSendButton.show()
                        attachFileSendButton.fadeIn(endAction = {
                            attachFileImageContainer.foreground = null
                            attachFileImageContainer.setOnClickListener(null)
                        })
                    }
                }
            }
        }
    }

    fun imageWasUploaded(path: String) {
        val index = attachImageData.indexOfFirst {
            it.path == path
        }

        if (index == -1) {
            e { "Failed to update AttachImageData of: $path" }
            return
        }

        attachImageData[index].isLoading = false
        notifyItemChanged(index + 1)
    }

    override fun getItemViewType(position: Int) =
        if (position == 0) CAMERA_AND_MISC_VIEW_TYPE else IMAGE_VIEW_TYPE

    override fun onViewRecycled(holder: ViewHolder) {
        when (holder) {
            is ViewHolder.ImageViewHolder -> {
                Glide
                    .with(holder.attachFileImage)
                    .clear(holder.attachFileImage)
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class CameraAndMiscViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.camera_and_misc_item,
                parent,
                false
            )
        ) {
            val cameraButton: FrameLayout = itemView.cameraButton
            val cameraIcon: ImageView = itemView.cameraIcon
            val loadingSpinner: ProgressBar = itemView.loadingSpinner
            val miscButton: FrameLayout = itemView.miscButton
        }

        class ImageViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.attach_file_image_item,
                parent,
                false
            )
        ) {
            val attachFileImage: ImageView = itemView.attachFileImage
            val attachFileImageContainer: FrameLayout = itemView.attachFileContainer
            val attachFileSendButton: Button = itemView.attachFileSendButton
            val loadingSpinner: ProgressBar = itemView.loadingSpinner
        }
    }

    inner class AttachFilePreloadModelProvider :
        ListPreloader.PreloadModelProvider<AttachImageData> {
        override fun getPreloadItems(position: Int): List<AttachImageData> =
            attachImageData.getOrNull(position)?.let { listOf(it) } ?: emptyList()

        override fun getPreloadRequestBuilder(item: AttachImageData): RequestBuilder<*>? =
            Glide
                .with(context)
                .load(item.path)
                .transform(MultiTransformation(CenterCrop(), RoundedCorners(roundedCornersRadius)))
    }

    companion object {
        private const val CAMERA_AND_MISC_VIEW_TYPE = 0
        private const val IMAGE_VIEW_TYPE = 1
    }
}

