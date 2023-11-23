package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun EditCoInsuredDestination(
  contractId: String,
  allowEdit: Boolean,
  navigateUp: () -> Unit,
) {
  val viewModel: EditCoInsuredViewModel = koinViewModel { parametersOf(contractId) }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  EditCoInsuredScreen(
    navigateUp,
    allowEdit,
    uiState,
  )
}

@Composable
private fun EditCoInsuredScreen(
  navigateUp: () -> Unit,
  allowEdit: Boolean,
  uiState: EditCoInsuredState,
) {
  Column(Modifier.fillMaxSize()) {
    TopAppBarWithBack(
      title = stringResource(id = hedvig.resources.R.string.COINSURED_EDIT_TITLE),
      onClick = navigateUp,
    )
    CoInsuredList(uiState, allowEdit)
  }
}

@Composable
private fun CoInsuredList(uiState: EditCoInsuredState, allowEdit: Boolean) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  Column {
    uiState.member?.let {
      InsuredRow(
        displayName = it.displayName,
        details = it.ssn,
        hasMissingInfo = false,
        allowEdit = false,
        isMember = true,
        onRemove = { },
        onEdit = { },
      )
    }
    Divider(Modifier.padding(horizontal = 16.dp))
    uiState.coInsured.forEachIndexed { index, coInsured ->
      if (index != 0) {
        Divider()
      }

      InsuredRow(
        displayName = coInsured.displayName,
        details = coInsured.identifier(dateTimeFormatter),
        hasMissingInfo = coInsured.hasMissingInfo,
        isMember = false,
        allowEdit = allowEdit,
        onRemove = { },
        onEdit = { },
      )
    }
  }
}

@Composable
@HedvigPreview
private fun EditCoInsuredScreenEditablePreview() {
  HedvigTheme {
    Surface {
      EditCoInsuredScreen(
        navigateUp = { },
        allowEdit = true,
        uiState = EditCoInsuredState(
          isLoading = false,
          errorMessage = null,
          member = Member(
            firstName = "Member",
            lastName = "Membersson",
            ssn = "197312331093",
          ),
          coInsured = persistentListOf(
            CoInsured(
              "Test",
              "Testersson",
              LocalDate.fromEpochDays(300),
              "19910113-1093",
              hasMissingInfo = false,
            ),
            CoInsured(
              null,
              null,
              null,
              null,
              hasMissingInfo = true,
            ),
          ),
        ),
      )
    }
  }
}

@Composable
@HedvigPreview
private fun EditCoInsuredScreenNonEditablePreview() {
  HedvigTheme {
    Surface {
      EditCoInsuredScreen(
        navigateUp = { },
        allowEdit = false,
        uiState = EditCoInsuredState(
          isLoading = false,
          errorMessage = null,
          member = Member(
            firstName = "Member",
            lastName = "Membersson",
            ssn = "197312331093",
          ),
          coInsured = persistentListOf(
            CoInsured(
              "Test",
              "Testersson",
              LocalDate.fromEpochDays(300),
              "19910113-1093",
              hasMissingInfo = false,
            ),
            CoInsured(
              null,
              null,
              null,
              null,
              hasMissingInfo = true,
            ),
          ),
        ),
      )
    }
  }
}
