package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.os.bundleOf
import com.bumptech.glide.RequestBuilder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.PerilBottomSheetBinding
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import e
import org.koin.android.ext.android.inject
import java.util.ArrayList

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
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val title = requireArguments().getString(TITLE)
        val description = requireArguments().getString(DESCRIPTION)
        val iconUrl = requireArguments().getString(ICON_URL)
        val exception = requireArguments().getStringArrayList(EXCEPTIONS)
        val covered = requireArguments().getStringArrayList(COVERED)
        val info = requireArguments().getString(INFO)

        binding.apply {
            close.alpha = 0f
            (dialog as? BottomSheetDialog)?.behavior?.setPeekHeight(380.dp, true)

            dialog?.setOnShowListener { dialogInterface ->
                val containerLayout =
                    (dialogInterface as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.container)
                val shadow =
                    bottomSheetDialog.layoutInflater.inflate(R.layout.bottom_sheet_shadow, null)
                val chevron = shadow.findViewById<ImageView>(R.id.chevron)
                chevron.setHapticClickListener {
                    (dialog as? BottomSheetDialog)?.behavior?.state =
                        BottomSheetBehavior.STATE_EXPANDED
                }

                shadow.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.BOTTOM
                }
                containerLayout?.addView(shadow)
                (dialog as? BottomSheetDialog)?.behavior?.let { behaviour ->
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
                                    BottomSheetBehavior.STATE_COLLAPSED -> {
                                        close.setOnClickListener(null)
                                    }
                                }
                            }

                            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                                close.alpha = slideOffset
                                shadow.alpha = 1 - slideOffset
                            }
                        })
                }
            }

            if (title == null || description == null || iconUrl == null || exception == null || covered == null || info == null) {
                e { "Programmer error: Missing arguments in ${this@PerilBottomSheet.javaClass.name}" }
                return
            }

            recycler.adapter = PerilAdapter(requestBuilder).also {
                safeLet(
                    title,
                    description,
                    info,
                    covered,
                    exception,
                    iconUrl
                ) { t, d, i, c, e, u ->
                    it.submitList(
                        expandedList(t, d, i, c, e, u)
                    )
                }
            }
        }
    }

    private fun expandedList(
        title: String,
        description: String,
        info: String,
        covered: ArrayList<String>,
        exceptions: ArrayList<String>,
        iconLink: String
    ) = listOf(
        CoveredAndExceptionModel.Icon(iconLink),
        CoveredAndExceptionModel.Title(title),
        CoveredAndExceptionModel.Description(description),
        CoveredAndExceptionModel.Header.CoveredHeader,
        *covered.map { CoveredAndExceptionModel.CommonDenominator.Covered(it) }.toTypedArray(),
        CoveredAndExceptionModel.Header.ExceptionHeader,
        *exceptions.map { CoveredAndExceptionModel.CommonDenominator.Exception(it) }.toTypedArray(),
        CoveredAndExceptionModel.Header.InfoHeader,
        CoveredAndExceptionModel.Paragraph(info)
    )

    companion object {
        private const val TITLE = "TITLE"
        private const val DESCRIPTION = "BODY"
        private const val ICON_URL = "ICON_URL"
        private const val EXCEPTIONS = "EXCEPTIONS"
        private const val COVERED = "COVERED"
        private const val INFO = "INFO"

        val TAG = PerilBottomSheet::class.java.name

        fun newInstance(context: Context, peril: PerilFragment) = PerilBottomSheet().apply {
            arguments = bundleOf(
                TITLE to peril.title,
                DESCRIPTION to peril.description,
                ICON_URL to "${BuildConfig.BASE_URL}${
                    if (context.isDarkThemeActive) {
                        peril.icon.variants.dark.svgUrl
                    } else {
                        peril.icon.variants.light.svgUrl
                    }
                }",
                EXCEPTIONS to peril.exceptions,
                COVERED to peril.covered,
                INFO to peril.info
            )
        }
    }
}
