package com.hedvig.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.setScaleXY

class PagerIndicatorView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attributeSet, defStyle) {
    private val inflater by lazy { LayoutInflater.from(context) }

    private var changeCallback: PageCallback? = null

    var pager: ViewPager2? = null
        set(value) {
            field = value
            value?.let { v ->
                changeCallback?.let { v.unregisterOnPageChangeCallback(it) }
                changeCallback = PageCallback().also { v.registerOnPageChangeCallback(it) }

                removeAllViews()
                val nItems = v.adapter?.itemCount ?: return
                for (_i in 1..nItems) {
                    val indicator = inflater.inflate(R.layout.pager_indicator_view, this, true)
                    (indicator as? ImageView)?.let { image ->
                        image.drawable.mutate().setTint(context.compatColor(R.color.blur_white))
                    }
                }
            }
        }

    inner class PageCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            pager?.currentItem?.let { currentItem ->
                if (position == currentItem) {
                    // We're scrolling forwards
                    (getChildAt(position + 1) as? ImageView)?.let {
                        expandIndicator(
                            it,
                            positionOffset
                        )
                    }
                    (getChildAt(position) as? ImageView)?.let {
                        shrinkIndicator(it, positionOffset)
                    }
                } else {
                    // We're scrolling backwards
                    (getChildAt(position + 1) as? ImageView)?.let {
                        shrinkIndicator(
                            it,
                            1.0f - positionOffset
                        )
                    }
                    (getChildAt(position) as? ImageView)?.let {
                        expandIndicator(it, 1.0f - positionOffset)
                    }
                }
            }
        }
    }

    private fun shrinkIndicator(indicator: ImageView, percentage: Float) {
        indicator.setScaleXY(1.5f - percentage / 2)
    }

    private fun expandIndicator(indicator: ImageView, percentage: Float) {
        indicator.setScaleXY(1.0f + percentage / 2)
    }
}
