package com.hedvig.app.feature.chat.ui

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils.loadAnimation
import com.hedvig.app.R
import com.hedvig.app.databinding.AttachPickerDialogBinding
import com.hedvig.app.feature.chat.AttachImageData
import com.hedvig.app.util.extensions.view.fadeIn
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.whenApiVersion

class AttachPickerDialog(context: Context) : Dialog(context, R.style.TransparentDialog) {
    private lateinit var binding: AttachPickerDialogBinding

    var pickerHeight = 0

    private var preventDismiss = false
    private var runningDismissAnimation = false

    private lateinit var takePhotoCallback: () -> Unit
    private lateinit var showUploadBottomSheetCallback: () -> Unit
    private lateinit var dismissCallback: (MotionEvent?) -> Unit
    private lateinit var uploadFileCallback: (Uri) -> Unit

    private var dismissMotionEvent: MotionEvent? = null

    init {
        window?.addFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window?.setWindowAnimations(R.style.DialogNoAnimation)
        binding = AttachPickerDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDialogTouchEvents()
        setupWindowsParams()
        setupBottomSheetParams()
    }

    fun initialize(
        takePhotoCallback: () -> Unit,
        showUploadBottomSheetCallback: () -> Unit,
        dismissCallback: (MotionEvent?) -> Unit,
        uploadFileCallback: (Uri) -> Unit
    ) {
        this.takePhotoCallback = takePhotoCallback
        this.showUploadBottomSheetCallback = showUploadBottomSheetCallback
        this.dismissCallback = dismissCallback
        this.uploadFileCallback = uploadFileCallback
    }

    override fun show() {
        super.show()
        dismissMotionEvent = null
        animatePickerSheet(true)
    }

    override fun dismiss() {
        dismissCallback(dismissMotionEvent)
        if (!runningDismissAnimation) {
            preventDismiss = true
            runningDismissAnimation = true
            animatePickerSheet(false)
        }
        if (!preventDismiss) {
            super.dismiss()
        }
    }

    private fun animatePickerSheet(show: Boolean) {
        // maybe we should create a better animation but this is something
        val animation =
            loadAnimation(context, if (show) R.anim.slide_in_up else R.anim.slide_out_down)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                if (!show) {
                    preventDismiss = false
                    dismiss()
                    runningDismissAnimation = false
                }
            }

            override fun onAnimationRepeat(animation: Animation?) = Unit
            override fun onAnimationStart(animation: Animation?) = Unit
        })
        binding.attachPickerBottomSheet.startAnimation(animation)
    }

    private fun setupWindowsParams() = window?.let { window ->
        val params = window.attributes

        params.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT

            dimAmount = 0f
            flags = flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()

            flags = flags or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS

            whenApiVersion(Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        window.attributes = params
    }

    private fun setupBottomSheetParams() {
        val params = binding.attachPickerBottomSheet.layoutParams
        params.height = pickerHeight
        binding.attachPickerBottomSheet.layoutParams = params
    }

    fun setImages(images: List<String>) {
        val adapter = AttachFileAdapter(
            context,
            images.map { AttachImageData(it) },
            pickerHeight,
            takePhotoCallback,
            showUploadBottomSheetCallback,
            uploadFileCallback
        )
        binding.apply {
            attachFileRecyclerView.adapter = adapter
            attachFileRecyclerView.fadeIn()
            loadingSpinner.loadingSpinner.remove()
        }
    }

    fun imageWasUploaded(path: String) {
        val fileAdapter = binding.attachFileRecyclerView.adapter as? AttachFileAdapter
            ?: return
        fileAdapter.imageWasUploaded(path)
    }

    private fun setupDialogTouchEvents() {
        binding.attachPickerRoot.setOnTouchListener { _, event ->
            dismissMotionEvent = event
            dismiss()
            false
        }
        // prevent dismiss in this area
        binding.attachPickerBottomSheet.setOnTouchListener { _, _ -> true }
    }

    fun uploadingTakenPicture(isUploading: Boolean) {
        val fileAdapter = binding.attachFileRecyclerView.adapter as AttachFileAdapter?
            ?: run { return }
        fileAdapter.isUploadingTakenPicture = isUploading
    }
}
