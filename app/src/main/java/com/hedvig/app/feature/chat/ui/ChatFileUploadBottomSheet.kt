package com.hedvig.app.feature.chat.ui

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.ui.fragment.FileUploadBottomSheet
import com.hedvig.app.util.extensions.observe
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ChatFileUploadBottomSheet : FileUploadBottomSheet() {
    override val title = R.string.FILE_UPLOAD_OVERLAY_TITLE
    private val chatViewModel: ChatViewModel by sharedViewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        setupSubscriptions()

        return dialog
    }

    override fun onFileChosen(uri: Uri) {
        chatViewModel.uploadFileFromProvider(uri)
    }

    private fun setupSubscriptions() {
        chatViewModel.isUploading.observe(lifecycleOwner = this) { isUploading ->
            isUploading?.let { iu ->
                if (iu) {
                    uploadStarted()
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

    companion object {
        const val TAG = "ChatFileUploadBottomSheet"

        fun newInstance() = ChatFileUploadBottomSheet()
    }
}
