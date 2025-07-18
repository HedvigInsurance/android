package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import com.hedvig.android.feature.editcoinsured.data.Member
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun CoInsuredList(
  uiState: EditCoInsuredState.Loaded.CoInsuredListState,
  onRemove: (CoInsured) -> Unit,
  onEdit: (CoInsured) -> Unit,
  allowEdit: Boolean,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigBirthDateDateTimeFormatter()
  val contentPadding = PaddingValues(horizontal = 16.dp)
  Column(modifier = modifier) {
    uiState.member?.let {
      InsuredRow(
        displayName = it.displayName,
        identifier = it.identifier() ?: "",
        hasMissingInfo = false,
        allowEdit = false,
        isMember = true,
        onRemove = {},
        onEdit = {},
        contentPadding = contentPadding,
      )
    }

    uiState.coInsured.forEach { coInsured ->
      HorizontalDivider(Modifier.padding(contentPadding))

      InsuredRow(
        displayName = coInsured.displayName.ifBlank { stringResource(id = R.string.CONTRACT_COINSURED) },
        identifier = coInsured.identifier(dateTimeFormatter)
          ?: stringResource(id = R.string.CONTRACT_NO_INFORMATION),
        hasMissingInfo = coInsured.hasMissingInfo,
        isMember = false,
        allowEdit = allowEdit,
        onRemove = {
          onRemove(coInsured)
        },
        onEdit = {
          onEdit(coInsured)
        },
        contentPadding = contentPadding,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCoInsuredList() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CoInsuredList(
        EditCoInsuredState.Loaded.CoInsuredListState(
          originalCoInsured = listOf(
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
          member = Member(
            firstName = "Member",
            lastName = "Membersson",
            ssn = "197312331093",
          ),
          allCoInsured = listOf(),
        ),
        {},
        {},
        true,
      )
    }
  }
}
