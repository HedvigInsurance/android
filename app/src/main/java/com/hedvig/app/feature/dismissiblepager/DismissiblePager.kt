package com.hedvig.app.feature.dismissiblepager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentDismissablePagerBinding
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.screenWidth
import com.hedvig.app.util.extensions.view.applyNavigationBarInsets
import com.hedvig.app.util.extensions.view.applyStatusBarInsets
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

abstract class DismissiblePager : DialogFragment() {
    abstract val items: List<DismissiblePagerModel>
    abstract val shouldShowLogo: Boolean
    private val binding by viewBinding(FragmentDismissablePagerBinding::bind)

    @get:StringRes
    abstract val proceedLabel: Int

    @get:StringRes
    abstract val lastButtonText: Int

    @get:StyleRes
    abstract val animationStyle: Int

    @get:StringRes
    abstract val titleLabel: Int?

    @CallSuper
    open fun onDismiss() {
        dismiss()
    }

    @CallSuper
    open fun onLastSwipe() {
        dismiss()
    }

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
        savedInstanceState: Bundle?,
    ): View? =
        inflater.inflate(R.layout.fragment_dismissable_pager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            requireActivity().window.compatSetDecorFitsSystemWindows(false)
            proceed.applyNavigationBarInsets()
            topBar.applyStatusBarInsets()

            pagerIndicator.items = items

            close.setOnClickListener {
                onDismiss()
            }

            titleLabel?.let { tl ->
                title.text = resources.getString(tl)
                title.show()
            }

            pager.adapter = DismissiblePagerAdapter(childFragmentManager, lifecycle, items)
            pagerIndicator.pager = pager
            proceed.text = if (items.size > 1) {
                resources.getString(proceedLabel)
            } else {
                resources.getString(lastButtonText)
            }
            pager.registerOnPageChangeCallback(PageChangeListener())
            proceed.setHapticClickListener {
                pager.currentItem += 1
            }
        }
    }

    inner class PageChangeListener : ViewPager2.OnPageChangeCallback() {

        override fun onPageScrollStateChanged(p0: Int) {}

        override fun onPageScrolled(position: Int, offsetPercentage: Float, offsetPixels: Int) {
            binding.apply {
                pager.adapter?.itemCount?.let { count ->
                    when (items.last()) {
                        is DismissiblePagerModel.SwipeOffScreen -> {
                            if (position == count - 2) {
                                newsContainer.alpha = 1.0f - offsetPercentage
                                val translation = -(screenWidth * offsetPercentage)
                                proceed.translationX = translation
                                topBar.translationX = translation
                                pagerIndicator.translationX = translation
                            }
                            if (position == count - 1 && offsetPercentage == 0f) {
                                onLastSwipe()
                            }
                        }
                        is DismissiblePagerModel.NoTitlePage,
                        is DismissiblePagerModel.TitlePage,
                        -> {
                            // No-op }
                        }
                    }
                }
            }
        }

        override fun onPageSelected(page: Int) {
            binding.apply {
                when (val currentPage = items[page]) {
                    is DismissiblePagerModel.TitlePage -> {
                        proceed.text = currentPage.buttonText
                    }
                    is DismissiblePagerModel.NoTitlePage -> {
                        proceed.text = currentPage.buttonText
                    }
                    DismissiblePagerModel.SwipeOffScreen -> {}
                }

                pager.adapter?.itemCount?.let { count ->
                    when (items.last()) {
                        is DismissiblePagerModel.SwipeOffScreen -> {
                            proceed.text = if (isPositionLast(page, count) || isPositionNextToLast(
                                    page,
                                    count
                                )
                            ) {
                                resources.getString(lastButtonText)
                            } else {
                                resources.getString(proceedLabel)
                            }
                            if (isPositionNextToLast(page, count)) {
                                proceed.setHapticClickListener {
                                    onLastPageButton()
                                }
                            } else {
                                proceed.setHapticClickListener {
                                    pager.currentItem += 1
                                }
                            }
                        }
                        else -> {
                            proceed.text = if (page == count - 1) {
                                resources.getString(lastButtonText)
                            } else {
                                resources.getString(proceedLabel)
                            }
                            if (page == count - 1) {
                                proceed.setHapticClickListener {
                                    onLastPageButton()
                                }
                            } else {
                                proceed.setHapticClickListener {
                                    pager.currentItem += 1
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
