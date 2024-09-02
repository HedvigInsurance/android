package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.AccordionDefaults.Size
import com.hedvig.android.design.system.hedvig.tokens.AccordionTokens
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextAccordion
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.TextPrimary

@Composable
fun AccordionList(items: List<AccordionData>, modifier: Modifier = Modifier, size: Size = Size.Small) {
  Column(modifier,
    verticalArrangement = Arrangement.spacedBy(4.dp)) {
    for (perilItem in items) {
      var isExpanded by rememberSaveable { mutableStateOf(false) }
      AccordionItem(
        isExpanded = isExpanded,
        onClick = {
          isExpanded = !isExpanded
        },
        title = perilItem.title,
        description = perilItem.description,
        size = size,
      )
    }
  }
}

@Composable
private fun AccordionItem(
  isExpanded: Boolean,
  onClick: () -> Unit,
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  size: Size = Size.Small,
) {
  ExpandablePlusCard(
    modifier = modifier,
    shrunkContentPadding = size.padding,
    content = {
      HedvigText(
        text = title,
        color = accordionColors.labelTextColor,
        style = size.labelTextStyle,
      )
    },
    expandedContent = {
      Column {
        Spacer(Modifier.height(18.dp))
        HedvigText(
          text = description,
          color = accordionColors.descriptionTextColor,
          style = size.descriptionTextStyle,
        )
      }
    },
    isExpanded = isExpanded,
    onClick = onClick,
  )
}

object AccordionDefaults {
  sealed class Size {
    internal abstract val padding: PaddingValues

    @get:Composable
    internal abstract val labelTextStyle: TextStyle

    @get:Composable
    internal abstract val descriptionTextStyle: TextStyle

    data object Small : Size() {
      override val padding: PaddingValues
        get() = PaddingValues(
          top = AccordionTokens.SmallPaddingTop,
          bottom = AccordionTokens.SmallPaddingBottom,
          start = AccordionTokens.SmallPaddingHorizontal,
          end = AccordionTokens.SmallPaddingHorizontal,
        )
      override val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = AccordionTokens.SmallLabelTextStyle.value
      override val descriptionTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = AccordionTokens.DescriptionTextStyle.value
    }

    data object Large : Size() {
      override val padding: PaddingValues
        get() = PaddingValues(
          top = AccordionTokens.LargePaddingTop,
          bottom = AccordionTokens.LargePaddingBottom,
          start = AccordionTokens.LargePaddingHorizontal,
          end = AccordionTokens.LargePaddingHorizontal,
        )
      override val labelTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = AccordionTokens.LargeLabelTextStyle.value
      override val descriptionTextStyle: TextStyle
        @Composable
        @ReadOnlyComposable
        get() = AccordionTokens.DescriptionTextStyle.value
    }
  }
}

private data class AccordionColors(
  val labelTextColor: Color,
  val descriptionTextColor: Color,
)

private val accordionColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      AccordionColors(
        labelTextColor = fromToken(TextPrimary),
        descriptionTextColor = fromToken(TextAccordion),
      )
    }
  }

data class AccordionData(
  val title: String,
  val description: String,
)
