package com.hedvig.android.feature.home.otherservices

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.app.feature.home.model.CommonClaim

@Composable
fun OtherServicesBottomSheet(
  options: List<CommonClaim>,
  onChatClicked: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onEmergencyClaimClicked: (EmergencyData) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onCommonClaimClicked: (CommonClaimsData) -> Unit,
  onDismiss: () -> Unit,
  sheetState: SheetState,
) {
  ModalBottomSheet(
    containerColor = MaterialTheme.colorScheme.background,
    onDismissRequest = {
      onDismiss()
    },
    sheetState = sheetState,
    tonalElevation = 0.dp,
  ) {
    OtherServicesBottomSheetContent(
      options = options,
      onDismiss = onDismiss,
      onClick = {
        when (it) {
          CommonClaim.Chat -> onChatClicked()
          is CommonClaim.TitleAndBulletPoints -> onCommonClaimClicked(it.inner)
          is CommonClaim.Emergency -> onEmergencyClaimClicked(it.inner)
          CommonClaim.ChangeAddress -> onStartMovingFlow()
          CommonClaim.GenerateTravelCertificate -> onGenerateTravelCertificateClicked()
        }
      },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
  }
}
