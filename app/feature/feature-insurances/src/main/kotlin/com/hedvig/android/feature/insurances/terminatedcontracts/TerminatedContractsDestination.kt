package com.hedvig.android.feature.insurances.terminatedcontracts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.ui.createChips
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@Composable
internal fun TerminatedContractsDestination(
  viewModel: TerminatedContractsViewModel,
  navigateToContractDetail: (contractId: String) -> Unit,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TerminatedContractsScreen(
    uiState = uiState,
    onContractClick = navigateToContractDetail,
    navigateUp = navigateUp,
    retry = viewModel::retry,
    imageLoader = imageLoader,
  )
}

@Composable
private fun TerminatedContractsScreen(
  uiState: TerminatedContractsUiState,
  onContractClick: (contractId: String) -> Unit,
  navigateUp: () -> Unit,
  retry: () -> Unit,
  imageLoader: ImageLoader,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.insurances_tab_cancelled_insurances_title),
  ) {
    Spacer(Modifier.height(16.dp))
    when (uiState) {
      TerminatedContractsUiState.Error -> {
        HedvigErrorSection(retry)
      }
      TerminatedContractsUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress()
      }
      TerminatedContractsUiState.NoTerminatedInsurances -> {
        HedvigErrorSection(
          buttonText = stringResource(R.string.general_back_button),
          onButtonClick = navigateUp,
        )
      }
      is TerminatedContractsUiState.Success -> {
        VectorInfoCard(
          text = stringResource(R.string.insurances_tab_cancelled_insurances_note),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(16.dp))
        for ((index, contract) in uiState.insuranceContracts.withIndex()) {
          InsuranceCard(
            chips = contract.createChips(),
            topText = contract.currentInsuranceAgreement.productVariant.displayName,
            bottomText = contract.exposureDisplayName,
            imageLoader = imageLoader,
            shape = MaterialTheme.shapes.squircleMedium,
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .clip(MaterialTheme.shapes.squircleMedium)
              .clickable {
                onContractClick(contract.id)
              },
            isLoading = false,
          )
          if (index != uiState.insuranceContracts.lastIndex) {
            Spacer(Modifier.height(8.dp))
          }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewTerminatedContractsScreen(
  @PreviewParameter(PreviewTerminatedContractsUiStateProvider::class) uiState: TerminatedContractsUiState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      TerminatedContractsScreen(uiState, {}, {}, {}, rememberPreviewImageLoader())
    }
  }
}

private class PreviewTerminatedContractsUiStateProvider :
  CollectionPreviewParameterProvider<TerminatedContractsUiState>(
    listOf(
      TerminatedContractsUiState.Loading,
      TerminatedContractsUiState.NoTerminatedInsurances,
      TerminatedContractsUiState.Error,
      TerminatedContractsUiState.Success(
        persistentListOf(
          InsuranceContract(
            "1",
            "Test123",
            exposureDisplayName = "Test exposure",
            inceptionDate = LocalDate.fromEpochDays(200),
            terminationDate = LocalDate.fromEpochDays(400),
            currentInsuranceAgreement = InsuranceAgreement(
              activeFrom = LocalDate.fromEpochDays(240),
              activeTo = LocalDate.fromEpochDays(340),
              displayItems = persistentListOf(),
              productVariant = ProductVariant(
                displayName = "Variant",
                contractGroup = ContractGroup.RENTAL,
                contractType = ContractType.SE_APARTMENT_RENT,
                partner = null,
                perils = persistentListOf(),
                insurableLimits = persistentListOf(),
                documents = persistentListOf(),
              ),
              certificateUrl = null,
              coInsured = persistentListOf(),
              creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
            ),
            upcomingInsuranceAgreement = null,
            renewalDate = LocalDate.fromEpochDays(500),
            supportsAddressChange = false,
            supportsEditCoInsured = true,
            isTerminated = true,
            contractHolderDisplayName = "Hugo Linder",
            contractHolderSSN = "19910113-1093",
          ),
        ),
      ),
    ),
  )
