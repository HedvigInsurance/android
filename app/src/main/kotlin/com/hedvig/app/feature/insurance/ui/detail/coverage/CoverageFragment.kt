package com.hedvig.app.feature.insurance.ui.detail.coverage

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.ContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.insurance.PerilGrid
import com.hedvig.android.core.ui.insurance.PerilGridData
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailCoverageFragmentBinding
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilBottomSheet
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CoverageFragment : Fragment(R.layout.contract_detail_coverage_fragment) {
  private val contractId: String by lazy {
    arguments?.getString(INSURANCE_ID) ?: error("Call CoverageFragment.newInstance() instead")
  }
  private val binding by viewBinding(ContractDetailCoverageFragmentBinding::bind)
  private val viewModel: CoverageViewModel by viewModel { parametersOf(contractId) }
  private val imageLoader: ImageLoader by inject()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    binding.composeView.setContent {
      HedvigTheme {
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        CoverageFragmentScreen(
          uiState = uiState,
          imageLoader = imageLoader,
          onPerilClick = { peril ->
            PerilBottomSheet
              .newInstance(peril)
              .show(
                parentFragmentManager,
                PerilBottomSheet.TAG,
              )
          },
          onMoreInfoClick = { moreInfo ->
            InsurableLimitsBottomSheet
              .newInstance(moreInfo.label, moreInfo.description)
              .show(parentFragmentManager, InsurableLimitsBottomSheet.TAG)
          },
        )
      }
    }
  }

  companion object {
    const val INSURANCE_ID = "com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageFragment.INSURANCE_ID"

    fun newInstance(contractId: String): CoverageFragment {
      return CoverageFragment().apply {
        arguments = bundleOf(INSURANCE_ID to contractId)
      }
    }
  }
}

@Composable
private fun CoverageFragmentScreen(
  uiState: CoverageUiState,
  imageLoader: ImageLoader,
  onPerilClick: (Peril) -> Unit,
  onMoreInfoClick: (ContractCoverage.MoreInfo) -> Unit,
) {
  when (uiState) {
    CoverageUiState.Error -> {}
    CoverageUiState.Loading -> {}
    is CoverageUiState.Success -> {
      SuccessScreen(
        uiState = uiState,
        imageLoader = imageLoader,
        onPerilClick = onPerilClick,
        onMoreInfoClick = onMoreInfoClick,
      )
    }
  }
}

@Composable
private fun SuccessScreen(
  uiState: CoverageUiState.Success,
  imageLoader: ImageLoader,
  onPerilClick: (Peril) -> Unit,
  onMoreInfoClick: (ContractCoverage.MoreInfo) -> Unit,
) {
  Column {
    Spacer(Modifier.height(8.dp))
    PerilSection(uiState, imageLoader, onPerilClick)
    Spacer(Modifier.height(32.dp))
    Divider(Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(8.dp))
    MoreInfoSection(uiState, onMoreInfoClick)
    Spacer(Modifier.height(16.dp))
    Spacer(
      Modifier.windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
      ),
    )
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.PerilSection(
  uiState: CoverageUiState.Success,
  imageLoader: ImageLoader,
  onPerilClick: (Peril) -> Unit,
) {
  Text(
    text = stringResource(hedvig.resources.R.string.CONTRACT_COVERAGE_CONTRACT_TYPE, uiState.contractDisplayName),
    style = MaterialTheme.typography.titleLarge,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .heightIn(min = 40.dp)
      .wrapContentSize(Alignment.BottomStart),
  )
  Spacer(Modifier.height(16.dp))
  PerilGrid(
    perils = uiState.perilItems.map { peril: Peril ->
      PerilGridData(
        text = peril.title,
        iconUrl = if (isSystemInDarkTheme()) peril.darkUrl else peril.lightUrl,
        onClick = { onPerilClick(peril) },
      )
    },
    imageLoader = imageLoader,
    contentPadding = PaddingValues(horizontal = 16.dp),
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.MoreInfoSection(
  uiState: CoverageUiState.Success,
  onMoreInfoClick: (ContractCoverage.MoreInfo) -> Unit,
) {
  Text(
    text = stringResource(hedvig.resources.R.string.CONTRACT_COVERAGE_MORE_INFO),
    style = MaterialTheme.typography.titleLarge,
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .heightIn(min = 40.dp)
      .wrapContentSize(Alignment.BottomStart),
  )
  Spacer(Modifier.height(16.dp))
  uiState.moreInfoItems.map { insurableLimitItem ->
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .heightIn(64.dp)
        .clickable {
          onMoreInfoClick(insurableLimitItem)
        }
        .padding(horizontal = 16.dp),
    ) {
      Text(insurableLimitItem.label, modifier = Modifier.weight(1f))
      Spacer(Modifier.widthIn(min = 8.dp))
      CompositionLocalProvider(LocalContentColor provides LocalContentColor.current.copy(alpha = ContentAlpha.medium)) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.End) {
          Text(insurableLimitItem.limit)
          Spacer(Modifier.width(8.dp))
          Icon(Icons.Outlined.Info, null, Modifier.size(24.dp))
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCoverageFragmentScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      CoverageFragmentScreen(
        CoverageUiState.Success(
          "Your insurance",
          List(7) { Peril("Fire", "Descr", "", "", emptyList(), emptyList(), "Info") },
          List(3) { ContractCoverage.MoreInfo("Label", "limit", "Description") },
        ),
        rememberPreviewImageLoader(),
        {},
        {},
      )
    }
  }
}
