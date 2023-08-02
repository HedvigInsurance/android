package com.hedvig.android.feature.insurances.insurancedetail

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.animation.animateContentHeight
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.insurance.ContractType
import com.hedvig.android.core.ui.insurance.toDrawableRes
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.feature.insurances.insurancedetail.coverage.CoverageTab
import com.hedvig.android.feature.insurances.insurancedetail.documents.DocumentsTab
import com.hedvig.android.feature.insurances.insurancedetail.yourinfo.YourInfoTab
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
internal fun ContractDetailDestination(
  viewModel: ContractDetailViewModel,
  onEditCoInsuredClick: () -> Unit,
  onChangeAddressClick: () -> Unit,
  onCancelInsuranceClick: (ContractDetails.CancelInsuranceData) -> Unit,
  openWebsite: (Uri) -> Unit,
  navigateUp: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState: ContractDetailsUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ContractDetailScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    retry = viewModel::retryLoadingContract,
    onEditCoInsuredClick = onEditCoInsuredClick,
    onChangeAddressClick = onChangeAddressClick,
    onCancelInsuranceClick = onCancelInsuranceClick,
    openWebsite = openWebsite,
    navigateUp = navigateUp,
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContractDetailScreen(
  uiState: ContractDetailsUiState,
  imageLoader: ImageLoader,
  retry: () -> Unit,
  onEditCoInsuredClick: () -> Unit,
  onChangeAddressClick: () -> Unit,
  onCancelInsuranceClick: (ContractDetails.CancelInsuranceData) -> Unit,
  openWebsite: (Uri) -> Unit,
  navigateUp: () -> Unit,
) {
  Column {
    TopAppBarWithBack(
      title = "",
      onClick = navigateUp,
    )
    val pagerState = rememberPagerState()
    Box(Modifier.weight(1f)) {
      when (uiState) {
        ContractDetailsUiState.Error -> {
          HedvigErrorSection(retry = retry)
        }
        ContractDetailsUiState.Loading -> {}
        is ContractDetailsUiState.Success -> {
          LazyColumn(
            contentPadding = WindowInsets
              .safeDrawing
              .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
              .asPaddingValues()
              .plus(PaddingValues(top = 16.dp)),
            modifier = Modifier.consumeWindowInsets(
              WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
            ),
          ) {
            item(
              key = 1,
              contentType = "InsuranceCard",
            ) {
              val contractCardData = uiState.contractDetails.contractCardData
              InsuranceCard(
                chips = contractCardData.chips,
                topText = contractCardData.title,
                bottomText = contractCardData.subtitle,
                imageLoader = imageLoader,
                modifier = Modifier.padding(horizontal = 16.dp),
                fallbackPainter = contractCardData.contractType.toDrawableRes().let { drawableRes ->
                  painterResource(id = drawableRes)
                },
              )
            }
            item(key = 2, contentType = "space") { Spacer(Modifier.height(16.dp)) }
            stickyHeader(key = 3, contentType = "PagerSelector") { PagerSelector(pagerState) }
            item(
              key = 4,
              contentType = "Pager",
            ) {
              HorizontalPager(
                pageCount = 3,
                state = pagerState,
                key = { it },
                verticalAlignment = Alignment.Top,
                modifier = Modifier.animateContentHeight(spring(stiffness = Spring.StiffnessLow)),
              ) { pageIndex ->
                when (pageIndex) {
                  0 -> {
                    YourInfoTab(
                      coverageItems = uiState.contractDetails.overviewItems,
                      cancelInsuranceData = uiState.contractDetails.cancelInsuranceData,
                      allowEditCoInsured = uiState.contractDetails.allowEditCoInsured,
                      onEditCoInsuredClick = onEditCoInsuredClick,
                      onChangeAddressClick = onChangeAddressClick,
                      onCancelInsuranceClick = onCancelInsuranceClick,
                    )
                  }
                  1 -> {
                    CoverageTab(
                      uiState.contractDetails.insurableLimits,
                      uiState.contractDetails.perils,
                    )
                  }
                  2 -> {
                    DocumentsTab(
                      documents = uiState.contractDetails.documents,
                      onDocumentClicked = openWebsite,
                    )
                  }
                  else -> {}
                }
              }
            }
          }
        }
      }
      HedvigFullScreenCenterAlignedProgress(show = uiState is ContractDetailsUiState.Loading)
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerSelector(pagerState: PagerState) {
  LocalConfiguration.current
  val resources = LocalContext.current.resources
  val couroutineScope = rememberCoroutineScope()
  TabRow(
    selectedTabIndex = pagerState.currentPage,
    containerColor = MaterialTheme.colorScheme.background,
    contentColor = MaterialTheme.colorScheme.onBackground,
    modifier = Modifier.fillMaxWidth(),
  ) {
    remember {
      listOf(
        resources.getString(R.string.insurance_details_view_tab_1_title),
        resources.getString(R.string.insurance_details_view_tab_2_title),
        resources.getString(R.string.insurance_details_view_tab_3_title),
      )
    }.mapIndexed { index, tabTitle ->
      Tab(
        selected = pagerState.currentPage == index,
        onClick = {
          couroutineScope.launch {
            pagerState.animateScrollToPage(index)
          }
        },
        text = {
          Text(text = tabTitle, style = MaterialTheme.typography.bodyMedium)
        },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewContractDetailScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ContractDetailScreen(
        uiState = ContractDetailsUiState.Success(
          ContractDetails(
            contractCardData = ContractDetails.ContractCardData(
              contractId = "asd",
              backgroundImageUrl = null,
              chips = persistentListOf(
                "Activates 20.03.2024",
                "Activates 21.03.2024",
              ),
              title = "Home Insurance",
              subtitle = "Bellmansgatan 19A ∙ You +1",
              contractType = ContractType.HOMEOWNER,
            ),
            overviewItems = persistentListOf(),
            cancelInsuranceData = ContractDetails.CancelInsuranceData("", ""),
            allowEditCoInsured = true,
            insurableLimits = persistentListOf(),
            perils = persistentListOf(),
            documents = persistentListOf(),
          ),
        ),
        imageLoader = rememberPreviewImageLoader(),
        retry = {},
        onEditCoInsuredClick = {},
        onChangeAddressClick = {},
        onCancelInsuranceClick = {},
        openWebsite = {},
        navigateUp = {},
      )
    }
  }
}
