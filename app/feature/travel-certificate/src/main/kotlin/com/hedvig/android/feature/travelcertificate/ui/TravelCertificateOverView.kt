package com.hedvig.android.feature.travelcertificate.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl

@Composable
fun TravelCertificateOverView(
  travelCertificateUrl: TravelCertificateUrl,
  navigateBack: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = {
      navigateBack()
    },
    modifier = Modifier.clearFocusOnTap(),
  ) {
    Text(travelCertificateUrl.url)
  }
}
