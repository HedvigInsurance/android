package com.hedvig.android.design.system.hedvig

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.PerilDefaults.PerilSize
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextPrimary
import com.hedvig.android.design.system.hedvig.tokens.PerilCommonTokens
import com.hedvig.android.design.system.hedvig.tokens.PerilLargeTokens
import com.hedvig.android.design.system.hedvig.tokens.PerilSmallTokens
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

@Composable
fun PerilList(perilItems: List<PerilData>, size: PerilSize, modifier: Modifier = Modifier) {
  Column(
    modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    for (perilItem in perilItems) {
      var isExpanded by rememberSaveable { mutableStateOf(false) }
      ExpandablePerilCard(
        isExpanded = isExpanded,
        onClick = {
          isExpanded = !isExpanded
        },
        color = if (perilItem.isEnabled) {
          parseColorString(perilItem.colorCode)
        } else {
          perilColors.disabledIconColor
        },
        title = perilItem.title,
        description = perilItem.description,
        expandedDescriptionList = perilItem.covered,
        size = size,
        isEnabled = perilItem.isEnabled,
      )
    }
  }
}

@Composable
private fun ExpandablePerilCard(
  isExpanded: Boolean,
  isEnabled: Boolean,
  onClick: () -> Unit,
  color: Color,
  title: String,
  description: String,
  size: PerilSize,
  expandedDescriptionList: List<String>,
  modifier: Modifier = Modifier,
) {
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = onClick,
    content = {
      Spacer(
        Modifier
          .wrapContentSize(Alignment.Center)
          .size(size.circleSize)
          .padding(1.dp)
          .background(color, CircleShape),
      )
      Spacer(Modifier.width(size.labelLineSpacerWidth))
      HedvigText(
        text = title,
        style = size.labelTextStyle,
        color = perilColors.textColor(isEnabled),
        modifier = Modifier.weight(1f, true),
      )
    },
    expandedContent = {
      Column(
        Modifier.padding(size.extendedPadding),
      ) {
        Spacer(Modifier.height(size.verticalSpaceBetween))
        Spacer(Modifier.height(12.dp))
        HedvigText(
          text = description,
          style = size.descriptionTextStyle,
          color = perilColors.textColor(isEnabled),
        )
        Spacer(Modifier.height(12.dp))
        if (expandedDescriptionList.isNotEmpty()) {
          Spacer(Modifier.height(12.dp))
          for ((index, itemDescription) in expandedDescriptionList.withIndex()) {
            Row {
              HedvigText(
                text = (index + 1).toString().padStart(length = 2, padChar = '0'),
                style = size.descriptionTextStyle,
                color = perilColors.numbersColor,
              )
              Spacer(Modifier.width(12.dp))
              HedvigText(
                text = itemDescription,
                style = size.descriptionTextStyle,
                color = perilColors.textColor(isEnabled),
                modifier = Modifier.weight(1f),
              )
            }
            if (index != expandedDescriptionList.lastIndex) {
              Spacer(Modifier.height(12.dp))
            }
          }
        }
      }
    },
    modifier = modifier,
    contentPadding = size.padding,
  )
}

data class PerilData(
  val title: String,
  val description: String,
  val covered: List<String>,
  val colorCode: String?,
  val isEnabled: Boolean = true,
)

@Composable
private fun parseColorString(colorString: String?): Color = with(HedvigTheme.colorScheme) {
  remember(this) {
    try {
      Color(parseColor(colorString))
    } catch (e: Exception) {
      logcat(priority = LogPriority.ERROR) { "Parsing color with colorString:$colorString resulted in an error" }
      fromToken(TextPrimary)
    }
  }
}

object PerilDefaults {
  sealed class PerilSize {
    internal abstract val padding: PaddingValues
    internal abstract val extendedPadding: PaddingValues
    internal abstract val verticalSpaceBetween: Dp
    internal abstract val circleSize: Dp
    internal abstract val labelLineSpacerWidth: Dp

    @get:Composable
    internal abstract val labelTextStyle: TextStyle

    @get:Composable
    internal abstract val descriptionTextStyle: TextStyle

    data object Small : PerilSize() {
      override val padding: PaddingValues
        get() = PaddingValues(
          top = PerilSmallTokens.PaddingTop,
          bottom = PerilSmallTokens.PaddingBottom,
          start = PerilSmallTokens.PaddingHorizontal,
          end = PerilSmallTokens.PaddingHorizontal,
        )
      override val extendedPadding: PaddingValues
        get() = PaddingValues(
          top = PerilSmallTokens.ExpandedPaddingTop,
          bottom = PerilSmallTokens.ExpandedPaddingBottom,
          start = PerilSmallTokens.ExpandedPaddingStart,
          end = PerilSmallTokens.ExpandedPaddingEnd,
        )
      override val verticalSpaceBetween: Dp
        get() = PerilSmallTokens.VerticalSpaceBetween
      override val circleSize: Dp
        get() = PerilSmallTokens.CircleSize
      override val labelLineSpacerWidth: Dp
        get() = PerilSmallTokens.LabelLineSpacerWidth
      override val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = PerilSmallTokens.LabelTextFont.value
      override val descriptionTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = PerilSmallTokens.DescriptionTextFont.value
    }

    data object Large : PerilSize() {
      override val padding: PaddingValues
        get() = PaddingValues(
          top = PerilLargeTokens.PaddingTop,
          bottom = PerilLargeTokens.PaddingBottom,
          start = PerilLargeTokens.PaddingHorizontal,
          end = PerilLargeTokens.PaddingHorizontal,
        )
      override val labelLineSpacerWidth: Dp
        get() = PerilLargeTokens.LabelLineSpacerWidth
      override val extendedPadding: PaddingValues
        get() = PaddingValues(
          top = PerilLargeTokens.ExpandedPaddingTop,
          bottom = PerilLargeTokens.ExpandedPaddingBottom,
          start = PerilLargeTokens.ExpandedPaddingStart,
          end = PerilLargeTokens.ExpandedPaddingEnd,
        )
      override val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = PerilLargeTokens.LabelTextFont.value
      override val descriptionTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = PerilLargeTokens.DescriptionTextFont.value
      override val verticalSpaceBetween: Dp
        get() = PerilLargeTokens.VerticalSpaceBetween
      override val circleSize: Dp
        get() = PerilLargeTokens.CircleSize
    }
  }
}

private data class PerilColors(
  val enabledTextColor: Color,
  val numbersColor: Color,
  val enabledIconColor: Color,
  val disabledTextColor: Color,
  val disabledIconColor: Color,
) {
  fun textColor(isEnabled: Boolean): Color {
    return if (isEnabled) enabledTextColor else disabledTextColor
  }
}

private val perilColors: PerilColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      PerilColors(
        enabledIconColor = fromToken(PerilCommonTokens.DefaultIconColor),
        numbersColor = fromToken(PerilCommonTokens.NumbersColor),
        enabledTextColor = fromToken(PerilCommonTokens.TextColor),
        disabledIconColor = fromToken(PerilCommonTokens.DisabledIconColor),
        disabledTextColor = fromToken(PerilCommonTokens.DisabledTextColor),
      )
    }
  }
