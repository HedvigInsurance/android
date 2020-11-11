package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.bumptech.glide.RequestBuilder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.PerilBottomSheetBinding
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import org.koin.android.ext.android.inject

class PerilBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(PerilBottomSheetBinding::bind)
    private val requestBuilder: RequestBuilder<PictureDrawable> by inject()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.peril_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val defaultStatusBarColor = dialog?.window?.statusBarColor
        val defaultSystemUiVisibility = dialog?.window?.decorView?.systemUiVisibility
        val peril = requireArguments().getParcelable<Peril>(PERIL)

        binding.apply {
            close.alpha = 0f
            (dialog as? BottomSheetDialog)?.behavior?.let { behaviour ->
                val peekHeight = 380.dp
                behaviour.setPeekHeight(peekHeight, true)
                chevronContainer.measure(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
                val chevronContainerHeight = chevronContainer.measuredHeight
                val startTranslation = (peekHeight - chevronContainerHeight).toFloat()
                chevronContainer.translationY = startTranslation
                behaviour.addBottomSheetCallback(
                    object : BottomSheetCallback() {

                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            when (newState) {
                                BottomSheetBehavior.STATE_EXPANDED -> {
                                    dialog?.window?.statusBarColor =
                                        requireContext().colorAttr(R.attr.colorSurface)
                                    if (!requireContext().isDarkThemeActive) {
                                        dialog?.window?.decorView?.let {
                                            it.systemUiVisibility =
                                                it.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                        }
                                    }

                                    close.setHapticClickListener {
                                        this@PerilBottomSheet.dismiss()
                                    }
                                }
                                STATE_DRAGGING -> {
                                    defaultStatusBarColor?.let {
                                        dialog?.window?.statusBarColor = it
                                    }
                                    defaultSystemUiVisibility?.let {
                                        dialog?.window?.decorView?.systemUiVisibility = it
                                    }
                                }
                                STATE_COLLAPSED -> {
                                    close.setOnClickListener(null)
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
                    behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            if (peril == null) {
                e { "Programmer error: Missing arguments in ${this@PerilBottomSheet.javaClass.name}" }
                return
            }
            recycler.adapter = PerilAdapter(requestBuilder).also { adapter ->
                adapter.submitList(
                    expandedList(
                        peril.title,
                        peril.description,
                        peril.info,
                        peril.covered,
                        peril.exception,
                        peril.iconUrl
                    )
                )
            }
        }
    }

    private fun expandedList(
        title: String,
        description: String,
        info: String,
        covered: List<String>,
        exceptions: List<String>,
        iconLink: String
    ) = listOf(
        PerilModel.Icon(iconLink),
        PerilModel.Title(title),
        PerilModel.Description(description),
        PerilModel.Header.CoveredHeader,
        *covered.map { PerilModel.PerilList.Covered(it) }.toTypedArray(),
        PerilModel.Header.ExceptionHeader,
        *exceptions.map { PerilModel.PerilList.Exception(it) }.toTypedArray(),
        PerilModel.Header.InfoHeader,
        PerilModel.Paragraph(info)
    )

    companion object {
        private const val PERIL = "PERIL"

        val TAG = PerilBottomSheet::class.java.name

        fun newInstance(context: Context, peril: PerilFragment) = PerilBottomSheet().apply {
            arguments = bundleOf(
                PERIL to Peril(
                    title = peril.title,
                    description = peril.description,
                    iconUrl = "${BuildConfig.BASE_URL}${
                        if (context.isDarkThemeActive) {
                            peril.icon.variants.dark.svgUrl
                        } else {
                            peril.icon.variants.light.svgUrl
                        }
                    }",
                    exception = peril.exceptions,
                    covered = peril.covered,
                    info = peril.info
                )
            )
        }
    }
}
