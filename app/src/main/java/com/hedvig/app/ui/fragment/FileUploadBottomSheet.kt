package com.hedvig.app.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FileUploadDialogBinding
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

abstract class FileUploadBottomSheet : BottomSheetDialogFragment() {
    abstract fun onFileChosen(uri: Uri)

    @get:StringRes
    abstract val title: Int

    protected val binding by viewBinding(FileUploadDialogBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.file_upload_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            header.text = requireContext().getString(title)
            uploadImageOrVideo.setHapticClickListener {
                selectImageFromLibrary()
            }
            uploadFile.setHapticClickListener {
                selectFile()
            }
        }
    }

    protected fun uploadStarted() {
        binding.apply {
            header.text = resources.getString(R.string.FILE_UPLOAD_IS_UPLOADING)
            loadingSpinner.show()
            uploadImageOrVideo.remove()
            uploadFile.remove()
            isCancelable = false
        }
    }

    private fun selectImageFromLibrary() {
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ),
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

    companion object {
        private const val SELECT_FILE_REQUEST_CODE = 42
        private const val SELECT_IMAGE_REQUEST_CODE = 43
    }
}
