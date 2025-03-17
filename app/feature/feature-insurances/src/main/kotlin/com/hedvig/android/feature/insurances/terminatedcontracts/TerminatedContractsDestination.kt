package com.hedvig.android.feature.insurances.terminatedcontracts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.InsuranceCard
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.insurances.data.AbstractInsuranceContract.InsuranceContract
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.ui.createChips
import hedvig.resources.R
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
    retry = { viewModel.emit(TerminatedContractsEvent.Retry) },
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
        for ((index, contract) in uiState.insuranceContracts.withIndex()) {
          InsuranceCard(
            chips = contract.createChips(),
            topText = contract.currentInsuranceAgreement.productVariant.displayName,
            bottomText = contract.exposureDisplayName,
            imageLoader = imageLoader,
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .clip(HedvigTheme.shapes.cornerLarge)
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
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
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
        listOf(
          InsuranceContract(
            "1",
            "Test123",
            tierName = "Premium",
            exposureDisplayName = "Test exposure",
            inceptionDate = LocalDate.fromEpochDays(200),
            terminationDate = LocalDate.fromEpochDays(400),
            currentInsuranceAgreement = InsuranceAgreement(
              activeFrom = LocalDate.fromEpochDays(240),
              activeTo = LocalDate.fromEpochDays(340),
              displayItems = listOf(),
              productVariant = ProductVariant(
                displayName = "Variant",
                contractGroup = ContractGroup.RENTAL,
                contractType = ContractType.SE_APARTMENT_RENT,
                partner = null,
                perils = listOf(),
                insurableLimits = listOf(),
                documents = listOf(),
                displayTierName = "Standard",
                tierDescription = "Our most standard coverage",
                termsVersion = "SE_DOG_STANDARD-20230330-HEDVIG-null",
              ),
              certificateUrl = null,
              coInsured = listOf(),
              creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
              addons = null,
            ),
            upcomingInsuranceAgreement = null,
            renewalDate = LocalDate.fromEpochDays(500),
            supportsAddressChange = false,
            supportsEditCoInsured = true,
            isTerminated = true,
            contractHolderDisplayName = "Hugo Linder",
            contractHolderSSN = "19910113-1093",
            supportsTierChange = false,
          ),
        ),
      ),
    ),
  )
