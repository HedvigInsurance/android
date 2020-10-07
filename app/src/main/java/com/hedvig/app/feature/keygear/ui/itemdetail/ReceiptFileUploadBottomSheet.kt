package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.observe
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.FileUploadBottomSheet
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ReceiptFileUploadBottomSheet : FileUploadBottomSheet() {
    private val model: KeyGearItemDetailViewModel by sharedViewModel()

    override val title = R.string.KEY_GEAR_RECEIPT_UPLOAD_SHEET_TITLE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.isUploading.observe(viewLifecycleOwner) { isUploading ->
            if (isUploading) {
                uploadStarted()
            } else {
                dismiss()
            }
        }
    }

    override fun onFileChosen(uri: Uri) {
        model.uploadReceipt(uri)
    }

    companion object {
        const val TAG = "ReceiptFileUploadBottomSheet"

        fun newInstance() = ReceiptFileUploadBottomSheet()
    }
}
