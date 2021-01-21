package com.hedvig.app.feature.chat.ui

import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.hedvig.app.databinding.AttachFileImageItemBinding
import com.hedvig.app.databinding.CameraAndMiscItemBinding
import com.hedvig.app.feature.chat.AttachImageData
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.fadeOut
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import e

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
                viewHolder.bind(isUploadingTakenPicture, takePhoto, showUploadFileDialog)
            }
            is ViewHolder.ImageViewHolder -> {
                viewHolder.bind(attachImageData, pickerHeight, roundedCornersRadius, uploadFile)
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
                    .with(holder.binding.attachFileImage)
                    .clear(holder.binding.attachFileImage)
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
            val binding by viewBinding(CameraAndMiscItemBinding::bind)
            fun bind(
                isUploadingTakenPicture: Boolean,
                takePhoto: () -> Unit,
                showUploadFileDialog: () -> Unit
            ) {
                binding.apply {
                    if (isUploadingTakenPicture) {
                        loadingSpinner.root.show()
                        cameraIcon.remove()
                    } else {
                        loadingSpinner.root.remove()
                        cameraIcon.show()
                    }
                    cameraButton.setHapticClickListener {
                        takePhoto()
                    }
                    miscButton.setHapticClickListener {
                        showUploadFileDialog()
                    }
                }
            }
        }

        class ImageViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.attach_file_image_item,
                parent,
                false
            )
        ) {
            val binding by viewBinding(AttachFileImageItemBinding::bind)
            fun bind(
                attachImageData: List<AttachImageData>,
                pickerHeight: Int,
                roundedCornersRadius: Int,
                uploadFile: (Uri) -> Unit
            ) {
                binding.apply {
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
                        loadSpinner.root.fadeIn()
                    }
                    if (image.isLoading) {
                        loadSpinner.root.show()
                    } else {
                        loadSpinner.root.remove()
                    }
                    val outValue = TypedValue()
                    attachFileContainer.context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackgroundBorderless,
                        outValue,
                        true
                    )
                    attachFileContainer.foreground =
                        attachFileContainer.context.compatDrawable(outValue.resourceId)

                    attachFileContainer.setHapticClickListener {

                        attachFileSendButton.show()
                        attachFileSendButton.fadeIn(
                            endAction = {
                                attachFileContainer.foreground = null
                                attachFileContainer.setOnClickListener(null)
                            }
                        )
                    }
                }
            }
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
