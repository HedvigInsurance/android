package com.hedvig.app.feature.embark.passages

import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import com.hedvig.app.util.boundedProgress
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring

fun animateResponse(responseView: TextView, responseText: String, done: () -> Unit) {
    responseView.text = responseText
    responseView.show()
    val initialTranslation = responseView.translationY

    responseView
        .spring(DynamicAnimation.TRANSLATION_Y)
        .addUpdateListener { _, value, _ ->
            responseView.alpha = boundedProgress(initialTranslation, 0f, value)
        }
        .addEndListener { _, _, _, _ ->
            done()
        }
        .animateToFinalPosition(0f)
}
