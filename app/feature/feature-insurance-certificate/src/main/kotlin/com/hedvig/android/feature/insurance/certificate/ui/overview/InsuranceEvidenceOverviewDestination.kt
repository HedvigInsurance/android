package com.hedvig.android.feature.insurance.certificate.ui.overview

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewState.Success
import hedvig.resources.R
import java.io.File

@Composable
internal fun InsuranceEvidenceOverviewDestination(
  insuranceEvidenceUrl: String,
  viewModel: InsuranceEvidenceOverviewViewModel,
  navigateUp: () -> Unit,
  onShareInsuranceEvidence: (File) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  InsuranceEvidenceOverview(
    insuranceEvidenceUrl = insuranceEvidenceUrl,
    onDownloadCertificate = { viewModel.emit(InsuranceEvidenceOverviewEvent.OnDownloadCertificate(it)) },
    navigateUp = navigateUp,
    onShareInsuranceEvidence = onShareInsuranceEvidence,
    uiState = uiState,
    onRetry = {
      viewModel.emit(InsuranceEvidenceOverviewEvent.RetryLoadData)
    },
  )
}

@Composable
internal fun InsuranceEvidenceOverview(
  uiState: InsuranceEvidenceOverviewState,
  insuranceEvidenceUrl: String,
  onDownloadCertificate: (String) -> Unit,
  navigateUp: () -> Unit,
  onShareInsuranceEvidence: (File) -> Unit,
  onRetry: () -> Unit,
) {
  when (uiState) {
    InsuranceEvidenceOverviewState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
        topAppBarActionType = TopAppBarActionType.CLOSE,
      ) {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier.weight(1f),
        )
      }
    }

    InsuranceEvidenceOverviewState.Loading -> {
      HedvigFullScreenCenterAlignedProgress(modifier = Modifier.fillMaxSize())
    }

    is InsuranceEvidenceOverviewState.Success -> {
      LaunchedEffect(uiState.insuranceEvidenceUri) {
        uiState.insuranceEvidenceUri?.let {
          onShareInsuranceEvidence(it)
        }
      }
      HedvigScaffold(
        navigateUp,
        itemsColumnHorizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = stringResource(R.string.CERTIFICATES_EMAIL_SENT),
          description = stringResource(R.string.INSURANCE_EVIDENCE_EMAIL_SENT_DESCRIPTION),
          iconStyle = SUCCESS,
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        HedvigButton(
          text = stringResource(R.string.CERTIFICATES_DOWNLOAD),
          onClick = {
            if (uiState.insuranceEvidenceUri != null) {
              onShareInsuranceEvidence(uiState.insuranceEvidenceUri)
            } else {
              onDownloadCertificate(insuranceEvidenceUrl)
            }
          },
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(id = R.string.general_close_button),
          onClick = navigateUp,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceEvidenceOverview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsuranceEvidenceOverview(
        insuranceEvidenceUrl = "",
        onDownloadCertificate = {},
        navigateUp = {},
        onShareInsuranceEvidence = {},
        uiState = Success(null),
        onRetry = {},
      )
    }
  }
}
