package com.hedvig.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.R
import com.hedvig.app.databinding.DatePickLayoutBinding
import com.hedvig.app.util.extensions.colorAttr
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.util.spring

class DateInputView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {
    private val binding by viewBinding(DatePickLayoutBinding::bind)

    init {
        LayoutInflater.from(context).inflate(R.layout.date_pick_layout, this, true)

        clipChildren = false
        clipToPadding = false
        updatePadding(top = BASE_MARGIN)

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.DateInputView)

        val color = typedArray.getColor(
            R.styleable.DateInputView_dateHintBackground,
            context.colorAttr(android.R.attr.colorBackground)
        )
        setHintBackgroundColor(color)
        typedArray.recycle()
    }

    var text: String? = null
        set(value) {
            field = value
            animateHint()
            binding.dateText.show()
            binding.dateText.text = text
        }

    private fun animateHint() {
        val animateDistance =
            (getViewHeight() - BASE_MARGIN) / 2f

        binding.dateHint.spring(
            SpringAnimation.TRANSLATION_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(-animateDistance)
    }

    private fun setHintBackgroundColor(backgroundColorId: Int) {
        binding.dateHint.setBackgroundColor(backgroundColorId)
    }

    private fun getViewHeight(): Int {
        this.measure(
            MeasureSpec.makeMeasureSpec((parent as View).width, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec((parent as View).height, MeasureSpec.AT_MOST)
        )
        return this.measuredHeight
    }
}
