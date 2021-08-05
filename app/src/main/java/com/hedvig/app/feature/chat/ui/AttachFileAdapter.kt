package com.hedvig.app.feature.chat.ui

import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.clear
import coil.load
import coil.size.Scale
import coil.transform.RoundedCornersTransformation
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
    context: Context,
    private val attachImageData: List<AttachImageData>,
    private val pickerHeight: Int,
    private val takePhoto: () -> Unit,
    private val showUploadFileDialog: () -> Unit,
    private val uploadFile: (Uri) -> Unit
) : RecyclerView.Adapter<AttachFileAdapter.ViewHolder>() {

    private val roundedCornersRadius =
        context.resources.getDimensionPixelSize(R.dimen.attach_file_rounded_corners_radius).toFloat()

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
        when     (holder) {
            is ViewHolder.ImageViewHolder -> holder.binding.attachFileImage.clear()
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
                roundedCornersRadius: Float,
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
                    attachFileImage.load(image.path) {
                        transformations(RoundedCornersTransformation(roundedCornersRadius))
                        scale(Scale.FILL)
                    }
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

    companion object {
        private const val CAMERA_AND_MISC_VIEW_TYPE = 0
        private const val IMAGE_VIEW_TYPE = 1
    }
}
