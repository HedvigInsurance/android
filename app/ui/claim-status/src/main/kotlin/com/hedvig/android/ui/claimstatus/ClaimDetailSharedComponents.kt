package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.LayoutWithoutPlacement
import com.hedvig.android.compose.ui.stringWithShiftedLabel
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Date
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.DateTime
import com.hedvig.android.data.display.items.DisplayItem.DisplayItemValue.Text
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigThreeDotsProgressIndicator
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.LocalTextStyle
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import hedvig.resources.MY_DOCUMENTS_INSURANCE_TERMS
import hedvig.resources.Res
import hedvig.resources.TALKBACK_OPEN_EXTERNAL_LINK
import hedvig.resources.claim_status_claim_details_info_text
import hedvig.resources.general_close_button
import org.jetbrains.compose.resources.stringResource

@Composable
fun ClaimExplanationBottomSheet(sheetState: HedvigBottomSheetState<Unit>) {
  HedvigBottomSheet(sheetState) { _ ->
    HedvigText(
      text = stringResource(Res.string.claim_status_claim_details_info_text),
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(32.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_close_button),
      buttonSize = Large,
      onClick = { sheetState.dismiss() },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
fun ClaimDisplayItemsSection(displayItems: List<DisplayItem>, modifier: Modifier = Modifier) {
  CompositionLocalProvider(LocalContentColor provides HedvigTheme.colorScheme.textSecondary) {
    Column(modifier) {
      for (displayItem in displayItems) {
        HorizontalItemsWithMaximumSpaceTaken(
          spaceBetween = 8.dp,
          startSlot = {
            HedvigText(text = displayItem.title)
          },
          endSlot = {
            val formatter = rememberHedvigDateTimeFormatter()
            HedvigText(
              text = when (val item = displayItem.value) {
                is Date -> formatter.format(item.date)
                is DateTime -> formatter.format(item.localDateTime)
                is Text -> item.text
              },
              textAlign = TextAlign.End,
            )
          },
        )
      }
    }
  }
}

@Composable
fun ClaimTermsConditionsCard(onClick: () -> Unit, isLoading: Boolean, modifier: Modifier = Modifier) {
  HedvigCard(onClick = onClick) {
    Row(
      modifier,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (isLoading) {
        LayoutWithoutPlacement(
          sizeAdjustingContent = {
            ClaimDocumentCard(
              title = stringResource(Res.string.MY_DOCUMENTS_INSURANCE_TERMS),
            )
          },
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
          ) {
            HedvigThreeDotsProgressIndicator()
          }
        }
      } else {
        ClaimDocumentCard(
          title = stringResource(Res.string.MY_DOCUMENTS_INSURANCE_TERMS),
        )
      }
    }
  }
}

@Composable
fun ClaimDocumentCard(title: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Column {
          HedvigText(
            text = stringWithShiftedLabel(
              text = title,
              labelText = "PDF",
              labelFontSize = HedvigTheme.typography.label.fontSize,
              textColor = LocalContentColor.current,
              textFontSize = LocalTextStyle.current.fontSize,
            ),
          )
        }
      },
      endSlot = {
        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = HedvigIcons.ArrowNorthEast,
            contentDescription = stringResource(Res.string.TALKBACK_OPEN_EXTERNAL_LINK),
            modifier = Modifier.size(16.dp),
          )
        }
      },
      spaceBetween = 8.dp,
    )
  }
}
