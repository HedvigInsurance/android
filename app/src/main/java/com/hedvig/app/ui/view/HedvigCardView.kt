package com.hedvig.app.ui.view

import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable

open class HedvigCardView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {
    init {
        background = context.compatDrawable(R.drawable.hedvig_card_view_ripple)
        val attributes = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.HedvigCardView,
            defStyle,
            0
        )
        if (!attributes.hasValue(R.styleable.HedvigCardView_android_elevation)) {
            elevation = resources.getDimension(R.dimen.hedvig_card_view_elevation)
        }

        attributes.recycle()

        outlineProvider = OutlineProvider()
    }

    inner class OutlineProvider : ViewOutlineProvider() {
        private val inset: Rect = Rect()

        override fun getOutline(view: View?, outline: Outline?) {
            view?.background?.copyBounds(inset)

            inset.left += resources.getDimensionPixelSize(R.dimen.hedvig_card_view_inset_horizontal)
            inset.right -= resources.getDimensionPixelSize(R.dimen.hedvig_card_view_inset_horizontal)
            inset.top += resources.getDimensionPixelSize(R.dimen.hedvig_card_view_inset_top)
            inset.bottom -= resources.getDimensionPixelSize(R.dimen.hedvig_card_view_inset_bottom)

            outline?.setRoundRect(inset, resources.getDimension(R.dimen.hedvig_card_view_radius))
        }
    }
}
