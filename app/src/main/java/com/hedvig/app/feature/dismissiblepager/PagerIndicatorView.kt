package com.hedvig.app.feature.dismissiblepager

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import com.hedvig.app.R
import com.hedvig.app.util.boundedColorLerp
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setScaleXY
import com.hedvig.app.util.safeLet

class PagerIndicatorView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    )

    init {
        gravity = Gravity.CENTER
    }

    private val colorSelected = context.compatColor(R.color.colorPrimary)
    private val colorDeselected = context.compatColor(R.color.gray)

    var items: List<DismissiblePagerModel> = emptyList()
        set(value) {
            field = value
            removeAllViews()
            items.forEach { item ->
                when (item) {
                    DismissiblePagerModel.SwipeOffScreen -> {
                        inflate(R.layout.hedvig_logo, true)
                    }
                    else -> {
                        inflate(R.layout.pager_indicator_view, true)
                    }
                }
            }
        }

    var pager: androidx.viewpager.widget.ViewPager? = null
        set(value) {
            field = value
            value?.addOnPageChangeListener(PageChangeListener())
        }

    inner class PageChangeListener : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(p0: Int) {}

        override fun onPageScrolled(position: Int, offsetPercentage: Float, offsetPixels: Int) {
            safeLet<Int, Int, Unit>(
                pager?.currentItem,
                pager?.adapter?.count
            ) { currentItem, count ->
                if (position == currentItem) {
                    when (items.last()) {
                        is DismissiblePagerModel.SwipeOffScreen -> {
                            if (!isPositionNextToLast(position, count)) {
                                (getChildAt(position + 1) as? ImageView)?.let {
                                    expandIndicator(
                                        it,
                                        offsetPercentage
                                    )
                                }
                            }
                            if (!isPositionLast(position, count)) {
                                (getChildAt(position) as? ImageView)?.let {
                                    shrinkIndicator(
                                        it,
                                        offsetPercentage
                                    )
                                }
                            }
                        }
                        else -> {
                            (getChildAt(position + 1) as? ImageView)?.let {
                                expandIndicator(
                                    it,
                                    offsetPercentage
                                )
                            }
                            (getChildAt(position) as? ImageView)?.let {
                                shrinkIndicator(
                                    it,
                                    offsetPercentage
                                )
                            }
                        }
                    }
                } else {
                    when (items.last()) {
                        is DismissiblePagerModel.SwipeOffScreen -> {
                            if (!isPositionNextToLast(position, count)) {
                                (getChildAt(position + 1) as? ImageView)?.let {
                                    shrinkIndicator(
                                        it,
                                        1.0f - offsetPercentage
                                    )
                                }
                            }
                        }
                        else -> {
                            (getChildAt(position + 1) as? ImageView)?.let {
                                shrinkIndicator(
                                    it,
                                    1.0f - offsetPercentage
                                )
                            }
                        }
                    }
                    (getChildAt(position) as? ImageView?)?.let {
                        expandIndicator(
                            it,
                            1.0f - offsetPercentage
                        )
                    }
                }
            }
        }

        override fun onPageSelected(position: Int) {
            pager?.adapter?.count?.let { count ->
                if (position != count - 1) {
                    (getChildAt(position) as? ImageView)?.drawable?.mutate()?.state =
                        intArrayOf(android.R.attr.state_active)
                }
            }
        }
    }

    private fun shrinkIndicator(indicator: ImageView, percentage: Float) {
        indicator.drawable.mutate()
            .setTint(boundedColorLerp(colorSelected, colorDeselected, percentage))
        indicator.setScaleXY(1.5f - percentage / 2)
    }

    private fun expandIndicator(indicator: ImageView, percentage: Float) {
        indicator.drawable.mutate()
            .setTint(boundedColorLerp(colorDeselected, colorSelected, percentage))
        indicator.setScaleXY(1.0f + percentage / 2)
    }
}
