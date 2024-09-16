package com.hedvig.android.feature.terminateinsurance.step.terminationsuccess

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationSuccessDestination(terminationDate: LocalDate?, onDone: () -> Unit) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    val connection = remember {
      object : NestedScrollConnection {}
    }
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxSize()
        .nestedScroll(connection)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(
          WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal +
              WindowInsetsSides.Bottom,
          ),
        ),
    ) {
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      EmptyState(
        text = stringResource(id = R.string.TERMINATION_FLOW_SUCCESS_TITLE),
        description = terminationDate?.let {
          stringResource(
            R.string.TERMINATION_FLOW_SUCCESS_SUBTITLE_WITH_DATE,
            rememberHedvigDateTimeFormatter().format(terminationDate.toJavaLocalDate()),
          )
        } ?: stringResource(id = R.string.TERMINATE_CONTRACT_TERMINATION_COMPLETE),
        iconStyle = SUCCESS,
      )
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.weight(1f))
      Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
      ) {
        HedvigButton(
          text = stringResource(id = R.string.general_done_button),
          enabled = true,
          onClick = onDone,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }

      Spacer(Modifier.height(16.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationSuccessScreen(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider::class,
  ) withTerminationDate: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TerminationSuccessDestination(
        terminationDate = if (withTerminationDate) {
          LocalDate.fromEpochDays(300)
        } else {
          null
        },
        {},
      )
    }
  }
}
