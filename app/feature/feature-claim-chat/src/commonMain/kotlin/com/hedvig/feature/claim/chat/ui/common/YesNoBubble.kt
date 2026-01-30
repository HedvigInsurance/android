package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.feature.claim.chat.data.StepContent
import hedvig.resources.GENERAL_NO
import hedvig.resources.GENERAL_YES
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun YesNoBubble(
  questionText: String,
  answerSelected: String?,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier,
  errorText: String? = null,
) {
  val options = listOf(
    StepContent.ContentSelect.Option(
      "true",
      stringResource(Res.string.GENERAL_YES),
    ),
    StepContent.ContentSelect.Option(
      "false",
      stringResource(Res.string.GENERAL_NO),
    ),
  )
  Column(modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End,
    ) {
      HedvigText(
        style = HedvigTheme.typography.label,
        text = questionText,
      )
      Spacer(Modifier.width(16.dp))
      ContentSelectChips(
        options = options,
        onOptionClick = { option ->
          onSelect(option.title)
        },
        style = StepContent.ContentSelectStyle.BINARY,
        selectedOptionId = options.firstOrNull { it.title == answerSelected }?.id,
      )
    }
    AnimatedVisibility(errorText != null) {
      Column {
        if (errorText != null) {
          Spacer(Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
          ) {
            HedvigText(
              errorText,
              style = HedvigTheme.typography.label,
              color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            )
          }
        }
      }
    }
  }
}
