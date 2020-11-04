package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.PerilBottomSheetBinding
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.svg.buildRequestBuilder
import e

class PerilBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(PerilBottomSheetBinding::bind)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.peril_bottom_sheet, container, false)

    override fun onStart() {
        super.onStart()
        binding.apply {
            close.setHapticClickListener {
                this@PerilBottomSheet.dismiss()
            }
            val parentLayout =
                dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { parent ->
                val behaviour = BottomSheetBehavior.from(parent)
                behaviour.addBottomSheetCallback(
                    object : BottomSheetCallback() {

                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            when (newState) {
                                BottomSheetBehavior.STATE_EXPANDED -> {
                                    close.show()
                                    readMoreContainer.remove()
                                }
                                BottomSheetBehavior.STATE_DRAGGING -> {
                                }
                                BottomSheetBehavior.STATE_COLLAPSED -> {
                                    close.remove()
                                    readMoreContainer.show()
                                }
                                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                                }
                                BottomSheetBehavior.STATE_HIDDEN -> {
                                    close.remove()
                                }
                                BottomSheetBehavior.STATE_SETTLING -> {

                                }
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        }
                    })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            val titleText = requireArguments().getString(TITLE)
            val bodyText = requireArguments().getString(BODY)
            val iconUrl = requireArguments().getString(ICON_URL)

            if (titleText == null || bodyText == null || iconUrl == null) {
                e { "Programmer error: Missing either TITLE, BODY or ICON_URL in ${this@PerilBottomSheet.javaClass.name}" }
                return
            }

            val requestBuilder = buildRequestBuilder()
            requestBuilder
                .load(iconUrl)
                .into(icon)
            title.text = titleText
            body.text = bodyText

            readMoreContainer.setHapticClickListener {
                val parentLayout =
                    dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

                parentLayout?.let { parent ->
                    val behaviour = BottomSheetBehavior.from(parent)
                    setupFullHeight(parent)
                    behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    companion object {
        private const val TITLE = "TITLE"
        private const val BODY = "BODY"
        private const val ICON_URL = "ICON_URL"
        private const val EXCEPTIONS = "EXCEPTIONS"
        private const val COVERED = "COVERED"

        val TAG = PerilBottomSheet::class.java.name

        fun newInstance(context: Context, peril: PerilFragment) = PerilBottomSheet().apply {
            arguments = bundleOf(
                TITLE to peril.title,
                BODY to peril.description,
                ICON_URL to "${BuildConfig.BASE_URL}${
                    if (context.isDarkThemeActive) {
                        peril.icon.variants.dark.svgUrl
                    } else {
                        peril.icon.variants.light.svgUrl
                    }
                }",
                EXCEPTIONS to peril.exceptions,
                COVERED to peril.covered,
            )
        }
    }
}
