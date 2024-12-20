package com.hedvig.android.feature.terminateinsurance.ui

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Ghost
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoOutline
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationScaffold(
  navigateUp: () -> Unit,
  closeTerminationFlow: () -> Unit,
  modifier: Modifier = Modifier,
  textForInfoIcon: String? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  var showExplanationBottomSheet by rememberSaveable { mutableStateOf(false) }
  if (textForInfoIcon != null) {
    ExplanationBottomSheet(
      onDismiss = { showExplanationBottomSheet = false },
      text = textForInfoIcon,
      isVisible = showExplanationBottomSheet,
    )
  }
  HedvigScaffold(
    modifier = modifier,
    navigateUp = navigateUp,
    topAppBarText = "",
    topAppBarActions = {
      if (textForInfoIcon != null) {
        IconButton(
          modifier = Modifier.size(24.dp),
          onClick = { showExplanationBottomSheet = true },
          content = {
            Icon(
              imageVector = HedvigIcons.InfoOutline,
              contentDescription = null,
            )
          },
        )
      }
      Spacer(Modifier.width(8.dp))
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = { closeTerminationFlow() },
        content = {
          Icon(
            imageVector = HedvigIcons.Close,
            contentDescription = null,
          )
        },
      )
    },
  ) {
    Spacer(modifier = Modifier.height(8.dp))
    HedvigText(
      text = stringResource(id = R.string.TERMINATION_FLOW_CANCELLATION_TITLE),
      style = HedvigTheme.typography.headlineMedium,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    content()
  }
}

// will probably need it later
@Composable
private fun CommonQuestions(modifier: Modifier = Modifier) {
  val listOfQuestions = buildList {
    add(
      AccordionData(
        stringResource(id = R.string.TERMINATION_Q_01),
        stringResource(id = R.string.TERMINATION_A_01),
      ),
    )
    add(
      AccordionData(
        stringResource(id = R.string.TERMINATION_Q_02),
        stringResource(id = R.string.TERMINATION_A_02),
      ),
    )
    add(
      AccordionData(
        stringResource(id = R.string.TERMINATION_Q_03),
        stringResource(id = R.string.TERMINATION_A_03),
      ),
    )
  }
  AccordionList(
    modifier = modifier,
    items = listOfQuestions,
  )
}

@Composable
private fun ExplanationBottomSheet(onDismiss: () -> Unit, text: String, isVisible: Boolean) {
  HedvigBottomSheet(
    onVisibleChange = { visible ->
      if (!visible) {
        onDismiss()
      }
    },
    isVisible = isVisible,
  ) {
    HedvigText(
      text = stringResource(id = R.string.TERMINATION_FLOW_CANCEL_INFO_TITLE),
      modifier = Modifier
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = text,
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier
        .fillMaxWidth(),
    )
    HedvigButton(
      onClick = onDismiss,
      text = stringResource(id = R.string.general_close_button),
      enabled = true,
      buttonStyle = Ghost,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun terminationDateText(terminationDate: LocalDate): String {
  val formatter = rememberHedvigDateTimeFormatter()
  val formattedDate = formatter.format(terminationDate.toJavaLocalDate())
  return stringResource(R.string.CONTRACT_STATUS_TO_BE_TERMINATED, formattedDate)
}

@HedvigPreview
@Composable
private fun PreviewTerminationOverviewScreenScaffold() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationScaffold(
        navigateUp = {},
        closeTerminationFlow = {},
        textForInfoIcon = "Icon text",
      ) {
      }
    }
  }
}
