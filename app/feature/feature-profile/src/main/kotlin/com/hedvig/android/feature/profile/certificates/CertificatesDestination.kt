package com.hedvig.android.feature.profile.certificates

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.MultipleDocuments
import com.hedvig.android.feature.profile.tab.ProfileRow
import hedvig.resources.R

@Composable
internal fun CertificatesDestination(
  viewModel: CertificatesViewModel,
  navigateUp: () -> Unit,
  onNavigateToInsuranceEvidence: () -> Unit,
  onNavigateToTravelCertificate: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  CertificatesScreen(
    uiState,
    navigateUp = navigateUp,
    onNavigateToInsuranceEvidence = onNavigateToInsuranceEvidence,
    onNavigateToTravelCertificate = onNavigateToTravelCertificate,
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
  onRetry: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.profile_certificates_title),
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
) {
  if (uiState.isTravelCertificateAvailable) {
    ProfileRow(
      title = stringResource(R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
      icon = HedvigIcons.MultipleDocuments,
      onClick = dropUnlessResumed { onNavigateToTravelCertificate() },
      isLoading = false,
    )
  }
  if (uiState.isInsuranceEvidenceAvailable) {
    ProfileRow(
      title = stringResource(R.string.INSURANCE_EVIDENCE_DOCUMENT_TITLE),
      icon = HedvigIcons.MultipleDocuments,
      onClick = dropUnlessResumed { onNavigateToInsuranceEvidence() },
      isLoading = false,
    )
  }
}

@Composable
private fun ColumnScope.CertificatesRowsPlaceholder() {
  ProfileRow(
    title = stringResource(R.string.PROFILE_ROW_TRAVEL_CERTIFICATE),
    icon = HedvigIcons.MultipleDocuments,
    onClick = {},
    isLoading = true,
  )
  ProfileRow(
    title = stringResource(R.string.INSURANCE_EVIDENCE_DOCUMENT_TITLE),
    icon = HedvigIcons.MultipleDocuments,
    onClick = { },
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
      ),
      CertificatesState.Success(
        isTravelCertificateAvailable = true,
        isInsuranceEvidenceAvailable = false,
      ),
      CertificatesState.Success(
        isTravelCertificateAvailable = false,
        isInsuranceEvidenceAvailable = true,
      ),
      CertificatesState.Failure,
      CertificatesState.Loading,
    ),
  )
