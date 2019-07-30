package com.hedvig.app.feature.chat

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.*
import kotlinx.android.synthetic.main.attach_file_image_item.view.*
import kotlinx.android.synthetic.main.camera_and_misc_item.view.*
import kotlinx.android.synthetic.main.loading_spinner.view.*
import timber.log.Timber

class AttachFileAdapter(
    private val attachImageData: List<AttachImageData>,
    private val pickerHeight: Int,
    private val takePhoto: () -> Unit,
    private val showUploadFileDialog: () -> Unit,
    private val uploadFile: (Uri) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<AttachFileAdapter.ViewHolder>() {

    var isUploadingTakenPicture: Boolean = false
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        if (viewType == CAMERA_AND_MISC_VIEW_TYPE) {
            ViewHolder.CameraAndMiscViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.camera_and_misc_item,
                    parent,
                    false
                )
            )
        } else {
            ViewHolder.ImageViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.attach_file_image_item,
                    parent,
                    false
                )
            )
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
                    val margin = attachFileImage.context.resources.getDimensionPixelSize(R.dimen.base_margin_double) * 2
                    val roundedCornersRadius =
                        attachFileImage.context.resources.getDimensionPixelSize(R.dimen.attach_file_rounded_corners_radius)
                    params.height = pickerHeight - margin
                    params.width = pickerHeight - margin
                    attachFileImage.layoutParams = params
                    Glide
                        .with(attachFileImage.context)
                        .load(image.path)
                        .transform(MultiTransformation(CenterCrop(), RoundedCorners(roundedCornersRadius)))
                        .into(attachFileImage)
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
                        attachFileImageContainer.context.getDrawable(outValue.resourceId)

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
            Timber.e("Failed to update AttachImageData of: $path")
            return
        }

        attachImageData[index].isLoading = false
        notifyItemChanged(index + 1)
    }

    override fun getItemViewType(position: Int) =
        if (position == 0) CAMERA_AND_MISC_VIEW_TYPE else IMAGE_VIEW_TYPE

    sealed class ViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        class CameraAndMiscViewHolder(itemView: View) : ViewHolder(itemView) {
            val cameraButton: FrameLayout = itemView.cameraButton
            val cameraIcon: ImageView = itemView.cameraIcon
            val loadingSpinner: LottieAnimationView = itemView.loadingSpinner
            val miscButton: FrameLayout = itemView.miscButton
        }

        class ImageViewHolder(itemView: View) : ViewHolder(itemView) {
            val attachFileImage: ImageView = itemView.attachFileImage
            val attachFileImageContainer: FrameLayout = itemView.attachFileContainer
            val attachFileSendButton: Button = itemView.attachFileSendButton
            val loadingSpinner: LottieAnimationView = itemView.loadingSpinner
        }
    }

    companion object {
        private const val CAMERA_AND_MISC_VIEW_TYPE = 0
        private const val IMAGE_VIEW_TYPE = 1
    }
}

