package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.TravelCertificateUiState
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
fun AddCoInsured(
  selectedCoInsuredId: String?,
  uiState: TravelCertificateUiState,
  onSsnChanged: (String) -> Unit,
  onNameChanged: (String) -> Unit,
  onSave: () -> Unit,
) {

  val selectedCoInsured = uiState.coInsured.input.firstOrNull { it.id == selectedCoInsuredId }

  HedvigScaffold(
    navigateUp = {},
    modifier = Modifier.clearFocusOnTap(),
  ) {
    Spacer(modifier = Modifier.height(48.dp))
    Text(
      text = "Add co-insured",
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(64.dp))
    HedvigTextField(
      value = selectedCoInsured?.name ?: "",
      onValueChange = { onNameChanged(it) },
      errorText = "",
      label = {
        Text("Full name")
      },
      maxLines = 1,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextField(
      value = selectedCoInsured?.ssn ?: "",
      onValueChange = { onSsnChanged(it) },
      errorText = "",
      label = {
        Text("YYYYMMDD-XXXX")
      },
      maxLines = 1,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )

    Spacer(modifier = Modifier.weight(1f))
    LargeContainedButton(
      onClick = onSave,
      shape = MaterialTheme.shapes.squircle,
      modifier = Modifier.padding(horizontal = 16.dp),
    ) {
      Text(stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL))
    }
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
fun AddCoInsuredPreview() {
  HedvigTheme {
    Surface {
      AddCoInsured(
        selectedCoInsuredId = "",
        uiState = TravelCertificateUiState(
          email = ValidatedInput(input = null),
          travelDate = ValidatedInput(input = null),
          coInsured = ValidatedInput(input = listOf()),
          includeMember = true,
          travelCertificateSpecifications = TravelCertificateResult.TravelCertificateSpecifications(
            contractId = "123",
            email = "hugo@hedvig.com",
            maxDurationDays = 3,
            dateRange = LocalDate(2023, 5, 23)..LocalDate(2023, 7, 23),
            numberOfCoInsured = 2,
          ),
        ),
        onSsnChanged = {},
        onNameChanged = {},
        onSave = {},
      )
    }
  }
}
