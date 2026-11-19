package com.hedvig.android.feature.greencard.ui.address

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.clearFocusOnTap
import com.hedvig.android.feature.greencard.data.Country
import com.hedvig.android.feature.greencard.data.GreenCardAddress
import com.hedvig.android.feature.greencard.ui.common.AddressFields
import com.hedvig.android.feature.greencard.ui.common.AddressFieldsState
import hedvig.resources.Res
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreenCardAddressInputDestination(navigateUp: () -> Unit, onContinue: (GreenCardAddress) -> Unit) {
  val street = rememberTextFieldState()
  val postalCode = rememberTextFieldState()
  val city = rememberTextFieldState()
  val country = rememberSaveable { mutableStateOf<Country?>(Country.SWEDEN) }
  val address = remember(street, postalCode, city, country) { AddressFieldsState(street, postalCode, city, country) }
  var showMissingFieldErrors by rememberSaveable { mutableStateOf(false) }
  GreenCardAddressInputScreen(
    address = address,
    showMissingFieldErrors = showMissingFieldErrors,
    navigateUp = navigateUp,
    submit = {
      showMissingFieldErrors = !address.isComplete()
      if (!showMissingFieldErrors) onContinue(address.toAddress())
    },
  )
}

@Composable
private fun GreenCardAddressInputScreen(
  address: AddressFieldsState,
  showMissingFieldErrors: Boolean,
  navigateUp: () -> Unit,
  submit: () -> Unit,
) {
  HedvigScaffold(navigateUp = navigateUp, modifier = Modifier.clearFocusOnTap()) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      // TODO: Add "Where should we send the green card?" / "Vart ska vi skicka det gröna kortet?" to Lokalise
      "Where should we send the green card?",
      // TODO: Add "We'll post the printed card to this address." / "Vi skickar det tryckta kortet till den här adressen." to Lokalise
      "We'll post the printed card to this address.",
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(24.dp))
    AddressFields(
      address = address,
      showMissingFieldErrors = showMissingFieldErrors,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = submit,
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigPreview
@Composable
private fun PreviewGreenCardAddressInput() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GreenCardAddressInputScreen(
        address = AddressFieldsState(
          street = rememberTextFieldState("Storgatan 1"),
          postalCode = rememberTextFieldState("111 22"),
        ),
        showMissingFieldErrors = true,
        navigateUp = {},
        submit = {},
      )
    }
  }
}
