package com.hedvig.android.feature.terminateinsurance.step.deletion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.feature.terminateinsurance.ui.TerminationOverviewScreenScaffold
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun InsuranceDeletionDestination(activeFrom: LocalDate, onContinue: () -> Unit, navigateUp: () -> Unit) {
  TerminationOverviewScreenScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.TERMINATE_CONTRACT_CONFIRMATION_TITLE),
  ) {
    Spacer(Modifier.weight(1f))
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(horizontal = 28.dp),
    ) {
      Icon(
        imageVector = Icons.Hedvig.WarningFilled,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.warningElement,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = stringResource(id = R.string.GENERAL_ARE_YOU_SURE),
        textAlign = TextAlign.Center,
        style = LocalTextStyle.current.copy(
          lineBreak = LineBreak.Heading,
        ),
        modifier = Modifier.fillMaxWidth(),
      )
      val dateTimeFormatter = rememberHedvigDateTimeFormatter()
      Spacer(Modifier.height(2.dp))
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Text(
          text = stringResource(
            id = R.string.TERMINATE_CONTRACT_DELETION_TEXT,
            dateTimeFormatter.format(activeFrom.toJavaLocalDate()),
          ),
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(24.dp))
    }
    Spacer(Modifier.weight(1f))
    HedvigContainedButton(
      text = stringResource(id = R.string.TERMINATE_CONTRACT_DELETION_CONTINUE_BUTTON),
      onClick = onContinue,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(bottom = 8.dp),
    )
    HedvigTextButton(
      text = stringResource(id = R.string.general_back_button),
      onClick = navigateUp,
      modifier = Modifier
        .padding(horizontal = 16.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceDeletionScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      InsuranceDeletionDestination(
        activeFrom = LocalDate.fromEpochDays(300),
        onContinue = {},
        navigateUp = {},
      )
    }
  }
}
