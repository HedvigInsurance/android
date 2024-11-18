package com.hedvig.android.feature.insurances.insurancedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.compose.ui.animateContentHeight
import com.hedvig.android.data.contract.ContractGroup.RENTAL
import com.hedvig.android.data.contract.ContractType.SE_APARTMENT_RENT
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTabRowMaxSixTabs
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.InsuranceCard
import com.hedvig.android.design.system.hedvig.InsuranceCardPlaceholder
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle.Filled
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.plus
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.insurances.data.CancelInsuranceData
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceAgreement.CreationCause.NEW_CONTRACT
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetailsUiState.Success
import com.hedvig.android.feature.insurances.insurancedetail.coverage.CoverageTab
import com.hedvig.android.feature.insurances.insurancedetail.documents.DocumentsTab
import com.hedvig.android.feature.insurances.insurancedetail.yourinfo.YourInfoTab
import com.hedvig.android.feature.insurances.ui.createChips
import com.hedvig.android.feature.insurances.ui.createPainter
import hedvig.resources.R
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
internal fun ContractDetailDestination(
  viewModel: ContractDetailViewModel,
  onEditCoInsuredClick: (String) -> Unit,
  onMissingInfoClick: (String) -> Unit,
  onChangeTierClick: (String) -> Unit,
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
    onChangeTierClick = onChangeTierClick,
  )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ContractDetailScreen(
  uiState: ContractDetailsUiState,
  imageLoader: ImageLoader,
  retry: () -> Unit,
  onEditCoInsuredClick: (String) -> Unit,
  onMissingInfoClick: (String) -> Unit,
  onChangeTierClick: (String) -> Unit,
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
    AnimatedContent(
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
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
      when (state) {
        ContractDetailsUiState.Error -> HedvigErrorSection(onButtonClick = retry, modifier = Modifier.fillMaxSize())
        ContractDetailsUiState.Loading -> {
          Column(
            Modifier
              .fillMaxSize()
              .padding(
                WindowInsets
                  .safeDrawing
                  .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                  .asPaddingValues()
                  .plus(PaddingValues(top = 16.dp)),
              ),
          ) {
            InsuranceCardPlaceholder(
              imageLoader = imageLoader,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }

        ContractDetailsUiState.NoContractFound -> {
          HedvigErrorSection(
            subTitle = stringResource(R.string.CONTRACT_DETAILS_ERROR),
            buttonText = stringResource(R.string.general_back_button),
            onButtonClick = navigateBack,
            modifier = Modifier.fillMaxSize(),
          )
        }

        is ContractDetailsUiState.Success -> {
          val consumedWindowInsets = remember { MutableWindowInsets() }
          LazyColumn(
            contentPadding = WindowInsets
              .safeDrawing
              .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
              .exclude(consumedWindowInsets)
              .asPaddingValues()
              .plus(PaddingValues(top = 16.dp)),
            modifier = Modifier
              .fillMaxSize()
              .onConsumedWindowInsetsChanged { consumedWindowInsets.insets = it }
              .windowInsetsPadding(
                WindowInsets
                  .safeDrawing
                  .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
              )
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
                isLoading = false,
              )
            }
            item(key = 2, contentType = "space") { Spacer(Modifier.height(16.dp)) }
            stickyHeader(key = 3, contentType = "PagerSelector") {
              Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
                PagerSelector(
                  pagerState = pagerState,
                  modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                )
              }
            }
            item(
              key = 4,
              contentType = "Pager",
            ) {
              HorizontalPager(
                state = pagerState,
                key = { it },
                flingBehavior = PagerDefaults.flingBehavior(
                  state = pagerState,
                  snapAnimationSpec = horizontalPagerSpringSpec(),
                ),
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                  .padding(top = 8.dp)
                  .animateContentHeight(spring(stiffness = Spring.StiffnessLow)),
              ) { pageIndex ->
                when (pageIndex) {
                  0 -> {
                    YourInfoTab(
                      coverageItems = state.insuranceContract.currentInsuranceAgreement.displayItems
                        .map { it.title to it.value },
                      coInsured = state.insuranceContract.currentInsuranceAgreement.coInsured,
                      allowEditCoInsured = state.insuranceContract.supportsEditCoInsured,
                      contractHolderDisplayName = state.insuranceContract.contractHolderDisplayName,
                      contractHolderSSN = state.insuranceContract.contractHolderSSN,
                      allowChangeAddress = state.insuranceContract.supportsAddressChange,
                      allowTerminatingInsurance = state.allowTerminatingInsurance,
                      allowChangeTier = state.insuranceContract.supportsTierChange,
                      onChangeTierClick = {
                        onChangeTierClick(state.insuranceContract.id)
                      },
                      onEditCoInsuredClick = {
                        onEditCoInsuredClick(state.insuranceContract.id)
                      },
                      onMissingInfoClick = {
                        onMissingInfoClick(state.insuranceContract.id)
                      },
                      onChangeAddressClick = onChangeAddressClick,
                      onNavigateToNewConversation = onNavigateToNewConversation,
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

private fun <T> horizontalPagerSpringSpec(visibilityThreshhold: T? = null) = spring<T>(
  stiffness = Spring.StiffnessMediumLow,
  visibilityThreshold = visibilityThreshhold,
)

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

@Composable
private fun PagerSelector(pagerState: PagerState, modifier: Modifier = Modifier) {
  val couroutineScope = rememberCoroutineScope()
  HedvigTabRowMaxSixTabs(
    tabTitles = listOf(
      stringResource(R.string.insurance_details_view_tab_1_title),
      stringResource(R.string.insurance_details_view_tab_2_title),
      stringResource(R.string.insurance_details_view_tab_3_title),
    ),
    selectedTabIndex = pagerState.currentPage,
    selectIndicatorAnimationSpec = horizontalPagerSpringSpec(IntOffset.VisibilityThreshold),
    onTabChosen = { index ->
      couroutineScope.launch {
        pagerState.animateScrollToPage(index)
      }
    },
    modifier = modifier.fillMaxWidth(),
    tabSize = TabSize.Medium,
    tabStyle = Filled,
  )
}

@HedvigPreview
@Composable
private fun PreviewContractDetailScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ContractDetailScreen(
        uiState = Success(
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
                contractGroup = RENTAL,
                contractType = SE_APARTMENT_RENT,
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
              creationCause = NEW_CONTRACT,
            ),
            upcomingInsuranceAgreement = null,
            renewalDate = LocalDate.fromEpochDays(500),
            supportsAddressChange = false,
            supportsEditCoInsured = true,
            isTerminated = false,
            contractHolderDisplayName = "Hugo Linder",
            contractHolderSSN = "199101131093",
            supportsTierChange = true,
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
        onChangeTierClick = {},
      )
    }
  }
}
