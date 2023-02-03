package com.hedvig.app.feature.chat.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.databinding.CameraAndMiscItemBinding
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show

class AttachFileAdapter(
  private val takePhoto: () -> Unit,
  private val showUploadFileDialog: () -> Unit,
) : RecyclerView.Adapter<AttachFileAdapter.CameraAndMiscViewHolder>() {

  var isUploadingTakenPicture: Boolean = false
    set(value) {
      field = value
      notifyItemChanged(0)
    }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CameraAndMiscViewHolder {
    return CameraAndMiscViewHolder(
      CameraAndMiscItemBinding.inflate(
        LayoutInflater.from(parent.context),
        parent,
        false,
      ),
    )
  }

  override fun getItemCount() = 1

  override fun onBindViewHolder(viewHolder: CameraAndMiscViewHolder, position: Int) {
    viewHolder.bind(isUploadingTakenPicture, takePhoto, showUploadFileDialog)
  }

  override fun onViewRecycled(holder: CameraAndMiscViewHolder) {
    val itemView = holder.itemView
    if (itemView is ComposeView) {
      itemView.disposeComposition()
    }
  }

  class CameraAndMiscViewHolder(
    val binding: CameraAndMiscItemBinding,
  ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(
      isUploadingTakenPicture: Boolean,
      takePhoto: () -> Unit,
      showUploadFileDialog: () -> Unit,
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
}
