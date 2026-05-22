package com.hedvig.android.feature.purchase.apartment.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@PreviewTest
@Preview(name = "apartment_form_filled", showBackground = true)
@Composable
fun PreviewApartmentFormFilledScreenshot() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ApartmentFormContent(
        street = "Storgatan 1",
        zipCode = "12345",
        livingSpace = "65",
        numberCoInsured = 1,
        streetError = null,
        zipCodeError = null,
        livingSpaceError = null,
        isSubmitting = false,
        onStreetChanged = {},
        onZipCodeChanged = {},
        onLivingSpaceChanged = {},
        onNumberCoInsuredChanged = {},
        onSubmit = {},
      )
    }
  }
}
