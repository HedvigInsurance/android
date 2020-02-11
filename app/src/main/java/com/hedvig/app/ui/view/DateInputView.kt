package com.hedvig.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.dynamicanimation.animation.SpringAnimation
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.date_pick_layout.view.*

class DateInputView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.date_pick_layout, this, true)

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DateInputView)

        val color = typedArray.getColor(
            R.styleable.DateInputView_dateHintBackground,
            context.compatColor(R.color.background)
        )
        setHintBackgroundColor(color)
        typedArray.recycle()
    }

    var text: String? = null
        set(value) {
            field = value
            animateHint()
            dateText.show()
            dateText.text = text
        }

    private fun animateHint() {
        val animateDistance =
            getViewHeight()
                .div(2.0)
                .toFloat()

        dateHint.spring(
            SpringAnimation.TRANSLATION_Y,
            10000.0f,
            1.0f
        ).animateToFinalPosition(-animateDistance)
    }

    private fun setHintBackgroundColor(backgroundColorId: Int) {
        dateHint.setBackgroundColor(backgroundColorId)
    }

    private fun getViewHeight(): Int {
        this.show()
        this.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        return this.measuredHeight
    }
}
