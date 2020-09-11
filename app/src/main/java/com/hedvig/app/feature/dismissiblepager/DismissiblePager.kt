package com.hedvig.app.feature.dismissiblepager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentDismissablePagerBinding
import com.hedvig.app.util.extensions.screenWidth
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updateMargin
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import d
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import dev.chrisbanes.insetter.setEdgeToEdgeSystemUiFlags

abstract class DismissiblePager : DialogFragment() {
    abstract val items: List<DismissiblePagerPage>
    abstract val tracker: DismissablePageTracker
    private val binding by viewBinding(FragmentDismissablePagerBinding::bind)

    @get:StringRes
    abstract val proceedLabel: Int

    @get:StringRes
    abstract val lastButtonText: Int

    @get:StyleRes
    abstract val animationStyle: Int

    @get:StringRes
    abstract val titleLabel: Int?

    abstract fun onDismiss()
    abstract fun onLastSwipe()
    abstract fun onLastPageButton()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        dialog?.window?.setWindowAnimations(animationStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        inflater.inflate(R.layout.fragment_dismissable_pager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            root.setEdgeToEdgeSystemUiFlags(true)

            proceed.doOnApplyWindowInsets { view, insets, initialState ->
                view.updateMargin(bottom = initialState.margins.bottom + insets.systemWindowInsetBottom)
            }

            topBar.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(top = initialState.paddings.top + insets.stableInsetTop)
            }

            close.setOnClickListener {
                onDismiss()
                dialog?.dismiss()
            }

            titleLabel?.let { tl ->
                title.text = resources.getString(tl)
                title.show()
            }

            pager.adapter = DismissablePagerAdapter(childFragmentManager, items, requireContext())
            pagerIndicator.pager = pager
            proceed.text = if (items.size > 1) {
                resources.getString(proceedLabel)
            } else {
                resources.getString(lastButtonText)
            }
            pager.addOnPageChangeListener(PageChangeListener())
            proceed.setHapticClickListener {
                tracker.clickProceed()
                pager.currentItem += 1
            }
        }
    }

    inner class PageChangeListener : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(p0: Int) {}

        override fun onPageScrolled(position: Int, offsetPercentage: Float, offsetPixels: Int) {

            binding.apply {
                pager.adapter?.count?.let { count ->
                    if (position == count - 2) {
                        newsContainer.alpha = 1.0f - offsetPercentage
                        val translation = -(screenWidth * offsetPercentage)
                        proceed.translationX = translation
                        topBar.translationX = translation
                        pagerIndicator.translationX = translation
                    }
                    if (isPositionLast(position, count - 1)) {
                        pager.setOnTouchListener(OnTouchListener { v, event -> true })
                        d { "offset: $offsetPixels" }
                    }
                    if (position == count - 1 && offsetPercentage == 0f) {
                        onLastSwipe()
                        dismiss()
                    }
                }
            }
        }

        override fun onPageSelected(page: Int) {
            binding.apply {
                pager.adapter?.count?.let { count ->
                    proceed.text =
                        if (isPositionLast(page, count) || isPositionNextToLast(page, count)) {
                            resources.getString(lastButtonText)
                        } else {
                            resources.getString(proceedLabel)
                        }
                    if (isPositionNextToLast(page, count)) {
                        proceed.setHapticClickListener {
                            onLastPageButton()
                        }
                    }
                }
            }
        }
    }
}

