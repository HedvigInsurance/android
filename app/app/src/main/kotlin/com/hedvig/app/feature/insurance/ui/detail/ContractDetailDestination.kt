package com.hedvig.app.feature.insurance.ui.detail

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.common.android.table.Table
import com.hedvig.android.core.designsystem.animation.animateContentHeight
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.insurance.GradientType
import com.hedvig.android.core.ui.insurance.toDrawable
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageTab
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageViewModel
import com.hedvig.app.feature.insurance.ui.detail.documents.DocumentsTab
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoTab
import hedvig.resources.R
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch

@Composable
internal fun ContractDetailDestination(
  viewModel: ContractDetailViewModel,
  coverageViewModel: CoverageViewModel,
  imageLoader: ImageLoader,
  onEditCoInsuredClick: () -> Unit,
  onChangeAddressClick: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState: ContractDetailViewModel.ViewState by viewModel.viewState.collectAsStateWithLifecycle()
  ContractDetailScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    retry = viewModel::retryLoadingContract,
    navigateUp = navigateUp,
    tab1 = {
      YourInfoTab(
        viewModel = viewModel,
        onEditCoInsuredClick = onEditCoInsuredClick,
        onChangeAddressClick = onChangeAddressClick,
      )
    },
    tab2 = { CoverageTab(coverageViewModel) },
    tab3 = { DocumentsTab(viewModel) },
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContractDetailScreen(
  uiState: ContractDetailViewModel.ViewState,
  imageLoader: ImageLoader,
  retry: () -> Unit,
  navigateUp: () -> Unit,
  tab1: @Composable () -> Unit,
  tab2: @Composable () -> Unit,
  tab3: @Composable () -> Unit,
) {
  val context = LocalContext.current
  Column {
    TopAppBarWithBack(
      title = "",
      onClick = navigateUp,
    )
    val pagerState = rememberPagerState()
    Box(Modifier.weight(1f)) {
      when (uiState) {
        ContractDetailViewModel.ViewState.Error -> {
          HedvigErrorSection(retry = retry)
        }
        ContractDetailViewModel.ViewState.Loading -> {}
        is ContractDetailViewModel.ViewState.Success -> {
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
            item {
              val contract: ContractCardViewState = uiState.state.contractCardViewState
              InsuranceCard(
                backgroundImageUrl = "",
                chips = buildList {
                  if (contract.firstStatusPillText != null) {
                    add(contract.firstStatusPillText)
                  }
                  if (contract.secondStatusPillText != null) {
                    add(contract.secondStatusPillText)
                  }
                }.toPersistentList(),
                topText = contract.displayName,
                bottomText = contract.detailPills.joinToString(separator = " ∙ "),
                imageLoader = imageLoader,
                modifier = Modifier.padding(horizontal = 16.dp),
                fallbackPainter = contract.gradientType.toDrawable(context)?.let { drawable ->
                  BitmapPainter(drawable.toBitmap(10, 10).asImageBitmap())
                } ?: ColorPainter(Color.Black),
              )
            }
            item { Spacer(Modifier.height(16.dp)) }
            stickyHeader { PagerSelector(pagerState) }
            item {
              HorizontalPager(
                pageCount = 3,
                state = pagerState,
                key = { it },
                verticalAlignment = Alignment.Top,
                modifier = Modifier.animateContentHeight(spring(stiffness = Spring.StiffnessLow)),
              ) { pageIndex ->
                when (pageIndex) {
                  0 -> tab1()
                  1 -> tab2()
                  2 -> tab3()
                  else -> {}
                }
              }
            }
          }
        }
      }
      HedvigFullScreenCenterAlignedProgress(show = uiState is ContractDetailViewModel.ViewState.Loading)
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
        ContractDetailViewModel.ViewState.Success(
          ContractDetailViewState(
            ContractCardViewState(
              id = "asd",
              firstStatusPillText = "Activates 20.03.2024",
              secondStatusPillText = "Activates 20.03.2024",
              gradientType = GradientType.HOME,
              displayName = "Home Insurance",
              detailPills = listOf("Bellmansgatan 19A", "You +1"),
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
            ContractDetailViewState.DocumentsViewState(documents = listOf()),
          ),
        ),
        rememberPreviewImageLoader(),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
