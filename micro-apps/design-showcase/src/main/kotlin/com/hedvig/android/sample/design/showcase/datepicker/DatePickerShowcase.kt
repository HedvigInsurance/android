package com.hedvig.android.sample.design.showcase.datepicker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePicker
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerImmutableState
import java.util.Locale
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
fun DatePickerShowcase() {
  val locale = getLocale()
  var state by remember {
    mutableStateOf(
      HedvigDatePickerImmutableState(
        selectedDateMillis = null,
        displayedMonthMillis = null,
        yearRange = 2024..2025,
        minDateInMillis = LocalDate(2024, 8, 1).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        maxDateInMillis = LocalDate(2024, 9, 30).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        locale = locale,
      ),
    )
  }
  var showDatePicker by rememberSaveable { mutableStateOf(false) }
  var selectedDate by rememberSaveable { mutableStateOf<Long?>(null) }
  Surface(
    modifier = Modifier
      .fillMaxSize(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(
      modifier = Modifier
        .safeContentPadding()
        .fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        text = "Open date picker",
        enabled = true,
        onClick = {
          showDatePicker = true
        },
      )
      Spacer(Modifier.height(16.dp))
      HedvigText(
        text = "selectedDate: ${selectedDate?.let {
          Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC)
        } ?: 0} ",
      )
      HedvigDatePicker(
        datePickerState = state,
        isVisible = showDatePicker,
        onConfirmRequest = {
          showDatePicker = false
          state = state.copy(selectedDateMillis = selectedDate)
        },
        onDismissRequest = {
          showDatePicker = false
        },
        onSelectedDateChanged = {
          selectedDate = it
        },
      )
    }
  }
}

@Composable
@ReadOnlyComposable
fun getLocale(): Locale {
  val configuration = LocalConfiguration.current
  return ConfigurationCompat.getLocales(configuration).get(0) ?: LocaleListCompat.getDefault()[0]!!
}
