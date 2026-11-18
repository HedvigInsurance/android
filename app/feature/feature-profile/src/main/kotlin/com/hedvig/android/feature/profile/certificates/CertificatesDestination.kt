package com.hedvig.android.feature.profile.certificates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.feature.profile.tab.ProfileRow
import hedvig.resources.INSURANCE_EVIDENCE_DOCUMENT_TITLE
import hedvig.resources.PROFILE_ROW_TRAVEL_CERTIFICATE
import hedvig.resources.Res
import hedvig.resources.profile_certificates_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CertificatesDestination(
  viewModel: CertificatesViewModel,
  navigateUp: () -> Unit,
  onNavigateToInsuranceEvidence: () -> Unit,
  onNavigateToTravelCertificate: () -> Unit,
  onNavigateToGreenCard: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  CertificatesScreen(
    uiState,
    navigateUp = navigateUp,
    onNavigateToInsuranceEvidence = onNavigateToInsuranceEvidence,
    onNavigateToTravelCertificate = onNavigateToTravelCertificate,
    onNavigateToGreenCard = onNavigateToGreenCard,
    onRetry = {
      viewModel.emit(CertificatesEvent.Retry)
    },
  )
}

@Composable
private fun CertificatesScreen(
  uiState: CertificatesState,
  navigateUp: () -> Unit,
  onNavigateToInsuranceEvidence: () -> Unit,
  onNavigateToTravelCertificate: () -> Unit,
  onNavigateToGreenCard: () -> Unit,
  onRetry: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(Res.string.profile_certificates_title),
  ) {
    when (uiState) {
      CertificatesState.Failure -> {
        Box(Modifier.weight(1f)) {
          HedvigErrorSection(
            onButtonClick = onRetry,
            modifier = Modifier.fillMaxSize(),
          )
        }
      }

      CertificatesState.Loading -> {
        CertificatesRowsPlaceholder()
      }

      is CertificatesState.Success -> {
        CertificatesRows(
          uiState,
          onNavigateToInsuranceEvidence = onNavigateToInsuranceEvidence,
          onNavigateToTravelCertificate = onNavigateToTravelCertificate,
          onNavigateToGreenCard = onNavigateToGreenCard,
        )
      }
    }
  }
}

@Composable
private fun ColumnScope.CertificatesRows(
  uiState: CertificatesState.Success,
  onNavigateToInsuranceEvidence: () -> Unit,
  onNavigateToTravelCertificate: () -> Unit,
  onNavigateToGreenCard: () -> Unit,
) {
  if (uiState.isTravelCertificateAvailable) {
    ProfileRow(
      title = stringResource(Res.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
      icon = HedvigIcons.Document,
      onClick = dropUnlessResumed { onNavigateToTravelCertificate() },
      isLoading = false,
    )
  }
  if (uiState.isInsuranceEvidenceAvailable) {
    ProfileRow(
      title = stringResource(Res.string.INSURANCE_EVIDENCE_DOCUMENT_TITLE),
      icon = HedvigIcons.Document,
      onClick = dropUnlessResumed { onNavigateToInsuranceEvidence() },
      isLoading = false,
    )
  }
  if (uiState.isGreenCardAvailable) {
    ProfileRow(
      // TODO: Add "Green card" / "Grönt kort" to Lokalise
      title = "Green card",
      icon = HedvigIcons.Document,
      onClick = dropUnlessResumed { onNavigateToGreenCard() },
      isLoading = false,
    )
  }
}

@Composable
private fun ColumnScope.CertificatesRowsPlaceholder() {
  ProfileRow(
    title = stringResource(Res.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
    icon = HedvigIcons.Document,
    onClick = {},
    isLoading = true,
  )
  ProfileRow(
    title = stringResource(Res.string.INSURANCE_EVIDENCE_DOCUMENT_TITLE),
    icon = HedvigIcons.Document,
    onClick = { },
    isLoading = true,
  )
  ProfileRow(
    // TODO: Add "Green card" / "Grönt kort" to Lokalise
    title = "Green card",
    icon = HedvigIcons.Document,
    onClick = {},
    isLoading = true,
  )
}

@HedvigPreview
@Composable
private fun PreviewInsuranceEvidenceEmailInputScreen(
  @PreviewParameter(CertificatesStateProvider::class) state: CertificatesState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      CertificatesScreen(
        state,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class CertificatesStateProvider :
  CollectionPreviewParameterProvider<CertificatesState>(
    listOf(
      CertificatesState.Success(
        isTravelCertificateAvailable = true,
        isInsuranceEvidenceAvailable = true,
        isGreenCardAvailable = true,
      ),
      CertificatesState.Success(
        isTravelCertificateAvailable = true,
        isInsuranceEvidenceAvailable = false,
        isGreenCardAvailable = false,
      ),
      CertificatesState.Success(
        isTravelCertificateAvailable = false,
        isInsuranceEvidenceAvailable = true,
        isGreenCardAvailable = true,
      ),
      CertificatesState.Failure,
      CertificatesState.Loading,
    ),
  )
