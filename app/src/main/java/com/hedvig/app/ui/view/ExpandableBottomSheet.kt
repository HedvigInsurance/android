package com.hedvig.app.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.ExpandableBottomSheetBinding
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding

private const val PEEK_HEIGHT = 380

open class ExpandableBottomSheet : BottomSheetDialogFragment() {

    val binding by viewBinding(ExpandableBottomSheetBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.expandable_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val defaultStatusBarColor = dialog?.window?.statusBarColor
        val defaultSystemUiVisibility = dialog?.window?.decorView?.systemUiVisibility

        binding.apply {
            close.alpha = 0f

            val peekHeight = PEEK_HEIGHT.dp
            val chevronContainerHeight = chevronContainer.measuredHeight
            val startTranslation = (peekHeight - chevronContainerHeight).toFloat()

            (dialog as? BottomSheetDialog)?.behavior?.let { behaviour ->

                chevronContainer.measure(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                chevronContainer.translationY = startTranslation

                behaviour.setPeekHeight(peekHeight, true)
                behaviour.addBottomSheetCallback(
                    object : BottomSheetBehavior.BottomSheetCallback() {

                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            when (newState) {
                                BottomSheetBehavior.STATE_EXPANDED -> {
                                    dialog?.window?.statusBarColor = requireContext().colorAttr(R.attr.colorSurface)
                                    if (!requireContext().isDarkThemeActive) {
                                        dialog?.window?.decorView?.let {
                                            it.systemUiVisibility = it.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                        }
                                    }
                                }
                                BottomSheetBehavior.STATE_DRAGGING -> {
                                    defaultStatusBarColor?.let {
                                        dialog?.window?.statusBarColor = it
                                    }
                                    defaultSystemUiVisibility?.let {
                                        dialog?.window?.decorView?.systemUiVisibility = it
                                    }
                                    close.show()
                                }
                                BottomSheetBehavior.STATE_COLLAPSED -> {
                                    close.remove()
                                }
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            close.alpha = slideOffset
                            chevronContainer.translationY =
                                boundedLerp(
                                    startTranslation,
                                    (binding.root.height - chevronContainer.height).toFloat(),
                                    slideOffset
                                )
                            binding.root.height
                            chevronContainer.alpha = 1 - slideOffset
                        }
                    })

                chevron.setHapticClickListener {
                    close.show()
                    behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            close.setHapticClickListener {
                dismiss()
            }
        }
    }
}
