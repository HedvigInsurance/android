package com.hedvig.android.feature.insurances.insurancedetail

import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.compose.ui.LocalSharedTransitionScope
import com.hedvig.android.compose.ui.animateContentHeight
import com.hedvig.android.compose.ui.rememberSharedContentState
import com.hedvig.android.compose.ui.sharedElement
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.card.InsuranceCardPlaceholder
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailsUiState.Loading
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailsUiState.Success
import com.hedvig.android.feature.insurances.insurancedetail.coverage.CoverageTab
import com.hedvig.android.feature.insurances.insurancedetail.documents.DocumentsTab
import com.hedvig.android.feature.insurances.insurancedetail.yourinfo.YourInfoTab
import com.hedvig.android.feature.insurances.ui.createChips
import com.hedvig.android.feature.insurances.ui.createPainter
import com.hedvig.android.navigation.compose.LocalNavAnimatedVisibilityScope
import hedvig.resources.R
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
internal fun ContractDetailDestination(
  viewModel: ContractDetailViewModel,
  onEditCoInsuredClick: (String) -> Unit,
  onMissingInfoClick: (String) -> Unit,
  onChangeAddressClick: () -> Unit,
  onCancelInsuranceClick: (cancelInsuranceData: CancelInsuranceData) -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  imageLoader: ImageLoader,
) {
  val uiState: ContractDetailsUiState by viewModel.uiState.collectAsStateWithLifecycle()
  ContractDetailScreen(
    uiState = uiState,
    imageLoader = imageLoader,
    retry = { viewModel.emit(ContractDetailsEvent.RetryLoadingContract) },
    onEditCoInsuredClick = onEditCoInsuredClick,
    onMissingInfoClick = onMissingInfoClick,
    onChangeAddressClick = onChangeAddressClick,
    onCancelInsuranceClick = onCancelInsuranceClick,
    onNavigateToNewConversation = onNavigateToNewConversation,
    openUrl = openUrl,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
private fun ContractDetailScreen(
  uiState: ContractDetailsUiState,
  imageLoader: ImageLoader,
  retry: () -> Unit,
  onEditCoInsuredClick: (String) -> Unit,
  onMissingInfoClick: (String) -> Unit,
  onChangeAddressClick: () -> Unit,
  onCancelInsuranceClick: (cancelInsuranceData: CancelInsuranceData) -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  openUrl: (String) -> Unit,
) {
  Column(
    Modifier
      .fillMaxSize()
      .consumeWindowInsets(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
      ),
  ) {
    TopAppBarWithBack(
      title = stringResource(R.string.insurance_details_view_title),
      onClick = navigateUp,
    )
    val pagerState = rememberPagerState(pageCount = { 3 })
    Box(modifier = Modifier.weight(1f)) {
      when (uiState) {
        ContractDetailsUiState.Error -> HedvigErrorSection(onButtonClick = retry, modifier = Modifier.fillMaxSize())

        ContractDetailsUiState.NoContractFound -> {
          HedvigErrorSection(
            subTitle = stringResource(R.string.CONTRACT_DETAILS_ERROR),
            buttonText = stringResource(R.string.general_back_button),
            onButtonClick = navigateBack,
            modifier = Modifier.fillMaxSize(),
          )
        }

        is ContractDetailsUiState.Success,
        is ContractDetailsUiState.Loading,
        -> {
          LazyColumn(
            contentPadding = WindowInsets
              .safeDrawing
              .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
              .asPaddingValues()
              .plus(PaddingValues(top = 16.dp)),
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
              when (uiState) {
                is Loading -> {
                  InsuranceCardPlaceholder(
                    imageLoader = imageLoader,
                    fallbackPainter = uiState.insuranceCardImageId?.let { painterResource(it) },
                    modifier = Modifier
                      .padding(horizontal = 16.dp)
                      .sharedElement(
                        LocalSharedTransitionScope.current,
                        LocalNavAnimatedVisibilityScope.current,
                        rememberSharedContentState(uiState.contractId),
                      ),
                  )
                }

                is Success -> {
                  InsuranceCard(
                    chips = uiState.insuranceContract.createChips(),
                    topText = uiState.insuranceContract.currentInsuranceAgreement.productVariant.displayName,
                    bottomText = uiState.insuranceContract.exposureDisplayName,
                    imageLoader = imageLoader,
                    fallbackPainter = uiState.insuranceContract.createPainter(),
                    isLoading = false,
                    modifier = Modifier
                      .padding(horizontal = 16.dp)
                      .sharedElement(
                        LocalSharedTransitionScope.current,
                        LocalNavAnimatedVisibilityScope.current,
                        rememberSharedContentState(uiState.insuranceContract.id),
                      ),
                  )
                }

                else -> {}
              }
            }
            if (uiState is ContractDetailsUiState.Success) {
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
                        coverageItems = uiState.insuranceContract.currentInsuranceAgreement.displayItems
                          .map { it.title to it.value },
                        coInsured = uiState.insuranceContract.currentInsuranceAgreement.coInsured,
                        allowEditCoInsured = uiState.insuranceContract.supportsEditCoInsured,
                        contractHolderDisplayName = uiState.insuranceContract.contractHolderDisplayName,
                        contractHolderSSN = uiState.insuranceContract.contractHolderSSN,
                        allowChangeAddress = uiState.insuranceContract.supportsAddressChange,
                        allowTerminatingInsurance = uiState.allowTerminatingInsurance,
                        onEditCoInsuredClick = {
                          onEditCoInsuredClick(uiState.insuranceContract.id)
                        },
                        onMissingInfoClick = {
                          onMissingInfoClick(uiState.insuranceContract.id)
                        },
                        onChangeAddressClick = onChangeAddressClick,
                        onNavigateToNewConversation = onNavigateToNewConversation,
                        openUrl = openUrl,
                        onCancelInsuranceClick = {
                          val contractGroup =
                            uiState.insuranceContract.currentInsuranceAgreement.productVariant.contractGroup
                          val contractDisplayName =
                            uiState.insuranceContract.currentInsuranceAgreement.productVariant.displayName
                          onCancelInsuranceClick(
                            CancelInsuranceData(
                              contractId = uiState.insuranceContract.id,
                              contractDisplayName = contractDisplayName,
                              contractExposure = uiState.insuranceContract.exposureDisplayName,
                              contractGroup = contractGroup,
                              activateFrom = uiState.insuranceContract.currentInsuranceAgreement.activeFrom,
                            ),
                          )
                        },
                        upcomingChangesInsuranceAgreement = uiState.insuranceContract.upcomingInsuranceAgreement,
                        isTerminated = uiState.insuranceContract.isTerminated,
                      )
                    }

                    1 -> {
                      CoverageTab(
                        uiState.insuranceContract.currentInsuranceAgreement.productVariant.insurableLimits,
                        uiState.insuranceContract.currentInsuranceAgreement.productVariant.perils,
                      )
                    }

                    2 -> {
                      DocumentsTab(
                        documents = uiState.insuranceContract.getAllDocuments(),
                        onDocumentClicked = openUrl,
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
}

@Composable
private fun InsuranceContract.getAllDocuments(): List<InsuranceVariantDocument> = buildList {
  addAll(currentInsuranceAgreement.productVariant.documents)
  if (currentInsuranceAgreement.certificateUrl != null) {
    val certificate = InsuranceVariantDocument(
      stringResource(id = R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE),
      url = currentInsuranceAgreement.certificateUrl,
      type = InsuranceVariantDocument.InsuranceDocumentType.PRE_SALE_INFO_EU_STANDARD,
    )
    add(certificate)
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
          InsuranceContract(
            "1",
            "Test123",
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
              ),
              certificateUrl = null,
              coInsured = listOf(),
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
        navigateUp = {},
        navigateBack = {},
        onNavigateToNewConversation = {},
        onMissingInfoClick = {},
        openUrl = {},
      )
    }
  }
}
