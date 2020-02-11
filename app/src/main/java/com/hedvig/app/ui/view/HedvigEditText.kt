package com.hedvig.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.hedvig_edit_text.view.*

class HedvigEditText @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.hedvig_edit_text, this, true)

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.HedvigEditText)

        val color = typedArray.getColor(
            R.styleable.HedvigEditText_hintBackground,
            context.compatColor(R.color.background)
        )
        val inputType = typedArray.getInt(R.styleable.HedvigEditText_android_inputType, 0)
        val imeOption = typedArray.getInt(R.styleable.HedvigEditText_android_imeOptions, 0)
        val hintText = typedArray.getString(R.styleable.HedvigEditText_hintText)
        textInput.inputType = inputType
        textInput.imeOptions = imeOption
        textHint.setBackgroundColor(color)
        textHint.text = hintText

        typedArray.recycle()

        textInput.setOnClickListener {
            animateHint()
        }

        textInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                animateHint()
            }
        }
    }

    private fun animateHint() {
        val animateDistance =
            getViewHeight()
                .div(2.0)
                .toFloat()

        textHint.spring(
            SpringAnimation.TRANSLATION_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(-animateDistance)
    }

    private fun getViewHeight(): Int {
        this.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        return this.measuredHeight
    }
}
