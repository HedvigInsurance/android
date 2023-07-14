package com.hedvig.app.feature.insurance.ui.detail.documents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.android.table.Table
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.core.ui.insurance.GradientType
import com.hedvig.android.core.ui.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.feature.terminateinsurance.TerminateInsuranceActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.ContractDetailDocumentsFragmentBinding
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewState
import com.hedvig.app.util.extensions.tryOpenUri
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class DocumentsFragment : Fragment(R.layout.contract_detail_documents_fragment) {
  private val binding by viewBinding(ContractDetailDocumentsFragmentBinding::bind)
  private val viewModel: ContractDetailViewModel by activityViewModel()

  private val registerForActivityResult: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
      if (activityResult.resultCode == Activity.RESULT_OK) {
        requireActivity().onBackPressedDispatcher.onBackPressed()
      }
    }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
    binding.composeView.setContent {
      HedvigTheme(useNewColorScheme = true) {
        Surface(
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier.fillMaxSize(),
        ) {
          val uiState by viewModel.viewState.collectAsStateWithLifecycle()
          DocumentsScreen(
            uiState = uiState,
            retry = viewModel::retryLoadingContract,
          )
        }
      }
    }
  }

  private fun openCancelInsuranceScreen(insuranceId: String, insuranceDisplayName: String) {
    registerForActivityResult.launch(
      TerminateInsuranceActivity.newInstance(requireContext(), insuranceId, insuranceDisplayName),
    )
  }
}

@Composable
fun DocumentsScreen(
  uiState: ContractDetailViewModel.ViewState,
  retry: () -> Unit,
) {
  when (uiState) {
    ContractDetailViewModel.ViewState.Error -> {
      HedvigErrorSection(retry = retry)
    }
    ContractDetailViewModel.ViewState.Loading -> {}
    is ContractDetailViewModel.ViewState.Success -> {
      val context = LocalContext.current
      Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))) {
        Spacer(Modifier.height(16.dp))
        val documents = uiState.state.documentsViewState.documents.filterIsInstance<DocumentItems.Document>()
        for ((index, document) in documents.withIndex()) {
          DocumentCard(
            onClick = {
              val uri = Uri.parse(document.uriString)
              context.tryOpenUri(uri)
            },
            title = document.getTitle(context),
            subtitle = document.getSubTitle(context),
          )
          if (index != documents.lastIndex) {
            Spacer(Modifier.height(4.dp))
          }
        }
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    }
  }
  HedvigFullScreenCenterAlignedProgress(show = uiState is ContractDetailViewModel.ViewState.Loading)
}

@Composable
private fun DocumentCard(
  onClick: () -> Unit,
  title: String?,
  subtitle: String?,
) {
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          text = buildAnnotatedString {
            val text = title ?: return@buildAnnotatedString
            append(text)
            append(" ")
            withStyle(
              SpanStyle(
                baselineShift = BaselineShift(0.3f),
                fontSize = 10.sp,
              ),
            ) {
              append("PDF")
            }
          },
        )
        Text(
          text = subtitle ?: "",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(Modifier.width(8.dp))
      Icon(
        imageVector = Icons.Hedvig.ArrowNorthEast,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDocumentsScreen(
  @PreviewParameter(UiStateProvider::class) uiState: ContractDetailViewModel.ViewState,
) {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      DocumentsScreen(uiState, {})
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
            sections = listOf(),
          ),
          changeAddressButton = null,
          changeCoInsured = null,
          cancelInsuranceData = null,
        ),
        ContractDetailViewState.DocumentsViewState(
          documents = listOf(
            DocumentItems.Document(
              title = "Terms & Conditions",
              titleRes = null,
              subtitle = "All details about your coverage",
              subTitleRes = null,
              uriString = "",
            ),
            DocumentItems.Document(
              title = "Pre-purchase info",
              titleRes = null,
              subtitle = "All pre-pruchase details",
              subTitleRes = null,
              uriString = "",
            ),
            DocumentItems.Document(
              title = "Productinfo (IPID)",
              titleRes = null,
              subtitle = "Compare your coverage",
              subTitleRes = null,
              uriString = "",
            ),
          ),
        ),
      ),
    ),
  ),
)
