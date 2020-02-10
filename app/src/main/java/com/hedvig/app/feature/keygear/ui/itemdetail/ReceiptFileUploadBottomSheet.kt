package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import com.hedvig.app.R
import com.hedvig.app.ui.fragment.FileUploadBottomSheet
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ReceiptFileUploadBottomSheet : FileUploadBottomSheet() {
    private val model: KeyGearItemDetailViewModel by sharedViewModel()

    // TODO: Replace with translation
    override val title = R.string.receipt_bottom_sheet_title

    override fun onFileChosen(uri: Uri) {
        // TODO: Handle the uploading-state
        model.uploadReceipt(uri)
        dialog?.dismiss()
    }

    companion object {
        const val TAG = "ReceiptFileUploadBottomSheet"

        fun newInstance() = ReceiptFileUploadBottomSheet()
    }
}
