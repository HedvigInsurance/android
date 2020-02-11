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
import kotlinx.android.synthetic.main.hedvig_edit_text.view.*



class HedvigEditText  @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {

    init {
        LayoutInflater.from(context).inflate(R.layout.hedvig_edit_text, this, true)

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.HedvigEditText)

        val color = typedArray.getColor(
            R.styleable.HedvigEditText_hintBackground,
            context.compatColor(R.color.com_facebook_blue)
        )
        setHintBackgroundColor(color)
        typedArray.recycle()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        textInput.setOnClickListener {
            animateHint()
        }

        textInput.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                animateHint()
            }
        }
    }

    private fun setHintBackgroundColor(backgroundColorId: Int) {
        textHint.setBackgroundColor(backgroundColorId)
    }

    private fun animateHint() {
        val animateDistance =
            getViewHeight()
                .div(2.0)
                .toFloat()

        textHint.spring(
            SpringAnimation.TRANSLATION_Y,
            10000.0f,
            1.0f
        ).animateToFinalPosition(-animateDistance)
    }

    private fun getViewHeight(): Int {
        this.show()
        this.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        return this.measuredHeight
    }
}
