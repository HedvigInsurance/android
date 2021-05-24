package com.hedvig.app.feature.embark.passages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.dynamicanimation.animation.DynamicAnimation
import com.hedvig.app.BASE_MARGIN
import com.hedvig.app.BASE_MARGIN_HALF
import com.hedvig.app.R
import com.hedvig.app.databinding.EmbarkResponseBinding
import com.hedvig.app.databinding.TextBody1Binding
import com.hedvig.app.databinding.TextBody2Binding
import com.hedvig.app.feature.embark.Response
import com.hedvig.app.util.boundedProgress
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.spring
import kotlinx.coroutines.suspendCancellableCoroutine

suspend fun animateResponse(
    binding: EmbarkResponseBinding,
    response: Response,
) = suspendCancellableCoroutine<Unit> { continuation ->
    binding.root.removeAllViews()
    val layoutInflater = LayoutInflater.from(binding.root.context)
    when (response) {
        is Response.SingleResponse -> {
            TextBody1Binding.inflate(layoutInflater, binding.root, true).apply {
                root.text = response.text
            }
        }
        is Response.GroupedResponse -> {
            response.title?.let { title ->
                TextBody1Binding.inflate(layoutInflater, binding.root, true).root.apply {
                    text = title
                }
            }
            response.groups.forEach { group ->
                TextBody2Binding.inflate(layoutInflater, binding.root, true).root.apply {
                    updatePaddingRelative(
                        start = BASE_MARGIN,
                        top = BASE_MARGIN_HALF,
                        end = BASE_MARGIN,
                        bottom = BASE_MARGIN_HALF
                    )
                    updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = BASE_MARGIN
                    }
                    setBackgroundResource(R.drawable.background_rounded_corners_surface_6dp)
                    text = group
                }
            }
        }
    }
    binding.root.show()
    val initialTranslation = binding.root.translationY

    binding.root
        .spring(DynamicAnimation.TRANSLATION_Y)
        .takeIf { !it.isRunning }
        ?.addUpdateListener { _, value, _ ->
            binding.root.alpha = boundedProgress(initialTranslation, 0f, value)
        }
        ?.addEndListener { _, _, _, _ ->
            if (continuation.isActive) {
                continuation.resume(Unit) {
                }
            }
        }
        ?.animateToFinalPosition(0f)

    continuation.invokeOnCancellation {
        binding.root.spring(DynamicAnimation.TRANSLATION_Y).cancel()
    }
}
