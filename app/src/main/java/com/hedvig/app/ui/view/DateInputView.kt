package com.hedvig.app.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.dynamicanimation.animation.SpringAnimation
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring
import kotlinx.android.synthetic.main.date_pick_layout.view.*
import timber.log.Timber

class DateInputView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attributeSet, defStyle) {
    override fun onFinishInflate() {
        super.onFinishInflate()
        Timber.d("inflated ${dateText.text}")
    }

    var text: String? = null
        set(value) {
            field = value
            animateHint()
            dateText.show()
            dateText.text = text
        }

    fun noAnimation() {
        val animateDistance = this
            .height.div(2.5)
            .toFloat()

        dateHint.translationY = -animateDistance
    }

    private fun animateHint() {
        val animateDistance = this
            .height.div(2.5)
            .toFloat()

        dateHint.spring(
            SpringAnimation.TRANSLATION_Y,
            10000.0f,
            1.0f
        ).animateToFinalPosition(-animateDistance)
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.date_pick_layout, this, true)
    }
}
