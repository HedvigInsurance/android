package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri


@Composable
fun TravelCertificateOverView(
  travelCertificateUrl: TravelCertificateUri,
  navigateBack: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = {
      navigateBack()
    },
    modifier = Modifier.clearFocusOnTap(),
  ) {

  }
}
