package com.hedvig.android.feature.movingflow.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.design.system.hedvig.DialogDefaults.DialogStyle.Buttons
import com.hedvig.android.design.system.hedvig.HedvigDialog
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType.BACK
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import hedvig.resources.R

@Composable
internal fun MovingFlowTopAppBar(
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  topAppBarText: String? = null,
  withExitConfirmation: Boolean = true,
) {
  var showExitDialog by rememberSaveable { mutableStateOf(false) }
  MovingFlowTopAppBar(
    showExitDialog = showExitDialog,
    setShowExitDialog = { showExitDialog = it },
    navigateUp = navigateUp,
    exitFlow = exitFlow,
    topAppBarText = topAppBarText,
    withExitConfirmation = withExitConfirmation,
  )
}

@Composable
private fun MovingFlowTopAppBar(
  showExitDialog: Boolean,
  setShowExitDialog: (Boolean) -> Unit,
  navigateUp: () -> Unit,
  exitFlow: () -> Unit,
  topAppBarText: String? = null,
  withExitConfirmation: Boolean = true,
) {
  if (showExitDialog) {
    HedvigDialog(
      onDismissRequest = { setShowExitDialog(false) },
      style = Buttons(
        onDismissRequest = { setShowExitDialog(false) },
        dismissButtonText = stringResource(R.string.GENERAL_NO),
        onConfirmButtonClick = {
          setShowExitDialog(false)
          exitFlow()
        },
        confirmButtonText = stringResource(R.string.GENERAL_YES),
      ),
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HedvigText(
          text = stringResource(R.string.GENERAL_ARE_YOU_SURE),
          textAlign = TextAlign.Center,
        )
        HedvigText(
          text = stringResource(R.string.GENERAL_PROGRESS_WILL_BE_LOST_ALERT),
          textAlign = TextAlign.Center,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }
  }
  TopAppBar(
    title = topAppBarText ?: "",
    actionType = BACK,
    onActionClick = dropUnlessResumed(block = navigateUp),
    topAppBarActions = {
      IconButton(
        modifier = Modifier.size(24.dp),
        onClick = dropUnlessResumed {
          if (withExitConfirmation) {
            setShowExitDialog(true)
          } else {
            exitFlow()
          }
        },
        content = { Icon(imageVector = HedvigIcons.Close, contentDescription = null) },
      )
    },
  )
}

@HedvigPreview
@Composable
private fun PreviewMovingFlowTopAppBar(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) showDialog: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MovingFlowTopAppBar(
        showDialog,
        {},
        {},
        {},
        "Title",
        true,
      )
    }
  }
}
