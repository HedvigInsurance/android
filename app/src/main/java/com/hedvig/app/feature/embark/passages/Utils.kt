package com.hedvig.app.feature.embark.passages

import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import com.hedvig.app.util.boundedProgress
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun animateResponse(responseView: TextView, responseText: String) = suspendCancellableCoroutine<Unit> { continuation ->
    responseView.text = responseText
    responseView.show()
    val initialTranslation = responseView.translationY

    responseView
        .spring(DynamicAnimation.TRANSLATION_Y)
        .takeIf { !it.isRunning }
        ?.addUpdateListener { _, value, _ ->
            responseView.alpha = boundedProgress(initialTranslation, 0f, value)
        }
        ?.addEndListener { _, _, _, _ ->
            if (continuation.isActive) {
                continuation.resume(Unit) {

                }
            }
        }
        ?.animateToFinalPosition(0f)

    continuation.invokeOnCancellation {
        responseView.spring(DynamicAnimation.TRANSLATION_Y).cancel()
    }
}
