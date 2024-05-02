package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.offset
import com.hedvig.android.compose.ui.animateContentHeight
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun HedvigTextFieldLayout(
  modifier: Modifier,
  size: HedvigTextFieldSize,
  textField: @Composable () -> Unit,
  label: @Composable (() -> Unit)?,
  leading: @Composable (() -> Unit)?,
  trailing: @Composable (() -> Unit)?,
  singleLine: Boolean,
  animationProgress: Float,
  container: @Composable () -> Unit,
  supporting: @Composable (() -> Unit)?,
  paddingValues: PaddingValues,
) {
  val measurePolicy = remember(singleLine, animationProgress, paddingValues) {
    TextFieldMeasurePolicy(singleLine, animationProgress, paddingValues)
  }
  val layoutDirection = LocalLayoutDirection.current
  Layout(
    modifier = modifier,
    content = {
      // The container is given as a Composable instead of a background modifier so that
      // elements like supporting text can be placed outside of it while still contributing
      // to the text field's measurements overall.
      container()

      if (leading != null) {
        Box(
          modifier = Modifier
            .layoutId(LeadingId)
            .then(IconDefaultSizeModifier),
          contentAlignment = Alignment.Center,
        ) {
          leading()
        }
      }
      if (trailing != null) {
        Box(
          modifier = Modifier
            .layoutId(TrailingId)
            .then(IconDefaultSizeModifier),
          contentAlignment = Alignment.Center,
        ) {
          trailing()
        }
      }

      val startTextFieldPadding = paddingValues.calculateStartPadding(layoutDirection)
      val endTextFieldPadding = paddingValues.calculateEndPadding(layoutDirection)

      // todo look into if this padding is the right if there is a leading icon
      val startPadding = if (leading != null) {
        (startTextFieldPadding - HorizontalIconPadding).coerceAtLeast(0.dp)
      } else {
        startTextFieldPadding
      }
      val endPadding = if (trailing != null) {
        (endTextFieldPadding - HorizontalIconPadding).coerceAtLeast(0.dp)
      } else {
        endTextFieldPadding
      }

      if (label != null) {
        Box(
          Modifier
            .layoutId(LabelId)
            .heightIn(
              min = lerp(
                MinTextLineHeight,
                MinFocusedLabelLineHeight,
                animationProgress,
              ),
            )
            .wrapContentHeight()
            .padding(start = startPadding, end = endPadding),
        ) { label() }
      }

      val textPadding = Modifier
        .heightIn(min = MinTextLineHeight)
        .wrapContentHeight()

      Box(
        modifier = Modifier
          .layoutId(TextFieldId)
          .then(textPadding),
        propagateMinConstraints = true,
      ) {
        textField()
      }

      Box(
        Modifier
          .layoutId(SupportingId)
          .animateContentHeight()
          .then(if (supporting == null) Modifier.requiredSize(0.dp) else Modifier)
          .wrapContentHeight()
          .padding(size.supportingTextPadding),
      ) {
        if (supporting != null) {
          supporting()
        }
      }
    },
    measurePolicy = measurePolicy,
  )
}

private class TextFieldMeasurePolicy(
  private val singleLine: Boolean,
  private val animationProgress: Float,
  private val paddingValues: PaddingValues,
) : MeasurePolicy {
  override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
    val topPaddingValue = paddingValues.calculateTopPadding().roundToPx()
    val bottomPaddingValue = paddingValues.calculateBottomPadding().roundToPx()

    var occupiedSpaceHorizontally = 0
    var occupiedSpaceVertically = 0

    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

    // measure leading icon
    val leadingPlaceable =
      measurables.find { it.layoutId == LeadingId }?.measure(looseConstraints)
    occupiedSpaceHorizontally += widthOrZero(leadingPlaceable)
    occupiedSpaceVertically = max(occupiedSpaceVertically, heightOrZero(leadingPlaceable))

    // measure trailing icon
    val trailingPlaceable = measurables.find { it.layoutId == TrailingId }
      ?.measure(looseConstraints.offset(horizontal = -occupiedSpaceHorizontally))
    occupiedSpaceHorizontally += widthOrZero(trailingPlaceable)
    occupiedSpaceVertically = max(occupiedSpaceVertically, heightOrZero(trailingPlaceable))

    // measure label
    val labelConstraints = looseConstraints
      .offset(
        vertical = -bottomPaddingValue,
        horizontal = -occupiedSpaceHorizontally,
      )
    val labelPlaceable =
      measurables.find { it.layoutId == LabelId }?.measure(labelConstraints)

    // measure input field
    val effectiveTopOffset = topPaddingValue + heightOrZero(labelPlaceable)
    val verticalConstraintOffset = -effectiveTopOffset - bottomPaddingValue
    val textFieldConstraints = constraints
      .copy(minHeight = 0)
      .offset(
        vertical = verticalConstraintOffset,
        horizontal = -occupiedSpaceHorizontally,
      )
    val textFieldPlaceable = measurables
      .first { it.layoutId == TextFieldId }
      .measure(textFieldConstraints)

    occupiedSpaceVertically = max(
      occupiedSpaceVertically,
      heightOrZero(textFieldPlaceable) + effectiveTopOffset + bottomPaddingValue,
    )

    // measure supporting text
    val supportingConstraints = looseConstraints.offset(
      vertical = -occupiedSpaceVertically,
    ).copy(minHeight = 0)
    val supportingPlaceable =
      measurables.find { it.layoutId == SupportingId }?.measure(supportingConstraints)
    val supportingHeight = heightOrZero(supportingPlaceable)

    val width = calculateWidth(
      leadingWidth = widthOrZero(leadingPlaceable),
      trailingWidth = widthOrZero(trailingPlaceable),
      textFieldWidth = textFieldPlaceable.width,
      labelWidth = widthOrZero(labelPlaceable),
      constraints = constraints,
    )
    val totalHeight = calculateHeight(
      textFieldHeight = textFieldPlaceable.height,
      labelHeight = heightOrZero(labelPlaceable),
      leadingHeight = heightOrZero(leadingPlaceable),
      trailingHeight = heightOrZero(trailingPlaceable),
      supportingHeight = heightOrZero(supportingPlaceable),
      isLabelFocused = animationProgress == 1f,
      constraints = constraints,
      density = density,
      paddingValues = paddingValues,
    )
    val height = totalHeight - supportingHeight

    val containerPlaceable = measurables.first { it.layoutId == ContainerId }.measure(
      Constraints(
        minWidth = if (width != Constraints.Infinity) width else 0,
        maxWidth = width,
        minHeight = if (height != Constraints.Infinity) height else 0,
        maxHeight = height,
      ),
    )

    return layout(width, totalHeight) {
      if (labelPlaceable != null) {
        placeWithLabel(
          width = width,
          totalHeight = totalHeight,
          textfieldPlaceable = textFieldPlaceable,
          labelPlaceable = labelPlaceable,
          leadingPlaceable = leadingPlaceable,
          trailingPlaceable = trailingPlaceable,
          containerPlaceable = containerPlaceable,
          supportingPlaceable = supportingPlaceable,
          singleLine = singleLine,
          labelEndPosition = topPaddingValue,
          textPosition = topPaddingValue + labelPlaceable.height,
          animationProgress = animationProgress,
          density = density,
        )
      } else {
        placeWithoutLabel(
          width = width,
          totalHeight = totalHeight,
          textPlaceable = textFieldPlaceable,
          leadingPlaceable = leadingPlaceable,
          trailingPlaceable = trailingPlaceable,
          containerPlaceable = containerPlaceable,
          supportingPlaceable = supportingPlaceable,
          singleLine = singleLine,
          density = density,
          paddingValues = paddingValues,
        )
      }
    }
  }

  override fun IntrinsicMeasureScope.maxIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int {
    return intrinsicHeight(measurables, width) { intrinsicMeasurable, w ->
      intrinsicMeasurable.maxIntrinsicHeight(w)
    }
  }

  override fun IntrinsicMeasureScope.minIntrinsicHeight(measurables: List<IntrinsicMeasurable>, width: Int): Int {
    return intrinsicHeight(measurables, width) { intrinsicMeasurable, w ->
      intrinsicMeasurable.minIntrinsicHeight(w)
    }
  }

  override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int {
    return intrinsicWidth(measurables, height) { intrinsicMeasurable, h ->
      intrinsicMeasurable.maxIntrinsicWidth(h)
    }
  }

  override fun IntrinsicMeasureScope.minIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int {
    return intrinsicWidth(measurables, height) { intrinsicMeasurable, h ->
      intrinsicMeasurable.minIntrinsicWidth(h)
    }
  }

  private fun intrinsicWidth(
    measurables: List<IntrinsicMeasurable>,
    height: Int,
    intrinsicMeasurer: (IntrinsicMeasurable, Int) -> Int,
  ): Int {
    val textFieldWidth =
      intrinsicMeasurer(measurables.first { it.layoutId == TextFieldId }, height)
    val labelWidth = measurables.find { it.layoutId == LabelId }?.let {
      intrinsicMeasurer(it, height)
    } ?: 0
    val trailingWidth = measurables.find { it.layoutId == TrailingId }?.let {
      intrinsicMeasurer(it, height)
    } ?: 0
    val leadingWidth = measurables.find { it.layoutId == LeadingId }?.let {
      intrinsicMeasurer(it, height)
    } ?: 0
    return calculateWidth(
      leadingWidth = leadingWidth,
      trailingWidth = trailingWidth,
      textFieldWidth = textFieldWidth,
      labelWidth = labelWidth,
      constraints = ZeroConstraints,
    )
  }

  private fun IntrinsicMeasureScope.intrinsicHeight(
    measurables: List<IntrinsicMeasurable>,
    width: Int,
    intrinsicMeasurer: (IntrinsicMeasurable, Int) -> Int,
  ): Int {
    val textFieldHeight =
      intrinsicMeasurer(measurables.first { it.layoutId == TextFieldId }, width)
    val labelHeight = measurables.find { it.layoutId == LabelId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    val trailingHeight = measurables.find { it.layoutId == TrailingId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    val leadingHeight = measurables.find { it.layoutId == LeadingId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    val supportingHeight = measurables.find { it.layoutId == SupportingId }?.let {
      intrinsicMeasurer(it, width)
    } ?: 0
    return calculateHeight(
      textFieldHeight = textFieldHeight,
      labelHeight = labelHeight,
      leadingHeight = leadingHeight,
      trailingHeight = trailingHeight,
      supportingHeight = supportingHeight,
      isLabelFocused = animationProgress == 1f,
      constraints = ZeroConstraints,
      density = density,
      paddingValues = paddingValues,
    )
  }
}

private fun calculateWidth(
  leadingWidth: Int,
  trailingWidth: Int,
  textFieldWidth: Int,
  labelWidth: Int,
  constraints: Constraints,
): Int {
  val middleSection = max(textFieldWidth, labelWidth)
  val wrappedWidth = leadingWidth + middleSection + trailingWidth
  return max(wrappedWidth, constraints.minWidth)
}

private fun calculateHeight(
  textFieldHeight: Int,
  labelHeight: Int,
  leadingHeight: Int,
  trailingHeight: Int,
  supportingHeight: Int,
  isLabelFocused: Boolean,
  constraints: Constraints,
  density: Float,
  paddingValues: PaddingValues,
): Int {
  val hasLabel = labelHeight > 0
  // Even though the padding is defined by the developer, if there's a label, it only affects the
  // text field in the focused state. Otherwise, we use the default value.
  val verticalPadding = density * if (!hasLabel || isLabelFocused) {
    (paddingValues.calculateTopPadding() + paddingValues.calculateBottomPadding()).value
  } else {
    (TextFieldPadding * 2).value
  }

  val middleSectionHeight = if (hasLabel && isLabelFocused) {
    verticalPadding + labelHeight + textFieldHeight
  } else {
    verticalPadding + maxOf(labelHeight, textFieldHeight)
  }
  return max(
    constraints.minHeight,
    maxOf(
      leadingHeight,
      trailingHeight,
      middleSectionHeight.roundToInt(),
    ) + supportingHeight,
  )
}

/**
 * Places the provided text field and label in the TextField given the PaddingValues
 * when there is a label. When there is no label, [placeWithoutLabel] is used instead.
 */
@Suppress("NAME_SHADOWING")
private fun Placeable.PlacementScope.placeWithLabel(
  width: Int,
  totalHeight: Int,
  textfieldPlaceable: Placeable,
  labelPlaceable: Placeable?,
  leadingPlaceable: Placeable?,
  trailingPlaceable: Placeable?,
  containerPlaceable: Placeable,
  supportingPlaceable: Placeable?,
  singleLine: Boolean,
  labelEndPosition: Int,
  textPosition: Int,
  animationProgress: Float,
  density: Float,
) {
  // place container
  containerPlaceable.place(IntOffset.Zero)

  // Most elements should be positioned w.r.t the text field's "visual" height, i.e., excluding
  // the supporting text on bottom
  val height = totalHeight - heightOrZero(supportingPlaceable)

  // Hedvig adjustment so that singleLine also makes it so that the text+label are center aligned in the container
  // height. This allows for the height to grow while not making the text field look like it's off-centered.
  val (textPosition, labelEndPosition) = if (singleLine) {
    val labelHeight = heightOrZero(labelPlaceable)
    val labelPlusTextFieldHeight = labelHeight + textfieldPlaceable.height
    val topYPosition = Alignment.CenterVertically.align(labelPlusTextFieldHeight, height)
    (topYPosition + labelHeight) to topYPosition
  } else {
    textPosition to labelEndPosition
  }
  leadingPlaceable?.placeRelative(
    0,
    Alignment.CenterVertically.align(leadingPlaceable.height, height),
  )
  trailingPlaceable?.placeRelative(
    width - trailingPlaceable.width,
    Alignment.CenterVertically.align(trailingPlaceable.height, height),
  )
  labelPlaceable?.let {
    // if it's a single line, the label's start position is in the center of the
    // container. When it's a multiline text field, the label's start position is at the
    // top with padding
    val startPosition = if (singleLine) {
      Alignment.CenterVertically.align(it.height, height)
    } else {
      // Even though the padding is defined by the developer, it only affects the text field
      // when the text field is focused. Otherwise, we use the default value.
      (TextFieldPadding.value * density).roundToInt()
    }
    val distance = startPosition - labelEndPosition
    val positionY = startPosition - (distance * animationProgress).roundToInt()
    it.placeRelative(widthOrZero(leadingPlaceable), positionY)
  }

  val textHorizontalPosition = widthOrZero(leadingPlaceable)
  textfieldPlaceable.placeRelative(textHorizontalPosition, textPosition)

  supportingPlaceable?.placeRelative(0, height)
}

/**
 * Places the provided text field in [TextField] when there is no label. When
 * there is a label, [placeWithLabel] is used
 */
private fun Placeable.PlacementScope.placeWithoutLabel(
  width: Int,
  totalHeight: Int,
  textPlaceable: Placeable,
  leadingPlaceable: Placeable?,
  trailingPlaceable: Placeable?,
  containerPlaceable: Placeable,
  supportingPlaceable: Placeable?,
  singleLine: Boolean,
  density: Float,
  paddingValues: PaddingValues,
) {
  // place container
  containerPlaceable.place(IntOffset.Zero)

  // Most elements should be positioned w.r.t the text field's "visual" height, i.e., excluding
  // the supporting text on bottom
  val height = totalHeight - heightOrZero(supportingPlaceable)
  val topPadding = (paddingValues.calculateTopPadding().value * density).roundToInt()

  leadingPlaceable?.placeRelative(
    0,
    Alignment.CenterVertically.align(leadingPlaceable.height, height),
  )
  trailingPlaceable?.placeRelative(
    width - trailingPlaceable.width,
    Alignment.CenterVertically.align(trailingPlaceable.height, height),
  )

  // Single line text field without label places its text components centered vertically.
  // Multiline text field without label places its text components at the top with padding.
  fun calculateVerticalPosition(placeable: Placeable): Int {
    return if (singleLine) {
      Alignment.CenterVertically.align(placeable.height, height)
    } else {
      topPadding
    }
  }
  val textHorizontalPosition = widthOrZero(leadingPlaceable)
  textPlaceable.placeRelative(textHorizontalPosition, calculateVerticalPosition(textPlaceable))

  supportingPlaceable?.placeRelative(0, height)
}
