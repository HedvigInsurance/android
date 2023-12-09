package com.hedvig.android.core.designsystem.animation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationEndReason
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
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
 * This is a copy of [animateContentSize] but with the width value *not* animating at all. The only modification done
 * is
 * ```
 * val (_, height) = animateTo(measuredSize)
 * return layout(measuredSize.width, height) {
 * ```
 * instead of
 * ```
 * val (width, height) = animateTo(measuredSize)
 * return layout(width, height) {
 * ```
 */
fun Modifier.animateContentHeight(
  animationSpec: FiniteAnimationSpec<IntSize> = spring(),
  finishedListener: ((initialValue: IntSize, targetValue: IntSize) -> Unit)? = null,
): Modifier = composed(
  inspectorInfo = debugInspectorInfo {
    name = "animateContentSize"
    properties["animationSpec"] = animationSpec
    properties["finishedListener"] = finishedListener
  },
) {
  val scope = rememberCoroutineScope()
  val animModifier = remember(scope) {
    HeightAnimationModifier(animationSpec, scope)
  }
  animModifier.listener = finishedListener
  this.clipToBounds().then(animModifier)
}

private class HeightAnimationModifier(
  val animSpec: AnimationSpec<IntSize>,
  val scope: CoroutineScope,
) : LayoutModifierWithPassThroughIntrinsics() {
  var listener: ((startSize: IntSize, endSize: IntSize) -> Unit)? = null

  data class AnimData(
    val anim: Animatable<IntSize, AnimationVector2D>,
    var startSize: IntSize,
  )

  var animData: AnimData? by mutableStateOf(null)

  override fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult {
    val placeable = measurable.measure(constraints)

    val measuredSize = IntSize(placeable.width, placeable.height)

    val (_, height) = animateTo(measuredSize)
    return layout(measuredSize.width, height) {
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
        initialValue = targetSize,
        typeConverter = IntSize.VectorConverter,
        visibilityThreshold = IntSize(1, 1),
      ),
      targetSize,
    )

    animData = data
    return data.anim.value
  }
}

private abstract class LayoutModifierWithPassThroughIntrinsics : LayoutModifier {
  final override fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int) =
    measurable.minIntrinsicWidth(height)

  final override fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int) =
    measurable.minIntrinsicHeight(width)

  final override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int) =
    measurable.maxIntrinsicWidth(height)

  final override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int) =
    measurable.maxIntrinsicHeight(width)
}
