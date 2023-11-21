package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
  navigateUp: () -> Unit,
) {
  val viewModel: EditCoInsuredViewModel = koinViewModel { parametersOf(contractId) }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  EditCoInsuredScreen(
    navigateUp,
    uiState,
  )
}

@Composable
private fun EditCoInsuredScreen(
  navigateUp: () -> Unit,
  uiState: EditCoInsuredState,
) {
  Column(Modifier.fillMaxSize()) {
    TopAppBarWithBack(
      title = "Insured people", // TODO
      onClick = navigateUp,
    )
    CoInsuredList(uiState)
  }
}

@Composable
private fun CoInsuredList(uiState: EditCoInsuredState) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  Column {
    uiState.member?.let {
      InsuredRow(
        displayName = it.displayName,
        details = it.ssn,
        hasMissingInfo = false,
        isMember = true,
        onRemove = { },
        onEdit = { },
      )
    }
    Divider()
    uiState.coInsured.forEachIndexed { index, coInsured ->
      InsuredRow(
        displayName = coInsured.displayName,
        details = coInsured.details(dateTimeFormatter),
        hasMissingInfo = coInsured.hasMissingInfo,
        isMember = false,
        onRemove = { },
        onEdit = { },
      )
      if (index < uiState.coInsured.size - 1) {
        Divider()
      }
    }
  }
}

@Composable
@HedvigPreview
private fun EditCoInsuredScreenPreview() {
  HedvigTheme {
    Surface {
      EditCoInsuredScreen(
        navigateUp = { },
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
