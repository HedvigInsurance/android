package com.hedvig.app.feature.chat.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.hedvig.app.R
import com.hedvig.app.feature.chat.viewmodel.ChatViewModel
import com.hedvig.app.ui.fragment.FileUploadBottomSheet
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ChatFileUploadBottomSheet : FileUploadBottomSheet() {
    override val title = R.string.FILE_UPLOAD_OVERLAY_TITLE
    private val chatViewModel: ChatViewModel by sharedViewModel()

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
    }

    override fun onFileChosen(uri: Uri) {
        chatViewModel.uploadFileFromProvider(uri)
    }

    companion object {
        const val TAG = "ChatFileUploadBottomSheet"

        fun newInstance() = ChatFileUploadBottomSheet()
    }
}
