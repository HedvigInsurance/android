package com.hedvig.android.feature.movingflow.ui.successfulmove

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun SuccessfulMoveDestination(moveDate: LocalDate, navigateUp: () -> Unit, popBackStack: () -> Unit) {
  val locale = getLocale()
  val formattedDate = remember(moveDate, locale) {
    HedvigDateTimeFormatterDefaults.dateMonthAndYear(locale).format(moveDate.toJavaLocalDate())
  }
  HedvigScaffold(navigateUp) {
    EmptyState(
      text = stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_TITLE),
      description = stringResource(R.string.TIER_FLOW_COMMIT_PROCESSING_DESCRIPTION, formattedDate),
      iconStyle = SUCCESS,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .wrapContentHeight(Alignment.CenterVertically)
        .padding(horizontal = 16.dp),
    )
    HedvigTextButton(
      text = stringResource(id = R.string.general_close_button),
      onClick = { popBackStack() },
      buttonSize = Large,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
        .padding(horizontal = 16.dp),
    )
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewSuccessfulMoveDestination() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SuccessfulMoveDestination(LocalDate(2022, 1, 1), {}, {})
    }
  }
}
