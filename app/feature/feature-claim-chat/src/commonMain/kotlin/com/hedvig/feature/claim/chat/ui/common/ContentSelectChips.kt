package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.feature.claim.chat.data.StepContent
import hedvig.resources.Res
import hedvig.resources.TALKBACK_OPTION_NOT_SELECTED
import hedvig.resources.TALKBACK_OPTION_SELECTED
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ContentSelectChips(
  options: List<StepContent.ContentSelect.Option>,
  selectedOptionId: String?,
  onOptionClick: (StepContent.ContentSelect.Option) -> Unit,
  style: StepContent.ContentSelectStyle,
  modifier: Modifier = Modifier,
) {
  when (style) {
    StepContent.ContentSelectStyle.PILL -> {
      FlowRow(
        modifier = modifier.semantics {
          selectableGroup()
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        for (item in options) {
          key(item) {
            val selectedDescription = stringResource(Res.string.TALKBACK_OPTION_SELECTED)
            val notSelectedDescription =
              stringResource(Res.string.TALKBACK_OPTION_NOT_SELECTED)
            RoundCornersPill(
              isSelected = item.id == selectedOptionId,
              modifier = Modifier.semantics {
                stateDescription = if (item.id == selectedOptionId) {
                  selectedDescription
                } else {
                  notSelectedDescription
                }
              },
              onClick = {
                onOptionClick(item)
              },
            ) {
              HedvigText(item.title)
            }
          }
        }
      }
    }

    StepContent.ContentSelectStyle.BINARY -> {
      Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        for (item in options) {
          RoundCornersPill(
            onClick = {
              onOptionClick(item)
            },
            isSelected = item.id == selectedOptionId,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
          ) {
            Row(
              Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Center,
            ) {
              HedvigText(
                item.title,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
              )
            }
          }
        }
      }
    }
  }
}
