package com.hedvig.android.feature.addon.purchase.ui.success

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Large
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.NoButton
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDateTimeFormatterDefaults
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun SubmitAddonSuccessScreen(activationDate: LocalDate, popBackStack: () -> Unit) {
  val locale = getLocale()
  val formattedDate = remember(activationDate, locale) {
    HedvigDateTimeFormatterDefaults
      .dateMonthAndYear(locale).format(activationDate.toJavaLocalDate())
  }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .windowInsetsPadding(
        WindowInsets.safeDrawing.only(
          WindowInsetsSides.Horizontal +
            WindowInsetsSides.Bottom,
        ),
      ),
  ) {
    Spacer(Modifier.weight(1f))
    EmptyState(
      modifier = Modifier.fillMaxWidth(),
      text = stringResource(R.string.ADDON_FLOW_SUCCESS_TITLE),
      description = stringResource(
        R.string.ADDON_FLOW_SUCCESS_SUBTITLE,
        formattedDate,
      ),
      iconStyle = SUCCESS,
      buttonStyle = NoButton,
    )
    Spacer(Modifier.weight(1f))
    HedvigTextButton(
      stringResource(R.string.general_close_button),
      onClick = dropUnlessResumed { popBackStack() },
      buttonSize = Large,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun SubmitTierSuccessScreenPreview() {
  SubmitAddonSuccessScreen(LocalDate(2024, 9, 23), {})
}
