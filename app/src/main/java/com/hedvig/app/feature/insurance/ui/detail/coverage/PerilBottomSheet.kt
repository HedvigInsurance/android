package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.databinding.PerilBottomSheetBinding
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.safeLet
import e
import java.util.ArrayList

class PerilBottomSheet : BottomSheetDialogFragment() {
    private val binding by viewBinding(PerilBottomSheetBinding::bind)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.peril_bottom_sheet, container, false)

    override fun onStart() {
        super.onStart()
        val window = requireActivity().window
        val defaultStatusBarColor = dialog?.window?.statusBarColor
        val defaultSystemUiVisibility = dialog?.window?.decorView?.systemUiVisibility

        binding.apply {
            close.alpha = 0f
            val parentLayout =
                dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { parent ->
                val behaviour = BottomSheetBehavior.from(parent)
                behaviour.setPeekHeight(dpToPx(380f).toInt(), true)
                readMoreContainer.translationY = dpToPx(310f)
                behaviour.addBottomSheetCallback(
                    object : BottomSheetCallback() {

                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            when (newState) {
                                BottomSheetBehavior.STATE_EXPANDED -> {
                                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                                    dialog?.window?.statusBarColor =
                                        requireContext().colorAttr(R.attr.colorSurface)
                                    if (!requireContext().isDarkThemeActive) {
                                        dialog?.window?.decorView?.systemUiVisibility =
                                            window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                    }

                                    close.setHapticClickListener {
                                        this@PerilBottomSheet.dismiss()
                                    }
                                    readMoreContainer.remove()
                                }
                                STATE_DRAGGING -> {
                                    defaultStatusBarColor?.let {
                                        dialog?.window?.statusBarColor = it
                                    }
                                    defaultSystemUiVisibility?.let {
                                        dialog?.window?.decorView?.systemUiVisibility = it
                                    }
                                    dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                                    //defaultStatusBarColor?.let { window.statusBarColor = it }
                                    readMoreContainer.show()
                                }
                                BottomSheetBehavior.STATE_COLLAPSED -> {
                                    close.setOnClickListener(null)
                                }
                            }
                        }

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            readMoreContainer.alpha = 1 - slideOffset
                            close.alpha = slideOffset
                        }
                    })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            val title = requireArguments().getString(TITLE)
            val description = requireArguments().getString(DESCRIPTION)
            val iconUrl = requireArguments().getString(ICON_URL)
            val exception = requireArguments().getStringArrayList(EXCEPTIONS)
            val covered = requireArguments().getStringArrayList(COVERED)
            val info = requireArguments().getString(INFO)

            if (title == null || description == null || iconUrl == null) {
                e { "Programmer error: Missing either TITLE, BODY or ICON_URL in ${this@PerilBottomSheet.javaClass.name}" }
                return
            }

            recycler.adapter = CoveredAndExceptionAdapter().also {
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
            readMoreContainer.setHapticClickListener {
                expandSheet()
            }
        }
    }

    private fun expandSheet() {
        val parentLayout =
            dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        parentLayout?.let { parent ->
            val behaviour = BottomSheetBehavior.from(parent)
            setupFullHeight(parent)
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun dpToPx(dp: Float) =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            resources.displayMetrics
        )

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun expandedList(
        title: String,
        description: String,
        info: String,
        covered: ArrayList<String>,
        exceptions: ArrayList<String>,
        iconLink: String
    ) = listOfNotNull(
        CoveredAndExceptionModel.Icon(iconLink),
        CoveredAndExceptionModel.Title(title),
        CoveredAndExceptionModel.Description(description),
        CoveredAndExceptionModel.Header.CoveredHeader,
        *coveredItems(covered).toTypedArray(),
        CoveredAndExceptionModel.Header.ExceptionHeader,
        *exceptionItems(exceptions).toTypedArray(),
        CoveredAndExceptionModel.Header.InfoHeader,
        CoveredAndExceptionModel.Paragraph(info)
    )

    private fun coveredItems(list: ArrayList<String>) = list.map {
        CoveredAndExceptionModel.Covered(it)
    }

    private fun exceptionItems(list: ArrayList<String>) = list.map {
        CoveredAndExceptionModel.Exception(it)
    }

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
