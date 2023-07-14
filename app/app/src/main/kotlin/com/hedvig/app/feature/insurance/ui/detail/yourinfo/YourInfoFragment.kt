package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.android.table.Table
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.insurance.GradientType
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.terminateinsurance.TerminateInsuranceActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailYourInfoFragmentBinding
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewState
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class YourInfoFragment : Fragment(R.layout.contract_detail_your_info_fragment) {

  private val binding by viewBinding(ContractDetailYourInfoFragmentBinding::bind)
  private val viewModel: ContractDetailViewModel by activityViewModel()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    binding.composeView.setContent {
      HedvigTheme(useNewColorScheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
          val uiState by viewModel.viewState.collectAsStateWithLifecycle()
          YourInfoTab(
            uiState = uiState,
            onEditInfoClick = { /* todo open bottom sheet */ },
            onCancelInsuranceClick = ::openCancelInsuranceScreen,
            retry = viewModel::retryLoadingContract,
          )
        }
      }
    }
  }

  private fun openCancelInsuranceScreen(cancelInsuranceData: YourInfoModel.CancelInsuranceData) {
    TerminateInsuranceActivity.newInstance(
      requireContext(),
      cancelInsuranceData.insuranceId,
      cancelInsuranceData.insuranceDisplayName,
    )
  }
}

@Composable
internal fun YourInfoTab(
  uiState: ContractDetailViewModel.ViewState,
  onEditInfoClick: () -> Unit,
  onCancelInsuranceClick: (YourInfoModel.CancelInsuranceData) -> Unit,
  retry: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    ContractDetailViewModel.ViewState.Error -> {
      HedvigErrorSection(
        retry = retry,
        modifier = modifier.fillMaxSize(),
      )
    }
    ContractDetailViewModel.ViewState.Loading -> {}
    is ContractDetailViewModel.ViewState.Success -> {
      val coverageRowItems = remember(uiState.state.memberDetailsViewState.detailsTable.sections) {
        uiState.state
          .memberDetailsViewState
          .detailsTable
          .sections
          .flatMap { it.tableRows.map { it.title to it.value } }
          .toPersistentList()
      }
      Column(modifier) {
        Spacer(Modifier.height(8.dp))
        CoverageRows(coverageRowItems, Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          text = stringResource(hedvig.resources.R.string.CONTRACT_EDIT_INFO_LABEL),
          onClick = onEditInfoClick,
          colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
          ),
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        val cancelInsurance = uiState.state.memberDetailsViewState.cancelInsurance
        if (cancelInsurance != null) {
          Spacer(Modifier.height(8.dp))
          HedvigTextButton(
            text = stringResource(hedvig.resources.R.string.TERMINATION_BUTTON),
            onClick = { onCancelInsuranceClick(cancelInsurance) },
            colors = ButtonDefaults.textButtonColors(
              contentColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    }
  }
  HedvigFullScreenCenterAlignedProgress(show = uiState is ContractDetailViewModel.ViewState.Loading)
}

@Composable
private fun CoverageRows(
  coverageRowItems: ImmutableList<Pair<String, String>>,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    coverageRowItems.forEachIndexed { index, (firstText, secondText) ->
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(firstText)
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(vertical = 16.dp),
          ) {
            Text(
              text = secondText,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.End,
            )
          }
        },
        spaceBetween = 8.dp,
      )
      if (index != coverageRowItems.lastIndex) {
        Divider()
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewYourInfoTab(
  @PreviewParameter(UiStateProvider::class) uiState: ContractDetailViewModel.ViewState,
) {
  HedvigTheme(useNewColorScheme = true) {
    androidx.compose.material.Surface(color = MaterialTheme.colorScheme.background) {
      YourInfoTab(
        uiState = uiState,
        onEditInfoClick = {},
        onCancelInsuranceClick = {},
        retry = {},
      )
    }
  }
}

private class UiStateProvider : CollectionPreviewParameterProvider<ContractDetailViewModel.ViewState>(
  listOf(
    ContractDetailViewModel.ViewState.Loading,
    ContractDetailViewModel.ViewState.Error,
    ContractDetailViewModel.ViewState.Success(
      ContractDetailViewState(
        ContractCardViewState(
          id = "",
          firstStatusPillText = null,
          secondStatusPillText = null,
          gradientType = GradientType.HOME,
          displayName = "",
          detailPills = listOf(),
          logoUrls = null,
        ),
        ContractDetailViewState.MemberDetailsViewState(
          pendingAddressChange = null,
          detailsTable = Table(
            title = "",
            sections = listOf(
              Table.Section(
                title = "",
                tableRows = listOf(
                  Table.TableRow("Address".repeat(4), "", "Bellmansgatan 19A"),
                  Table.TableRow("Postal code", "", "118 47".repeat(6)),
                  Table.TableRow("Type", "", "Homeowner"),
                  Table.TableRow("Size", "", "56 m2"),
                  Table.TableRow("Co-insured".repeat(4), "", "You +1".repeat(5)),
                ),
              ),
            ),
          ),
          changeAddressButton = null,
          change = null,
          cancelInsurance = YourInfoModel.CancelInsuranceData("", ""),
        ),
        ContractDetailViewState.DocumentsViewState(documents = listOf()),
      ),
    ),
  ),
)
