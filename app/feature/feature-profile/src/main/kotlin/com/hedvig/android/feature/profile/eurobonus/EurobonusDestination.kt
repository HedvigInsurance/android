package com.hedvig.android.feature.profile.eurobonus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.warningElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.scaffold.HedvigScaffold

@Composable
internal fun EurobonusDestination(viewModel: EurobonusViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.isEligibleForEurobonus) {
    if (uiState.isEligibleForEurobonus == false) {
      navigateUp()
    }
  }
  val focusManager = LocalFocusManager.current
  EurobonusScreen(
    setEurobonusText = { viewModel.emit(EurobonusEvent.UpdateEurobonusValue(it)) },
    uiState = uiState,
    onSubmitEurobonus = {
      focusManager.clearFocus()
      viewModel.emit(EurobonusEvent.SubmitEurobonus)
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun EurobonusScreen(
  uiState: EurobonusUiState,
  setEurobonusText: (String) -> Unit,
  onSubmitEurobonus: () -> Unit,
  navigateUp: () -> Unit,
) {
  Box(
    propagateMinConstraints = true,
    modifier = Modifier.fillMaxSize(),
  ) {
    HedvigScaffold(
      topAppBarText = stringResource(hedvig.resources.R.string.sas_integration_title),
      navigateUp = navigateUp,
      modifier = Modifier.clearFocusOnTap(),
    ) {
      Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .heightIn(80.dp)
            .fillMaxWidth(),
      ) {
        Text(
          text = stringResource(hedvig.resources.R.string.sas_integration_connect_your_eurobonus),
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier.padding(16.dp),
        )
      }
      EurobonusNumberField(
        canSubmit = uiState.canSubmit,
        canEditText = uiState.canEditText,
        hasError = uiState.hasError ?: false,
        number = uiState.eurobonusText,
        onSubmitEurobonus = onSubmitEurobonus,
        setEurobonusText = setEurobonusText,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.sas_integration_info),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
      HedvigContainedButton(
        text = stringResource(hedvig.resources.R.string.general_save_button),
        enabled = uiState.canSubmit,
        onClick = { onSubmitEurobonus() },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
    HedvigFullScreenCenterAlignedProgressDebounced(show = uiState.isLoading)
  }
}

@Composable
private fun EurobonusNumberField(
  number: String,
  hasError: Boolean,
  canEditText: Boolean,
  canSubmit: Boolean,
  setEurobonusText: (String) -> Unit,
  onSubmitEurobonus: () -> Unit,
) {
  HedvigTextField(
    value = number,
    onValueChange = { newInput ->
      if (newInput.indices.all { newInput[it].isWhitespace().not() }) {
        setEurobonusText(newInput)
      }
    },
    enabled = canEditText,
    label = {
      Text(
        buildString {
          append(stringResource(hedvig.resources.R.string.sas_integration_title))
          append(" ")
          append(stringResource(hedvig.resources.R.string.sas_integration_number))
        },
      )
    },
    supportingText = {
      if (hasError) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Hedvig.WarningFilled,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.warningElement,
          )
          Spacer(Modifier.width(6.dp))
          Text(stringResource(hedvig.resources.R.string.something_went_wrong))
        }
      } else {
        Text(stringResource(hedvig.resources.R.string.sas_integration_number_placeholder))
      }
    },
    isError = hasError,
    keyboardOptions = KeyboardOptions(
      capitalization = KeyboardCapitalization.Characters,
      keyboardType = KeyboardType.Text,
      imeAction = ImeAction.Done,
    ),
    keyboardActions = KeyboardActions(
      onDone = {
        if (canSubmit) {
          onSubmitEurobonus()
        }
      },
    ),
    withNewDesign = true,
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
  )
}

@HedvigPreview
@Composable
private fun PreviewEurobonusScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) hasError: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      EurobonusScreen(
        EurobonusUiState(
          eurobonusText = "ABC-123",
          canSubmit = true,
          isLoading = true,
          canEditText = true,
          hasError = hasError,
        ),
        {},
        {},
        {},
      )
    }
  }
}
