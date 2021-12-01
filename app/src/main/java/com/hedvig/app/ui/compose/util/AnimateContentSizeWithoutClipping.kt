package com.hedvig.app.ui.compose.util

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Direct copy from [androidx.compose.animation.animateContentSize] with the edit of removing the call to
 * [androidx.compose.ui.draw.clipToBounds]
 */
@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.animateContentSizeWithoutClipping(
    animationSpec: FiniteAnimationSpec<IntSize> = spring(),
    finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)? = null
): Modifier = composed(
    inspectorInfo = debugInspectorInfo {
        name = "animateContentSize"
        properties["animationSpec"] = animationSpec
        properties["finishedListener"] = finishedListener
    }
) {
    val scope = rememberCoroutineScope()
    val animModifier = remember(scope) {
        SizeAnimationModifier(animationSpec, scope)
    }
    animModifier.listener = finishedListener
    // Here there was
    // this.clipToBounds().then(animModifier)
    this.then(animModifier)
}

private class SizeAnimationModifier(
    val animSpec: AnimationSpec<IntSize>,
    val scope: CoroutineScope,
) : LayoutModifier {
    var listener: ((startSize: IntSize, endSize: IntSize) -> Unit)? = null

    data class AnimData(
        val anim: Animatable<IntSize, AnimationVector2D>,
        var startSize: IntSize
    )

    var animData: AnimData? = null

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {

        val placeable = measurable.measure(constraints)

        val measuredSize = IntSize(placeable.width, placeable.height)

        val (width, height) = animateTo(measuredSize)
        return layout(width, height) {
            placeable.placeRelative(0, 0)
        }
    }

    fun animateTo(targetSize: IntSize): IntSize {
        val data = animData?.apply {
            if (targetSize != anim.targetValue) {
                startSize = anim.value
                scope.launch {
                    val result = anim.animateTo(targetSize, animSpec)
                    if (result.endReason == AnimationEndReason.Finished) {
                        listener?.invoke(startSize, result.endState.value)
                    }
                }
            }
        } ?: AnimData(
            Animatable(
                targetSize, IntSize.VectorConverter, IntSize(1, 1)
            ),
            targetSize
        )

        animData = data
        return data.anim.value
    }
}
