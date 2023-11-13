package com.hedvig.android.feature.home.otherservices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.common.android.ThemedIconUrls
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claims.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.claims.commonclaim.EmergencyData
import com.hedvig.android.feature.home.home.ui.HomeUiState
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import octopus.type.HedvigColor

@Composable
internal fun OtherServicesBottomSheet(
  uiState: HomeUiState.Success,
  dismissBottomSheet: () -> Unit,
  onChatClicked: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onEmergencyClaimClicked: (EmergencyData) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  sheetState: SheetState,
) {
  ModalBottomSheet(
    containerColor = MaterialTheme.colorScheme.background,
    onDismissRequest = dismissBottomSheet,
    shape = MaterialTheme.shapes.squircleLargeTop,
    sheetState = sheetState,
    tonalElevation = 0.dp,
    windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
  ) {
    OtherServicesBottomSheetContent(
      onChatClicked = {
        dismissBottomSheet()
        onChatClicked()
      },
      showMovingFlow = uiState.allowAddressChange,
      onStartMovingFlow = {
        dismissBottomSheet()
        onStartMovingFlow()
      },
      showGenerateTravelcertificate = uiState.allowGeneratingTravelCertificate,
      onGenerateTravelCertificateClicked = {
        dismissBottomSheet()
        onGenerateTravelCertificateClicked()
      },
      emergencyData = uiState.emergencyData,
      onEmergencyClaimClicked = { it: EmergencyData ->
        dismissBottomSheet()
        onEmergencyClaimClicked(it)
      },
      commonClaims = uiState.commonClaimsData,
      onOpenCommonClaim = onOpenCommonClaim,
      dismissBottomSheet = dismissBottomSheet,
    )
  }
}

@Composable
private fun OtherServicesBottomSheetContent(
  onChatClicked: () -> Unit,
  showMovingFlow: Boolean,
  onStartMovingFlow: () -> Unit,
  showGenerateTravelcertificate: Boolean,
  onGenerateTravelCertificateClicked: () -> Unit,
  emergencyData: EmergencyData?,
  onEmergencyClaimClicked: (EmergencyData) -> Unit,
  commonClaims: ImmutableList<CommonClaimsData>,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  dismissBottomSheet: () -> Unit,
) {
  Column(Modifier.padding(horizontal = 16.dp)) {
    Text(
      text = stringResource(id = R.string.home_tab_other_services),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(24.dp))
    Column(
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      ClickableOption(stringResource(R.string.CHAT_TITLE), onChatClicked)
      if (showMovingFlow) {
        ClickableOption(stringResource(R.string.insurance_details_change_address_button), onStartMovingFlow)
      }
      if (showGenerateTravelcertificate) {
        ClickableOption(stringResource(R.string.travel_certificate_card_title), onGenerateTravelCertificateClicked)
      }
      for (commonClaim in commonClaims) {
        ClickableOption(commonClaim.title, { onOpenCommonClaim(commonClaim) })
      }
      if (emergencyData != null) {
        ClickableOption(
          text = emergencyData.title,
          onClick = { onEmergencyClaimClicked(emergencyData) },
          cardColors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.warningContainer,
            contentColor = MaterialTheme.colorScheme.onWarningContainer,
          ),
        )
      }
    }
    Spacer(Modifier.height(16.dp))
    HedvigTextButton(
      text = stringResource(R.string.general_close_button),
      onClick = dismissBottomSheet,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun ClickableOption(
  text: String,
  onClick: () -> Unit,
  cardColors: CardColors = CardDefaults.outlinedCardColors(),
) {
  HedvigCard(
    onClick = onClick,
    colors = cardColors,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(56.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
      )
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewOtherServicesBottomSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      OtherServicesBottomSheetContent(
        onChatClicked = {},
        showMovingFlow = true,
        onStartMovingFlow = {},
        showGenerateTravelcertificate = true,
        onGenerateTravelCertificateClicked = {},
        emergencyData = EmergencyData(ThemedIconUrls("", ""), HedvigColor.UNKNOWN__, "Sick abroad", true, ""),
        onEmergencyClaimClicked = {},
        commonClaims = persistentListOf(
          CommonClaimsData(
            id = "",
            iconUrls = ThemedIconUrls("", ""),
            title = "Contact FirstVet",
            color = HedvigColor.DarkPurple,
            layoutTitle = "",
            buttonText = "",
            eligibleToClaim = true,
            bulletPoints = emptyList(),
          ),
        ),
        onOpenCommonClaim = {},
        dismissBottomSheet = {},
      )
    }
  }
}
