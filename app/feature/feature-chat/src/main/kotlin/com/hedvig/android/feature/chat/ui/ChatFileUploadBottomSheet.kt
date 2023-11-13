package com.hedvig.android.feature.chat.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.core.common.android.remove
import com.hedvig.android.core.common.android.show
import com.hedvig.android.feature.chat.ChatViewModel
import com.hedvig.android.feature.chat.R
import com.hedvig.android.feature.chat.databinding.FileUploadDialogBinding
import com.hedvig.android.feature.chat.legacy.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel

internal class ChatFileUploadBottomSheet : BottomSheetDialogFragment() {
  private val chatViewModel: ChatViewModel by activityViewModel()
  private val binding by viewBinding(FileUploadDialogBinding::bind)

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
    inflater.inflate(R.layout.file_upload_dialog, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    chatViewModel.isUploading.observe(viewLifecycleOwner) { isUploading ->
      if (isUploading) {
        uploadStarted()
      }
    }

    chatViewModel.uploadBottomSheetResponse.observe(viewLifecycleOwner) {
      isCancelable = true
      dismiss()
    }

    binding.apply {
      header.text = requireContext().getString(hedvig.resources.R.string.FILE_UPLOAD_OVERLAY_TITLE)
      uploadImageOrVideo.setHapticClickListener {
        startActivityForResult(
          Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
          ),
          SELECT_IMAGE_REQUEST_CODE,
        )
      }
      uploadFile.setHapticClickListener {
        startActivityForResult(
          Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
          },
          SELECT_FILE_REQUEST_CODE,
        )
      }
    }
  }

  private fun uploadStarted() {
    binding.apply {
      header.text = resources.getString(hedvig.resources.R.string.FILE_UPLOAD_IS_UPLOADING)
      loadingSpinner.show()
      uploadImageOrVideo.remove()
      uploadFile.remove()
      isCancelable = false
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
    when (requestCode) {
      SELECT_FILE_REQUEST_CODE -> {
        if (resultCode == Activity.RESULT_OK) {
          resultData?.data?.let { uri ->
            onFileChosen(uri)
          }
        }
      }
      SELECT_IMAGE_REQUEST_CODE -> {
        if (resultCode == Activity.RESULT_OK) {
          resultData?.data?.let { uri ->
            onFileChosen(uri)
          }
        }
      }
    }
  }

  private fun onFileChosen(uri: Uri) {
    chatViewModel.uploadFileFromProvider(uri)
  }

  companion object {
    private const val SELECT_FILE_REQUEST_CODE = 42
    private const val SELECT_IMAGE_REQUEST_CODE = 43
    const val TAG = "ChatFileUploadBottomSheet"

    fun newInstance() = ChatFileUploadBottomSheet()
  }
}
