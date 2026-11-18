package com.hedvig.android.feature.greencard.ui.codrivers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.ErrorDialog
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.greencard.data.CreateGreenCardError
import com.hedvig.android.feature.greencard.ui.common.AddressFields
import com.hedvig.android.feature.greencard.ui.common.RequiredTextField
import hedvig.resources.FULL_NAME_TEXT
import hedvig.resources.GENERAL_REMOVE
import hedvig.resources.Res
import hedvig.resources.general_error
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreenCardCoDriversInputDestination(viewModel: GreenCardCoDriversInputViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  GreenCardCoDriversInputScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onAddCoDriver = { viewModel.emit(GreenCardCoDriversInputEvent.AddCoDriver) },
    onRemoveCoDriver = { viewModel.emit(GreenCardCoDriversInputEvent.RemoveCoDriver(it)) },
    onDismissCreateError = { viewModel.emit(GreenCardCoDriversInputEvent.DismissCreateError) },
    submit = { viewModel.emit(GreenCardCoDriversInputEvent.Submit) },
  )
}

@Composable
private fun GreenCardCoDriversInputScreen(
  uiState: GreenCardCoDriversInputUiState,
  navigateUp: () -> Unit,
  onAddCoDriver: () -> Unit,
  onRemoveCoDriver: (CoDriverFieldsState) -> Unit,
  onDismissCreateError: () -> Unit,
  submit: () -> Unit,
) {
  val createError = uiState.createError
  if (createError != null) {
    ErrorDialog(
      title = stringResource(Res.string.general_error),
      message = when (createError) {
        is CreateGreenCardError.UserError -> createError.message
        CreateGreenCardError.Unknown -> stringResource(Res.string.something_went_wrong)
      },
      onDismiss = onDismissCreateError,
    )
  }
  HedvigScaffold(navigateUp = navigateUp, modifier = Modifier.clearFocusOnTap()) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      // TODO: Add "Who else will drive?" / "Vem mer ska köra?" to Lokalise
      "Who else will drive?",
      // TODO: Add "Add everyone else who will drive the car abroad. Skip this if you're the only driver." / "Lägg till alla andra som ska köra bilen utomlands. Hoppa över det här om du är den enda föraren." to Lokalise
      "Add everyone else who will drive the car abroad. Skip this if you're the only driver.",
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    uiState.coDrivers.forEachIndexed { index, coDriver ->
      key(coDriver) {
        Spacer(Modifier.height(24.dp))
        CoDriverFields(
          number = index + 1,
          coDriver = coDriver,
          onRemove = { onRemoveCoDriver(coDriver) },
          showMissingFieldErrors = uiState.showMissingFieldErrors,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
    }
    Spacer(Modifier.height(24.dp))
    HedvigButton(
      // TODO: Add "Add co-driver" / "Lägg till medförare" to Lokalise
      text = "Add co-driver",
      onClick = onAddCoDriver,
      enabled = true,
      buttonStyle = Secondary,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigButton(
      // TODO: Add "Create green card" / "Skapa grönt kort" to Lokalise
      text = "Create green card",
      onClick = submit,
      enabled = true,
      isLoading = uiState.isCreating,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun CoDriverFields(
  number: Int,
  coDriver: CoDriverFieldsState,
  onRemove: () -> Unit,
  showMissingFieldErrors: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      HedvigText(
        // TODO: Add "Co-driver %d" / "Medförare %d" to Lokalise
        text = "Co-driver $number",
        modifier = Modifier
          .weight(1f)
          .semantics { heading() },
      )
      HedvigTextButton(
        text = stringResource(Res.string.GENERAL_REMOVE),
        onClick = onRemove,
        buttonSize = ButtonSize.Small,
      )
    }
    RequiredTextField(
      state = coDriver.name,
      labelText = stringResource(Res.string.FULL_NAME_TEXT),
      showMissingFieldError = showMissingFieldErrors,
    )
    AddressFields(
      address = coDriver.address,
      showMissingFieldErrors = showMissingFieldErrors,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewGreenCardCoDriversInput() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GreenCardCoDriversInputScreen(
        uiState = GreenCardCoDriversInputUiState(
          coDrivers = listOf(CoDriverFieldsState(name = TextFieldState("Anna Andersson"))),
          showMissingFieldErrors = true,
          isCreating = false,
          createError = null,
        ),
        navigateUp = {},
        onAddCoDriver = {},
        onRemoveCoDriver = {},
        onDismissCreateError = {},
        submit = {},
      )
    }
  }
}
