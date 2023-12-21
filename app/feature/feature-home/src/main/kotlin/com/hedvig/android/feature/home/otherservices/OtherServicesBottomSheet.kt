package com.hedvig.android.feature.home.otherservices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.ClickableOption
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.material3.onWarningContainer
import com.hedvig.android.core.designsystem.material3.squircleLargeTop
import com.hedvig.android.core.designsystem.material3.warningContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.commonclaim.CommonClaimsData
import com.hedvig.android.feature.home.emergency.EmergencyData
import com.hedvig.android.feature.home.home.ui.HomeUiState
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val BOTTOMSHEET_DISMISS_DELAY = 200L

@Composable
internal fun OtherServicesBottomSheet(
  uiState: HomeUiState.Success,
  dismissBottomSheet: () -> Unit,
  onStartMovingFlow: () -> Unit,
  onEmergencyClicked: (EmergencyData) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  navigateToHelpCenter: () -> Unit,
  sheetState: SheetState,
) {
  val coroutineScope = rememberCoroutineScope()

  ModalBottomSheet(
    containerColor = MaterialTheme.colorScheme.background,
    onDismissRequest = dismissBottomSheet,
    shape = MaterialTheme.shapes.squircleLargeTop,
    sheetState = sheetState,
    tonalElevation = 0.dp,
    windowInsets = BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Top),
  ) {
    OtherServicesBottomSheetContent(
      showMovingFlow = uiState.allowAddressChange,
      isHelpCenterEnabled = uiState.isHelpCenterEnabled,
      onStartMovingFlow = {
        coroutineScope.launch {
          dismissBottomSheet()
          delay(BOTTOMSHEET_DISMISS_DELAY)
          onStartMovingFlow()
        }
      },
      showGenerateTravelcertificate = uiState.allowGeneratingTravelCertificate,
      onGenerateTravelCertificateClicked = {
        coroutineScope.launch {
          dismissBottomSheet()
          delay(BOTTOMSHEET_DISMISS_DELAY)
          onGenerateTravelCertificateClicked()
        }
      },
      emergencyData = uiState.emergencyData,
      onEmergencyClicked = { it: EmergencyData ->
        coroutineScope.launch {
          dismissBottomSheet()
          delay(BOTTOMSHEET_DISMISS_DELAY)
          onEmergencyClicked(it)
        }
      },
      commonClaims = uiState.commonClaimsData,
      onOpenCommonClaim = {
        coroutineScope.launch {
          dismissBottomSheet()
          delay(BOTTOMSHEET_DISMISS_DELAY)
          onOpenCommonClaim(it)
        }
      },
      navigateToHelpCenter = {
        coroutineScope.launch {
          dismissBottomSheet()
          delay(BOTTOMSHEET_DISMISS_DELAY)
          navigateToHelpCenter()
        }
      },
      dismissBottomSheet = dismissBottomSheet,
    )
  }
}

@Composable
private fun OtherServicesBottomSheetContent(
  showMovingFlow: Boolean,
  isHelpCenterEnabled: Boolean,
  onStartMovingFlow: () -> Unit,
  showGenerateTravelcertificate: Boolean,
  onGenerateTravelCertificateClicked: () -> Unit,
  emergencyData: EmergencyData?,
  onEmergencyClicked: (EmergencyData) -> Unit,
  commonClaims: ImmutableList<CommonClaimsData>,
  onOpenCommonClaim: (CommonClaimsData) -> Unit,
  navigateToHelpCenter: () -> Unit,
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
      if (isHelpCenterEnabled) {
        ClickableOption("Help center", navigateToHelpCenter) // todo help-center: localize
      }
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
          text = stringResource(R.string.COMMON_CLAIM_EMERGENCY_TITLE),
          onClick = { onEmergencyClicked(emergencyData) },
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
@HedvigPreview
private fun PreviewOtherServicesBottomSheetContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      OtherServicesBottomSheetContent(
        showMovingFlow = true,
        isHelpCenterEnabled = true,
        onStartMovingFlow = {},
        showGenerateTravelcertificate = true,
        onGenerateTravelCertificateClicked = {},
        emergencyData = EmergencyData("Sick abroad", "+46123456789"),
        onEmergencyClicked = {},
        commonClaims = persistentListOf(
          CommonClaimsData(
            id = "",
            title = "Contact FirstVet",
            bulletPoints = emptyList(),
          ),
        ),
        onOpenCommonClaim = {},
        navigateToHelpCenter = {},
        dismissBottomSheet = {},
      )
    }
  }
}
