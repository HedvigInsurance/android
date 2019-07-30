package com.hedvig.app.feature.chat

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.RoundedBottomSheetDialogFragment
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.file_upload_dialog.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class UploadBottomSheet : RoundedBottomSheetDialogFragment() {
    val chatViewModel: ChatViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val view = LayoutInflater
            .from(requireContext())
            .inflate(R.layout.file_upload_dialog, null)
        dialog.setContentView(view)

        dialog.uploadImageOrVideo.setHapticClickListener {
            selectImageFromLibrary()
        }

        dialog.uploadFile.setHapticClickListener {
            selectFile()
        }

        setupSubscriptions()
        return dialog
    }

    private fun setupSubscriptions() {
        chatViewModel.isUploading.observe(lifecycleOwner = this) { isUploading ->
            isUploading?.let { iu ->
                if (iu) {
                    dialog.header.text = resources.getString(R.string.FILE_UPLOAD_IS_UPLOADING)
                    dialog.loadingSpinner.playAnimation()
                    dialog.loadingSpinner.show()
                    dialog.uploadImageOrVideo.remove()
                    dialog.uploadFile.remove()
                    isCancelable = false
                }
            }
        }

        chatViewModel.uploadBottomSheetResponse.observe(lifecycleOwner = this) { data ->
            data?.uploadFile?.key?.let {
                isCancelable = true
                dismiss()
            }
        }
    }

    private fun selectImageFromLibrary() {
        startActivityForResult(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            SELECT_IMAGE_REQUEST_CODE
        )
    }

    private fun selectFile() {
        startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            },
            SELECT_FILE_REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        when (requestCode) {
            SELECT_FILE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    resultData?.data?.let { uri ->
                        chatViewModel.uploadFileFromProvider(uri)
                    }
                }
            }
            SELECT_IMAGE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    resultData?.data?.let { uri ->
                        chatViewModel.uploadFileFromProvider(uri)
                    }
                }
            }
        }
    }

    companion object {
        private const val SELECT_FILE_REQUEST_CODE = 42
        private const val SELECT_IMAGE_REQUEST_CODE = 43
    }
}
