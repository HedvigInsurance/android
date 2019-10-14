package com.hedvig.app.ui.view

import android.content.Context
import android.graphics.Outline
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable

open class HedvigCardView : FrameLayout {
    private var attributeSet: AttributeSet? = null
    private var defStyle: Int = 0

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        this.attributeSet = attributeSet
        initialize()
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        this.attributeSet = attributeSet
        this.defStyle = defStyle
        initialize()
    }

    private fun initialize() {
        background = context.compatDrawable(R.drawable.hedvig_card_view)

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
