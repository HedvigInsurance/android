package com.hedvig.android.feature.insurances.insurancedetail

import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.animation.FadeAnimatedContent
import com.hedvig.android.core.designsystem.animation.animateContentHeight
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.insurancedetail.coverage.CoverageTab
import com.hedvig.android.feature.insurances.insurancedetail.documents.DocumentsTab
import com.hedvig.android.feature.insurances.insurancedetail.yourinfo.YourInfoTab
import com.hedvig.android.feature.insurances.ui.createChips
import com.hedvig.android.feature.insurances.ui.createPainter
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
internal fun ContractDetailDestination(
  viewModel: ContractDetailViewModel,
  onEditCoInsuredClick: (String) -> Unit,
  onMissingInfoClick: (String) -> Unit,
  onChangeAddressClick: () -> Unit,
  onCancelInsuranceClick: (cancelInsuranceData: CancelInsuranceData) -> Unit,
  openWebsite: (Uri) -> Unit,
  openChat: () -> Unit,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState: ContractDetailsUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ContractDetailScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    retry = viewModel::retryLoadingContract,
    onEditCoInsuredClick = onEditCoInsuredClick,
    onMissingInfoClick = onMissingInfoClick,
    onChangeAddressClick = onChangeAddressClick,
    onCancelInsuranceClick = onCancelInsuranceClick,
    openChat = openChat,
    openUrl = openUrl,
    openWebsite = openWebsite,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContractDetailScreen(
  uiState: ContractDetailsUiState,
  imageLoader: ImageLoader,
  retry: () -> Unit,
  onEditCoInsuredClick: (String) -> Unit,
  onMissingInfoClick: (String) -> Unit,
  onChangeAddressClick: () -> Unit,
  onCancelInsuranceClick: (cancelInsuranceData: CancelInsuranceData) -> Unit,
  openWebsite: (Uri) -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  openChat: () -> Unit,
  openUrl: (String) -> Unit,
) {
  Column(Modifier.fillMaxSize()) {
    TopAppBarWithBack(
      title = stringResource(R.string.insurance_details_view_title),
      onClick = navigateUp,
    )
    val pagerState = rememberPagerState(pageCount = { 3 })
    FadeAnimatedContent(
      targetState = uiState,
      contentKey = { uiState ->
        when (uiState) {
          ContractDetailsUiState.Error -> "Error"
          ContractDetailsUiState.NoContractFound -> "NoContractFound"
          ContractDetailsUiState.Loading -> "Loading"
          is ContractDetailsUiState.Success -> "Success"
        }
      },
      label = "contract detail screen fade animated content",
      modifier = Modifier.weight(1f),
    ) { state ->
      when (state) {
        ContractDetailsUiState.Error -> HedvigErrorSection(onButtonClick = retry, modifier = Modifier.fillMaxSize())
        ContractDetailsUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced(
          show = state is ContractDetailsUiState.Loading,
          modifier = Modifier.fillMaxSize(),
        )

        ContractDetailsUiState.NoContractFound -> {
          HedvigErrorSection(
            subTitle = stringResource(R.string.CONTRACT_DETAILS_ERROR),
            buttonText = stringResource(R.string.general_back_button),
            onButtonClick = navigateBack,
            modifier = Modifier.fillMaxSize(),
          )
        }

        is ContractDetailsUiState.Success -> {
          LazyColumn(
            contentPadding = WindowInsets
              .safeDrawing
              .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
              .asPaddingValues()
              .plus(PaddingValues(top = 8.dp)),
            modifier = Modifier
              .fillMaxSize()
              .consumeWindowInsets(
                WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
              ),
          ) {
            item(
              key = 1,
              contentType = "InsuranceCard",
            ) {
              val contract = state.insuranceContract
              InsuranceCard(
                chips = contract.createChips(),
                topText = contract.currentInsuranceAgreement.productVariant.displayName,
                bottomText = contract.exposureDisplayName,
                imageLoader = imageLoader,
                modifier = Modifier.padding(horizontal = 16.dp),
                fallbackPainter = contract.createPainter(),
              )
            }
            item(key = 2, contentType = "space") { Spacer(Modifier.height(16.dp)) }
            stickyHeader(key = 3, contentType = "PagerSelector") { PagerSelector(pagerState) }
            item(
              key = 4,
              contentType = "Pager",
            ) {
              HorizontalPager(
                state = pagerState,
                key = { it },
                verticalAlignment = Alignment.Top,
                modifier = Modifier.animateContentHeight(spring(stiffness = Spring.StiffnessLow)),
              ) { pageIndex ->
                when (pageIndex) {
                  0 -> {
                    YourInfoTab(
                      coverageItems = state.insuranceContract.currentInsuranceAgreement.displayItems
                        .map { it.title to it.value }
                        .toImmutableList(),
                      coInsured = state.insuranceContract.currentInsuranceAgreement.coInsured,
                      allowEditCoInsured = state.insuranceContract.supportsEditCoInsured,
                      contractHolderDisplayName = state.insuranceContract.contractHolderDisplayName,
                      contractHolderSSN = state.insuranceContract.contractHolderSSN,
                      allowChangeAddress = state.insuranceContract.supportsAddressChange,
                      allowTerminatingInsurance = state.allowTerminatingInsurance,
                      onEditCoInsuredClick = {
                        onEditCoInsuredClick(state.insuranceContract.id)
                      },
                      onMissingInfoClick = {
                        onMissingInfoClick(state.insuranceContract.id)
                      },
                      onChangeAddressClick = onChangeAddressClick,
                      openChat = openChat,
                      openUrl = openUrl,
                      onCancelInsuranceClick = {
                        val contractGroup =
                          state.insuranceContract.currentInsuranceAgreement.productVariant.contractGroup
                        val contractDisplayName =
                          state.insuranceContract.currentInsuranceAgreement.productVariant.displayName
                        onCancelInsuranceClick(
                          CancelInsuranceData(
                            contractId = state.insuranceContract.id,
                            contractDisplayName = contractDisplayName,
                            contractExposure = state.insuranceContract.exposureDisplayName,
                            contractGroup = contractGroup,
                            activateFrom = state.insuranceContract.currentInsuranceAgreement.activeFrom,
                          ),
                        )
                      },
                      upcomingChangesInsuranceAgreement = state.insuranceContract.upcomingInsuranceAgreement,
                      isTerminated = state.insuranceContract.isTerminated,
                    )
                  }

                  1 -> {
                    CoverageTab(
                      state.insuranceContract.currentInsuranceAgreement.productVariant.insurableLimits,
                      state.insuranceContract.currentInsuranceAgreement.productVariant.perils,
                    )
                  }

                  2 -> {
                    DocumentsTab(
                      documents = state.insuranceContract.getAllDocuments(),
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
    }
  }
}

@Composable
private fun InsuranceContract.getAllDocuments(): ImmutableList<InsuranceVariantDocument> = buildList {
  addAll(currentInsuranceAgreement.productVariant.documents)
  if (currentInsuranceAgreement.certificateUrl != null) {
    val certificate = InsuranceVariantDocument(
      stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE),
      url = currentInsuranceAgreement.certificateUrl,
      type = InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD,
    )
    add(certificate)
  }
}.toImmutableList()

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
            isTerminated = false,
            contractHolderDisplayName = "Hugo Linder",
            contractHolderSSN = "199101131093",
          ),
          true,
        ),
        imageLoader = rememberPreviewImageLoader(),
        retry = {},
        onEditCoInsuredClick = {},
        onChangeAddressClick = {},
        onCancelInsuranceClick = {
        },
        openWebsite = {},
        navigateUp = {},
        navigateBack = {},
        openChat = {},
        onMissingInfoClick = {},
        openUrl = {},
      )
    }
  }
}
