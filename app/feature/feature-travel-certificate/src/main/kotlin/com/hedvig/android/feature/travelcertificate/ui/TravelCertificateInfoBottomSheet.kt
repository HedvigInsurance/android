package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import hedvig.resources.R

@Composable
internal fun TravelCertificateInfoBottomSheet(sheetState: HedvigBottomSheetState<Unit>) {
  HedvigBottomSheet(sheetState) {
    TravelCertificateInfoBottomSheetContent(sheetState::dismiss)
  }
}

@Composable
private fun TravelCertificateInfoBottomSheetContent(onDismiss: () -> Unit) {
  Column {
    HedvigText(
      text = stringResource(R.string.travel_certificate_info_title),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigText(
      text = stringResource(R.string.travel_certificate_info_subtitle),
      color = HedvigTheme.colorScheme.textSecondary,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(16.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_close_button),
      onClick = onDismiss,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@HedvigPreview
@Composable
private fun PreviewTravelCertificateInfoBottomSheetContent() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TravelCertificateInfoBottomSheetContent({})
    }
  }
}
