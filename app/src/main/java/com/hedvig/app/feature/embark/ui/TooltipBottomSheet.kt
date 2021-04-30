package com.hedvig.app.feature.embark.ui

import android.os.Bundle
import android.os.Parcelable
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.TooltipBottomSheetBinding
import com.hedvig.app.feature.embark.TooltipModel
import com.hedvig.app.util.boundedLerp
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.dp
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import e
import kotlinx.android.parcel.Parcelize

class TooltipBottomSheet(private val windowManager: WindowManager) : BottomSheetDialogFragment() {
    private val binding by viewBinding(TooltipBottomSheetBinding::bind)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.tooltip_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tooltips = requireArguments().getParcelableArrayList<Tooltip>(TOOLTIPS)
        if (tooltips == null) {
            e { "Programmer error: no tooltips passed to ${this::class.java.name}" }
            return
        }
        binding.apply {
            recycler.adapter = TooltipBottomSheetAdapter().also { adapter ->
                adapter.submitList(
                    if (tooltips.size == 1) {
                        listOf(
                            TooltipModel.Header(tooltips.first().title),
                            *getTooltipsWithoutTitles(tooltips)
                        )
                    } else {
                        listOf(
                            TooltipModel.Header(),
                            *getTooltipsWithTitles(tooltips)
                        )
                    }
                )
            }

            if (tooltips.size > 1) {
                val defaultStatusBarColor = dialog?.window?.statusBarColor
                val defaultSystemUiVisibility = dialog?.window?.decorView?.systemUiVisibility
                close.alpha = 0f
                (dialog as? BottomSheetDialog)?.behavior?.let { behaviour ->

                    val displayMetrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(displayMetrics)
                    val windowHeight = displayMetrics.heightPixels

                    recycler.measure(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    val sheetContentHeight =
                        recycler.measuredHeight + recycler.marginTop + recycler.marginBottom + resources.getDimension(R.dimen.peril_bottom_sheet_close_icon_size)
                            .toInt().dp
                    val shouldPeekAtContentHeight = sheetContentHeight < windowHeight
                    val defaultPeekHeight = 295.dp
                    if (shouldPeekAtContentHeight) {
                        behaviour.setPeekHeight(windowHeight, true)
                        chevronContainer.remove()
                    } else {
                        behaviour.setPeekHeight(defaultPeekHeight, true)
                        chevronContainer.show()
                    }
                    if (!shouldPeekAtContentHeight) {
                        chevronContainer.measure(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        )
                        val chevronContainerHeight = chevronContainer.measuredHeight
                        val startTranslation = (defaultPeekHeight - chevronContainerHeight).toFloat()
                        chevronContainer.translationY = startTranslation
                        behaviour.addBottomSheetCallback(
                            object : BottomSheetBehavior.BottomSheetCallback() {

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
                    }
                    chevron.setHapticClickListener {
                        close.show()
                        behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
            close.setHapticClickListener {
                this@TooltipBottomSheet.dismiss()
            }
        }
    }

    private fun getNavBarHeight(): Int {
        val resources = requireContext().resources
        val resourceId: Int = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    companion object {
        private const val TOOLTIPS = "TOOLTIPS"

        val TAG: String = TooltipBottomSheet::class.java.name
        fun newInstance(tooltips: List<EmbarkStoryQuery.Tooltip>, windowManager: WindowManager) =
            TooltipBottomSheet(windowManager).apply {
                val parcelableTooltips = mutableListOf<Tooltip>()
                tooltips.forEach {
                    parcelableTooltips.add(
                        Tooltip(
                            title = it.title,
                            description = it.description
                        )
                    )
                }
                arguments = bundleOf(TOOLTIPS to parcelableTooltips)
            }

        fun getTooltipsWithTitles(list: List<Tooltip>) =
            list.map { TooltipModel.Tooltip.TooltipWithTitle(it.title, it.description) }.toTypedArray()

        fun getTooltipsWithoutTitles(list: List<Tooltip>) =
            list.map { TooltipModel.Tooltip.TooltipWithOutTitle(it.description) }.toTypedArray()
    }
}

@Parcelize
data class Tooltip(val title: String, val description: String) : Parcelable
