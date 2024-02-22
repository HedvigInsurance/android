package com.hedvig.android.feature.travelcertificate.ui.generate

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import hedvig.resources.R
import java.util.UUID

@Composable
internal fun AddCoInsured(
  coInsured: CoInsured?,
  navigateUp: () -> Unit,
  onRemoveCoInsured: (String) -> Unit,
  onEditCoInsured: (CoInsured) -> Unit,
  onAddCoInsured: (CoInsured) -> Unit,
) {
  var name by rememberSaveable { mutableStateOf(coInsured?.name ?: "") }
  var ssn by rememberSaveable { mutableStateOf(coInsured?.ssn ?: "") }
  var hasNameError by rememberSaveable { mutableStateOf<Boolean>(false) }
  var hasSsnError by rememberSaveable { mutableStateOf<Boolean>(false) }

  HedvigScaffold(
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    Spacer(modifier = Modifier.height(48.dp))
    Text(
      text = if (coInsured != null) {
        stringResource(id = R.string.travel_certificate_edit_member_title)
      } else {
        stringResource(id = R.string.travel_certificate_change_member_title)
      },
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(64.dp))
    HedvigTextField(
      value = name,
      onValueChange = {
        name = it
        hasNameError = false
      },
      errorText = if (hasNameError) {
        stringResource(id = R.string.travel_certificate_name_error_label)
      } else {
        null
      },
      label = {
        Text(stringResource(id = R.string.travel_certificate_full_name_label))
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(8.dp))
    HedvigTextField(
      value = ssn,
      onValueChange = {
        ssn = it
        hasSsnError = false
      },
      errorText = if (hasSsnError) {
        stringResource(id = R.string.travel_certificate_ssn_error_label)
      } else {
        null
      },
      label = {
        Text(stringResource(id = R.string.travel_certificate_ssn_label))
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )

    Spacer(modifier = Modifier.weight(1f))

    if (coInsured != null) {
      TextButton(
        onClick = { onRemoveCoInsured(coInsured.id) },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        Text("Remove", color = MaterialTheme.colorScheme.error)
      }
      Spacer(modifier = Modifier.height(8.dp))
    }

    HedvigContainedButton(
      text = stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
      onClick = {
        if (name.isBlank()) {
          hasNameError = true
        }

        if (ssn.isBlank()) {
          hasSsnError = true
        }

        if (!hasSsnError && !hasNameError) {
          if (coInsured != null) {
            val updatedCoInsured = coInsured.copy(
              name = name,
              ssn = ssn,
            )
            onEditCoInsured(updatedCoInsured)
          } else {
            val newCoInsured = CoInsured(
              id = UUID.randomUUID().toString(),
              name = name,
              ssn = ssn,
            )
            onAddCoInsured(newCoInsured)
          }
        }
      },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(32.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewAddCoInsured() {
  HedvigTheme {
    Surface {
      AddCoInsured(
        coInsured = null,
        navigateUp = {},
        onEditCoInsured = {},
        onAddCoInsured = {},
        onRemoveCoInsured = {},
      )
    }
  }
}
