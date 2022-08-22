package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import android.os.Bundle
import android.view.View
import com.hedvig.app.ui.fragment.FileUploadBottomSheet
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ReceiptFileUploadBottomSheet : FileUploadBottomSheet() {
  private val viewModel: KeyGearItemDetailViewModel by sharedViewModel()

  override val title = hedvig.resources.R.string.KEY_GEAR_RECEIPT_UPLOAD_SHEET_TITLE

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel.isUploading.observe(viewLifecycleOwner) { isUploading ->
      if (isUploading) {
        uploadStarted()
      } else {
        dismiss()
      }
    }
  }

  override fun onFileChosen(uri: Uri) {
    viewModel.uploadReceipt(uri)
  }

  companion object {
    const val TAG = "ReceiptFileUploadBottomSheet"

    fun newInstance() = ReceiptFileUploadBottomSheet()
  }
}
