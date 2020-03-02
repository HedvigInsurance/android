package com.hedvig.app.feature.keygear.ui.itemdetail

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.FileUploadBottomSheet
import com.hedvig.app.util.extensions.observe
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ReceiptFileUploadBottomSheet : FileUploadBottomSheet() {
    private val model: KeyGearItemDetailViewModel by sharedViewModel()

    override val title = R.string.KEY_GEAR_RECEIPT_UPLOAD_SHEET_TITLE

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        model.isUploading.observe(this) { isUploading ->
            if (isUploading == true) {
                uploadStarted()
            } else if (isUploading == false) {
                dismiss()
            }
        }

        return dialog
    }

    override fun onFileChosen(uri: Uri) {
        model.uploadReceipt(uri)
    }

    companion object {
        const val TAG = "ReceiptFileUploadBottomSheet"

        fun newInstance() = ReceiptFileUploadBottomSheet()
    }
}
