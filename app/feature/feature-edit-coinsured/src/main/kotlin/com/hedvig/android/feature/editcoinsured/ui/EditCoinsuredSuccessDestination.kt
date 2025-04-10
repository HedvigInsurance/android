package com.hedvig.android.feature.editcoinsured.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.datepicker.hedvigDateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun EditCoInsuredSuccessDestination(date: LocalDate?, navigateUp: () -> Unit, navigateBack: () -> Unit) {
  HedvigScaffold(navigateUp = navigateUp, Modifier.fillMaxSize()) {
    Spacer(Modifier.height(8.dp))
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
    ) {
      EmptyState(
        text = stringResource(id = hedvig.resources.R.string.CONTRACT_ADD_COINSURED_UPDATED_TITLE),
        description = stringResource(
          id = hedvig.resources.R.string.CONTRACT_ADD_COINSURED_UPDATED_LABEL,
          date?.toJavaLocalDate()?.format(hedvigDateTimeFormatter(getLocale())) ?: "-",
        ),
        iconStyle = EmptyStateDefaults.EmptyStateIconStyle.SUCCESS,
        modifier = Modifier.align(Alignment.Center),
      )
    }
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(id = hedvig.resources.R.string.general_done_button),
      onClick = navigateBack,
      modifier = Modifier
        .padding(start = 16.dp, end = 16.dp)
        .fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewEditCoInsuredSuccessDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EditCoInsuredSuccessDestination(
        date = LocalDate.fromEpochDays(3000),
        navigateUp = {},
        navigateBack = {},
      )
    }
  }
}
