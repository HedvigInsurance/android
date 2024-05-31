package com.hedvig.android.feature.terminateinsurance.step.terminationsuccess

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.success.HedvigSuccessSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.CircleWithCheckmarkFilled
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun TerminationSuccessDestination(terminationDate: LocalDate?, onDone: () -> Unit) {
  Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      Spacer(Modifier.height(16.dp))
      HedvigSuccessSection(
        title = stringResource(id = R.string.TERMINATION_FLOW_SUCCESS_TITLE),
        modifier = Modifier
          .weight(1f),
        contentPadding = PaddingValues(0.dp),
        subTitle = terminationDate?.let {
          stringResource(
            R.string.TERMINATION_FLOW_SUCCESS_SUBTITLE_WITH_DATE,
            rememberHedvigDateTimeFormatter().format(terminationDate.toJavaLocalDate()),
          )
        } ?: stringResource(id = R.string.TERMINATE_CONTRACT_TERMINATION_COMPLETE),
        withDefaultVerticalSpacing = false,
        iconImageVector = Icons.Hedvig.CircleWithCheckmarkFilled,
        iconModifier = Modifier.size(32.dp),
      )
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(id = R.string.general_done_button),
        onClick = onDone,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
      Spacer(
        Modifier.padding(
          WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom).asPaddingValues(),
        ),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminationSuccessScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withTerminationDate: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
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
