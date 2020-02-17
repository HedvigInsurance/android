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
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.onChange
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.hedvig_edit_text.view.*

class HedvigEditText @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.hedvig_edit_text, this, true)

        clipChildren = false
        clipToPadding = false
        updatePadding(top = BASE_MARGIN)

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

    fun getText() = textInput.text.toString()

    fun setOnChangeListener(action: () -> Unit) {
        textInput.onChange {
            action()
        }
    }

    private fun animateHint() {
        val animateDistance =
            (getViewHeight() - BASE_MARGIN) / 2f

        textHint.spring(
            SpringAnimation.TRANSLATION_Y,
            SpringForce.STIFFNESS_HIGH,
            SpringForce.DAMPING_RATIO_NO_BOUNCY
        ).animateToFinalPosition(-animateDistance)
    }

    private fun getViewHeight(): Int {
        this.measure(
            MeasureSpec.makeMeasureSpec((parent as View).width, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec((parent as View).height, MeasureSpec.AT_MOST)
        )
        return this.measuredHeight
    }
}
