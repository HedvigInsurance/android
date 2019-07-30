package com.hedvig.app.feature.dismissablepager

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.hedvig.app.R
import com.hedvig.app.util.extensions.screenWidth
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.fragment_dismissable_pager.*

abstract class DismissablePager : androidx.fragment.app.DialogFragment() {
    abstract val items: List<DismissablePagerPage>
    abstract val tracker: DismissablePageTracker

    @get:StringRes
    abstract val proceedLabel: Int
    @get:StringRes
    abstract val dismissLabel: Int
    @get:StyleRes
    abstract val animationStyle: Int
    @get:StringRes
    abstract val titleLabel: Int?

    abstract fun onDismiss()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        dialog?.window?.setWindowAnimations(animationStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_dismissable_pager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        close.setOnClickListener {
            onDismiss()
            dialog?.dismiss()
        }

        titleLabel?.let { tl ->
            title.text = resources.getString(tl)
            title.show()
        }

        pager.adapter = DismissablePagerAdapter(childFragmentManager, items)
        pagerIndicator.pager = pager
        proceed.text = if (items.size > 1) {
            resources.getString(proceedLabel)
        } else {
            resources.getString(dismissLabel)
        }
        pager.addOnPageChangeListener(PageChangeListener())
        proceed.setHapticClickListener {
            tracker.clickProceed()
            pager.currentItem += 1
        }
    }

    inner class PageChangeListener : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(p0: Int) {}

        override fun onPageScrolled(position: Int, offsetPercentage: Float, offsetPixels: Int) {
            pager.adapter?.count?.let { count ->
                if (position == count - 2) {
                    newsContainer.alpha = 1.0f - offsetPercentage
                    val translation = -(screenWidth * offsetPercentage)
                    proceed.translationX = translation
                    topBar.translationX = translation
                    pagerIndicator.translationX = translation
                }
                if (position == count - 1 && offsetPercentage == 0f) {
                    onDismiss()
                    dialog?.dismiss()
                }
            }
        }

        override fun onPageSelected(page: Int) {
            pager.adapter?.count?.let { count ->
                proceed.text = if (isPositionLast(page, count) || isPositionNextToLast(page, count)) {
                    resources.getString(dismissLabel)
                } else {
                    resources.getString(proceedLabel)
                }
            }
        }
    }
}

