package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.a11y.getPerMonthDescription
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.tokens.BottomSheetTokens
import com.hedvig.android.design.system.hedvig.tokens.ScrimTokens
import com.hedvig.android.design.system.internals.BottomSheet
import com.hedvig.android.design.system.internals.rememberInternalHedvigBottomSheetState
import eu.wewox.modalsheet.ExperimentalSheetApi
import hedvig.resources.R

@Composable
fun HedvigBottomSheetPriceBreakdown(
  state: HedvigBottomSheetState<PriceInfoForBottomSheet>,
  modifier: Modifier = Modifier,
) {
  HedvigBottomSheet(
    state,
    modifier = modifier,
  ) {
    val data = state.data
    if (data != null) {
      Column {
        HedvigText(
          text = stringResource(R.string.PRICE_DETAILS_TITLE),
          modifier = Modifier.semantics {
            heading()
          },
        )
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .horizontalDivider(DividerPosition.Bottom),
        ) {
          Spacer(Modifier.height(16.dp))
          for (item in data.displayItems) {
            HorizontalItemsWithMaximumSpaceTaken(
              {
                HedvigText(
                  item.first,
                  fontSize = HedvigTheme.typography.label.fontSize,
                  color = HedvigTheme.colorScheme.textSecondary,
                )
              },
              {
                HedvigText(
                  text = item.second,
                  textAlign = TextAlign.End,
                  fontSize = HedvigTheme.typography.label.fontSize,
                  color = HedvigTheme.colorScheme.textSecondary,
                )
              },
              spaceBetween = 8.dp,
            )
          }
          Spacer(Modifier.height(16.dp))
        }
        val netPriceDescription = stringResource(
          R.string.TALK_BACK_YOUR_PRICE_AFTER_DISCOUNTS,
          data.totalNet.getPerMonthDescription(),
        )
        val grossPriceDescription = stringResource(
          R.string.TALK_BACK_YOUR_PRICE_BEFORE_DISCOUNTS,
          data.totalGross.getPerMonthDescription(),
        )
        Spacer(Modifier.height(16.dp))
        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            HedvigText(stringResource(R.string.TIER_FLOW_TOTAL))
          },
          endSlot = {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
              modifier = Modifier.semantics(true) {},
            ) {
              if (data.totalGross != data.totalNet) {
                HedvigText(
                  stringResource(
                    R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                    data.totalGross,
                  ),
                  style = HedvigTheme.typography.bodySmall.copy(
                    textDecoration = TextDecoration.LineThrough,
                    color = HedvigTheme.colorScheme.textSecondary,
                  ),
                  modifier = Modifier.semantics {
                    contentDescription = grossPriceDescription
                  },
                )
              }
              HedvigText(
                stringResource(
                  R.string.OFFER_COST_AND_PREMIUM_PERIOD_ABBREVIATION,
                  data.totalNet,
                ),
                modifier = Modifier.semantics {
                  contentDescription = netPriceDescription
                },
              )
            }
          },
          spaceBetween = 8.dp,
        )
        Spacer(Modifier.height(32.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.general_close_button),
          enabled = true,
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            state.dismiss()
          },
        )
        Spacer(Modifier.height(8.dp))
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}

data class PriceInfoForBottomSheet(
  val displayItems: List<Pair<String, String>>,
  val totalGross: UiMoney,
  val totalNet: UiMoney,
)

@OptIn(ExperimentalSheetApi::class)
@Composable
fun <T> HedvigBottomSheet(
  hedvigBottomSheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
  sheetPadding: PaddingValues = PaddingValues(0.dp),
  style: BottomSheetStyle = BottomSheetDefaults.bottomSheetStyle,
  modifier: Modifier = Modifier,
  dragHandle: @Composable (() -> Unit)? = null,
  content: @Composable ColumnScope.(T) -> Unit,
) {
  InternalHedvigBottomSheet(
    onDismissRequest = {
      // Purposefully left empty, as this callback is called *after* the sheet has finished animating to the hidden
      // state. HedvigBottomSheetState has logic internally which observes that state to turn the isVisible flag to
      // false automaticaly.
    },
    contentPadding = contentPadding,
    sheetPadding = sheetPadding,
    style = style,
    dragHandle = dragHandle,
    modifier = modifier,
    sheetState = hedvigBottomSheetState,
  ) {
    if (hedvigBottomSheetState.data != null) {
      content(hedvigBottomSheetState.data!!)
    }
  }
}

fun HedvigBottomSheetState<Unit>.show() {
  show(Unit)
}

@Composable
fun <T> rememberHedvigBottomSheetState(): HedvigBottomSheetState<T> {
  return rememberInternalHedvigBottomSheetState()
}

@OptIn(ExperimentalSheetApi::class)
@Composable
private fun <T> InternalHedvigBottomSheet(
  onDismissRequest: () -> Unit,
  sheetState: HedvigBottomSheetState<T>,
  contentPadding: PaddingValues,
  sheetPadding: PaddingValues,
  style: BottomSheetStyle,
  modifier: Modifier = Modifier,
  dragHandle: @Composable (() -> Unit)?,
  content: @Composable ColumnScope.() -> Unit,
) {
  BottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    sheetState = sheetState,
    shape = bottomSheetShape.shape,
    scrimColor = style.scrimColor ?: bottomSheetColors.scrimColor,
    containerColor = if (style.transparentBackground) {
      Color.Transparent
    } else {
      bottomSheetColors.bottomSheetBackgroundColor
    },
    contentColor = bottomSheetColors.contentColor,
    contentPadding = contentPadding,
    sheetPadding = sheetPadding,
    dragHandle = dragHandle
      ?: {
        DragHandle(
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .padding(top = 8.dp, bottom = 20.dp),
        )
      },
  ) {
    Column(
      modifier = if (style.automaticallyScrollableContent) {
        Modifier.verticalScroll(rememberScrollState())
      } else {
        Modifier
      },
    ) {
      content()
    }
  }
}

@Composable
private fun DragHandle(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .width(40.dp)
      .height(4.dp)
      .background(
        shape = HedvigTheme.shapes.cornerSmall,
        color = bottomSheetColors.chipColor,
      ),
  )
}

@Immutable
internal data class BottomSheetColors(
  val scrimColor: Color,
  val bottomSheetBackgroundColor: Color,
  val contentColor: Color,
  val chipColor: Color,
  val arrowColor: Color,
  val arrowBackgroundColor: Color,
)

internal val bottomSheetColors: BottomSheetColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      BottomSheetColors(
        scrimColor = fromToken(ScrimTokens.ContainerColor).copy(ScrimTokens.ContainerOpacity),
        bottomSheetBackgroundColor = fromToken(BottomSheetTokens.BottomSheetBackgroundColor),
        contentColor = fromToken(BottomSheetTokens.ContentColor),
        chipColor = fromToken(BottomSheetTokens.UpperChipColor),
        arrowColor = fromToken(BottomSheetTokens.ArrowColor),
        arrowBackgroundColor = fromToken(BottomSheetTokens.ArrowColorBackground),
      )
    }
  }

@Immutable
internal data class BottomSheetShape(
  val shape: Shape,
  val contentHorizontalPadding: Dp,
)

internal val bottomSheetShape: BottomSheetShape
  @Composable
  get() = BottomSheetShape(
    shape = BottomSheetTokens.ContainerShape.value,
    contentHorizontalPadding = BottomSheetTokens.ContentPadding,
  )

@Immutable
data class BottomSheetStyle(
  val transparentBackground: Boolean,
  val automaticallyScrollableContent: Boolean,
  val scrimColor: Color?,
)

object BottomSheetDefaults {
  val bottomSheetStyle: BottomSheetStyle = BottomSheetStyle(
    transparentBackground = false,
    automaticallyScrollableContent = true,
    scrimColor = null,
  )
}
