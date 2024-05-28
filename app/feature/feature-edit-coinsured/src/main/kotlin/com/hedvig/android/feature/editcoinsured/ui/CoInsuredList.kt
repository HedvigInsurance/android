package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.ui.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.feature.editcoinsured.data.CoInsured
import hedvig.resources.R

@Composable
internal fun CoInsuredList(
  uiState: EditCoInsuredState.Loaded.CoInsuredListState,
  onRemove: (CoInsured) -> Unit,
  onEdit: (CoInsured) -> Unit,
  allowEdit: Boolean,
  modifier: Modifier = Modifier,
) {
  val dateTimeFormatter = rememberHedvigBirthDateDateTimeFormatter()
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
      )
    }

    uiState.coInsured.forEach { coInsured ->
      HorizontalDivider()

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
      )
    }
  }
}
