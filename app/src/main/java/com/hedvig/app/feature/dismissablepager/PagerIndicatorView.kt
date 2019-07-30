package com.hedvig.app.feature.dismissablepager

import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.setScaleXY
import com.hedvig.app.util.percentageFade
import com.hedvig.app.util.safeLet

class PagerIndicatorView : LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(context, attributeSet, defStyle)

    init {
        gravity = Gravity.CENTER
    }

    private val purple = context.compatColor(R.color.purple)
    private val gray = context.compatColor(R.color.gray)

    var pager: androidx.viewpager.widget.ViewPager? = null
        set(value) {
            field = value
            value?.let { pager ->
                pager.addOnPageChangeListener(PageChangeListener())
                removeAllViews()
                pager.adapter?.count?.let { count ->
                    for (i in 0 until count) {
                        if (isPositionLast(i, count)) {
                            LayoutInflater.from(context).inflate(R.layout.hedvig_logo, this, true)
                        } else {
                            LayoutInflater.from(context).inflate(R.layout.pager_indicator_view, this, true)
                        }
                    }
                }
            }
        }

    inner class PageChangeListener : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(p0: Int) {}

        override fun onPageScrolled(position: Int, offsetPercentage: Float, offsetPixels: Int) {
            safeLet<Int, Int, Unit>(pager?.currentItem, pager?.adapter?.count) { currentItem, count ->
                if (position == currentItem) {
                    if (!isPositionNextToLast(position, count)) {
                        (getChildAt(position + 1) as? ImageView)?.let { expandIndicator(it, offsetPercentage) }
                    }
                    if (!isPositionLast(position, count)) {
                        (getChildAt(position) as? ImageView)?.let { shrinkIndicator(it, offsetPercentage) }
                    }
                } else {
                    if (!isPositionNextToLast(position, count)) {
                        (getChildAt(position + 1) as? ImageView)?.let { shrinkIndicator(it, 1.0f - offsetPercentage) }
                    }
                    (getChildAt(position) as? ImageView?)?.let { expandIndicator(it, 1.0f - offsetPercentage) }
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
        indicator.drawable.mutate().setTint(percentageFade(purple, gray, percentage))
        indicator.setScaleXY(1.5f - percentage / 2)
    }

    private fun expandIndicator(indicator: ImageView, percentage: Float) {
        indicator.drawable.mutate().setTint(percentageFade(gray, purple, percentage))
        indicator.setScaleXY(1.0f + percentage / 2)
    }
}
