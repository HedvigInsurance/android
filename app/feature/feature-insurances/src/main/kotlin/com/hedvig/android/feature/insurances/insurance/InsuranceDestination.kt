package com.hedvig.android.feature.insurances.insurance

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import arrow.core.nonEmptyListOf
import coil.ImageLoader
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
import com.hedvig.android.crosssells.CrossSellItemPlaceholder
import com.hedvig.android.crosssells.CrossSellsSection
import com.hedvig.android.data.addons.data.TravelAddonBannerInfo
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.FeatureAddonBanner
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.InsuranceCard
import com.hedvig.android.design.system.hedvig.InsuranceCardPlaceholder
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.data.InsuranceContract.EstablishedInsuranceContract
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceScreenEvent
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceUiState
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.ui.createChips
import com.hedvig.android.feature.insurances.ui.createPainter
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.PullRefreshState
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.R
import kotlinx.datetime.LocalDate

@Composable
internal fun InsuranceDestination(
  viewModel: InsuranceViewModel,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  onNavigateToMovingFlow: () -> Unit,
  imageLoader: ImageLoader,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  val uiState: InsuranceUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val lifecycleOwner = LocalLifecycleOwner.current
  val currentViewModel by rememberUpdatedState(viewModel)
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_PAUSE) {
        currentViewModel.emit(InsuranceScreenEvent.MarkCardCrossSellsAsSeen)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }
  DisposableEffect(Unit) {
    onDispose {
      currentViewModel.emit(InsuranceScreenEvent.MarkCardCrossSellsAsSeen)
    }
  }
  InsuranceScreen(
    uiState = uiState,
    reload = { viewModel.emit(InsuranceScreenEvent.RetryLoading) },
    onInsuranceCardClick = onInsuranceCardClick,
    onCrossSellClick = onCrossSellClick,
    navigateToCancelledInsurances = navigateToCancelledInsurances,
    onNavigateToMovingFlow = onNavigateToMovingFlow,
    imageLoader = imageLoader,
    onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
  )
}

@Composable
private fun InsuranceScreen(
  uiState: InsuranceUiState,
  reload: () -> Unit,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  onNavigateToMovingFlow: () -> Unit,
  imageLoader: ImageLoader,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
) {
  val isRetrying = uiState.isRetrying
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = isRetrying,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  Surface(Modifier.fillMaxSize(), color = HedvigTheme.colorScheme.backgroundPrimary) {
    Box(propagateMinConstraints = true) {
      AnimatedContent(
        targetState = uiState,
        transitionSpec = {
          fadeIn() togetherWith fadeOut()
        },
        contentKey = { it.hasError },
        label = "uiState",
      ) { state ->
        if (state.hasError) {
          Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
            Spacer(Modifier.weight(1f))
            HedvigErrorSection(
              onButtonClick = reload,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
          }
        } else {
          InsuranceScreenContent(
            uiState = state,
            imageLoader = imageLoader,
            showNotificationBadge = state.showNotificationBadge,
            quantityOfCancelledInsurances = state.quantityOfCancelledInsurances,
            onInsuranceCardClick = onInsuranceCardClick,
            onCrossSellClick = onCrossSellClick,
            navigateToCancelledInsurances = navigateToCancelledInsurances,
            onNavigateToMovingFlow = onNavigateToMovingFlow,
            modifier = Modifier.fillMaxSize(),
            pullRefreshState = pullRefreshState,
            onNavigateToAddonPurchaseFlow = onNavigateToAddonPurchaseFlow,
          )
        }
      }
      PullRefreshIndicator(
        refreshing = isRetrying,
        state = pullRefreshState,
        scale = true,
        modifier = Modifier.wrapContentSize(Alignment.TopCenter),
      )
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun InsuranceScreenContent(
  uiState: InsuranceUiState,
  imageLoader: ImageLoader,
  pullRefreshState: PullRefreshState,
  showNotificationBadge: Boolean,
  quantityOfCancelledInsurances: Int,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  onNavigateToMovingFlow: () -> Unit,
  onNavigateToAddonPurchaseFlow: (List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .pullRefresh(pullRefreshState)
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
      .verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .height(64.dp)
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .semantics(mergeDescendants = true) {
          heading()
        },
    ) {
      HedvigText(
        text = stringResource(id = R.string.DASHBOARD_SCREEN_TITLE),
        style = HedvigTheme.typography.headlineSmall,
      )
    }
    Spacer(Modifier.height(16.dp))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (uiState.isLoading) {
        InsuranceCardPlaceholder(
          imageLoader = imageLoader,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
        CrossSellItemPlaceholder(Modifier.padding(horizontal = 16.dp))
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ContractsSection(
            imageLoader = imageLoader,
            onInsuranceCardClick = onInsuranceCardClick,
            contracts = uiState.contracts + uiState.pendingContracts,
          )
          if (uiState.travelAddonBannerInfo != null) {
            TravelAddonBanner(
              travelAddonBannerInfo = uiState.travelAddonBannerInfo,
              launchAddonPurchaseFlow = {
                onNavigateToAddonPurchaseFlow(uiState.travelAddonBannerInfo.eligibleInsurancesIds)
              },
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            )
          }
          if (uiState.shouldSuggestMovingFlow) {
            MovingFlowSuggestionSection(
              onNavigateToMovingFlow = onNavigateToMovingFlow,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }
        if (uiState.crossSells.isNotEmpty()) {
          CrossSellsSection(
            showNotificationBadge = showNotificationBadge,
            crossSells = uiState.crossSells,
            onCrossSellClick = onCrossSellClick,
            modifier = Modifier.padding(horizontal = 16.dp),
            onSheetDismissed = {},
          )
        }
        if (quantityOfCancelledInsurances > 0) {
          TerminatedContractsButton(
            text = pluralStringResource(
              R.plurals.insurances_tab_terminated_insurance_subtitile,
              quantityOfCancelledInsurances,
              quantityOfCancelledInsurances,
            ),
            onClick = navigateToCancelledInsurances,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
      }
    }
    Spacer(Modifier.height(8.dp))
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
  }
}

@Composable
private fun ContractsSection(
  contracts: List<InsuranceContract>,
  imageLoader: ImageLoader,
  onInsuranceCardClick: (contractId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    if (contracts.isEmpty()) {
      EmptyState(
        text = stringResource(id = R.string.INSURANCES_NO_ACTIVE),
        description = null,
      )
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (contract in contracts) {
          InsuranceCard(
            contract = contract,
            imageLoader = imageLoader,
            onInsuranceCardClick = onInsuranceCardClick,
          )
        }
      }
    }
  }
}

@Composable
private fun InsuranceCard(
  contract: InsuranceContract,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  onInsuranceCardClick: (contractId: String) -> Unit,
) {
  val topText = when (contract) {
    is EstablishedInsuranceContract ->
      contract.currentInsuranceAgreement.productVariant.displayName

    is InsuranceContract.PendingInsuranceContract ->
      contract.displayName
  }
  InsuranceCard(
    backgroundImageUrl = null,
    chips = contract.createChips(),
    topText = topText,
    bottomText = contract.exposureDisplayName,
    imageLoader = imageLoader,
    modifier = modifier
      .padding(horizontal = 16.dp)
      .clip(HedvigTheme.shapes.cornerXLarge)
      .clickable {
        onInsuranceCardClick(contract.id)
      },
    fallbackPainter = contract.createPainter(),
    isLoading = false,
  )
}

@Composable
private fun TerminatedContractsButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
  ) {
    HedvigText(text, Modifier.padding(16.dp))
  }
}

@Composable
private fun MovingFlowSuggestionSection(onNavigateToMovingFlow: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier) {
    HedvigNotificationCard(
      message = stringResource(R.string.insurances_tab_moving_flow_info_title),
      priority = NotificationPriority.Campaign,
      style = InfoCardStyle.Button(
        stringResource(R.string.insurances_tab_moving_flow_info_button_title),
        dropUnlessResumed { onNavigateToMovingFlow() },
      ),
    )
  }
}

@Composable
private fun TravelAddonBanner(
  travelAddonBannerInfo: TravelAddonBannerInfo,
  launchAddonPurchaseFlow: (ids: List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  FeatureAddonBanner(
    modifier = modifier,
    title = travelAddonBannerInfo.title,
    description = travelAddonBannerInfo.description,
    buttonText = stringResource(R.string.ADDON_FLOW_SEE_PRICE_BUTTON),
    labels = travelAddonBannerInfo.labels,
    onButtonClick = dropUnlessResumed { launchAddonPurchaseFlow(travelAddonBannerInfo.eligibleInsurancesIds) },
  )
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewInsuranceScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withContracts: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      InsuranceScreen(
        InsuranceUiState(
          contracts = if (withContracts) {
            listOf(previewInsurance)
          } else {
            listOf()
          },
          crossSells = List(10) { index ->
            CrossSell(
              id = index.toString(),
              title = "Pet#$index",
              subtitle = "Unlimited FirstVet calls#$index",
              storeUrl = "",
              type = CrossSell.CrossSellType.HOME,
            )
          },
          showNotificationBadge = false,
          quantityOfCancelledInsurances = 1,
          shouldSuggestMovingFlow = true,
          hasError = false,
          isLoading = false,
          isRetrying = false,
          travelAddonBannerInfo = null,
          pendingContracts = listOf(previewPendingContract),
        ),
        {},
        {},
        {},
        {},
        {},
        rememberPreviewImageLoader(),
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceDestinationAnimation() {
  val values = InsuranceUiStateProvider().values.toList()
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PreviewContentWithProvidedParametersAnimatedOnClick(
        parametersList = values,
        content = { insuranceUiState ->
          InsuranceScreen(
            uiState = insuranceUiState,
            imageLoader = rememberPreviewImageLoader(),
            reload = {},
            onInsuranceCardClick = {},
            onCrossSellClick = {},
            navigateToCancelledInsurances = {},
            onNavigateToMovingFlow = {},
            onNavigateToAddonPurchaseFlow = {},
          )
        },
      )
    }
  }
}

private class InsuranceUiStateProvider : CollectionPreviewParameterProvider<InsuranceUiState>(
  listOf(
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = true,
      isLoading = false,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      showNotificationBadge = false,
      shouldSuggestMovingFlow = true,
      travelAddonBannerInfo = null,
      pendingContracts = listOf(previewPendingContract),
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = false,
      isLoading = true,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      showNotificationBadge = false,
      shouldSuggestMovingFlow = true,
      travelAddonBannerInfo = null,
      pendingContracts = listOf(previewPendingContract),
    ),
    InsuranceUiState(
      contracts =
        listOf(previewInsurance),
      crossSells = listOf(
        CrossSell(
          id = "1",
          title = "Pet",
          subtitle = "Unlimited FirstVet calls",
          storeUrl = "",
          type = CrossSell.CrossSellType.HOME,
        ),
      ),
      showNotificationBadge = false,
      quantityOfCancelledInsurances = 1,
      hasError = false,
      isLoading = false,
      isRetrying = false,
      shouldSuggestMovingFlow = true,
      travelAddonBannerInfo = TravelAddonBannerInfo(
        title = "Travel Plus",
        description = "Extended travel insurance with extra coverage for your travels",
        labels = listOf("Popular"),
        eligibleInsurancesIds = nonEmptyListOf("id"),
      ),
      pendingContracts = listOf(),
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = false,
      isLoading = true,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      showNotificationBadge = false,
      shouldSuggestMovingFlow = true,
      travelAddonBannerInfo = null,
      pendingContracts = listOf(previewPendingContract),
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(
        CrossSell(
          id = "1",
          title = "Home",
          subtitle = "Unlimited home",
          storeUrl = "",
          type = CrossSell.CrossSellType.HOME,
        ),
        CrossSell(
          id = "2",
          title = "Pet",
          subtitle = "Unlimited FirstVet calls".repeat(2),
          storeUrl = "",
          type = CrossSell.CrossSellType.PET,
        ),
      ),
      hasError = false,
      isLoading = false,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      showNotificationBadge = false,
      shouldSuggestMovingFlow = true,
      travelAddonBannerInfo = null,
      pendingContracts = listOf(previewPendingContract),
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = false,
      isLoading = true,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      showNotificationBadge = false,
      shouldSuggestMovingFlow = true,
      travelAddonBannerInfo = null,
      pendingContracts = listOf(previewPendingContract),
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = true,
      isLoading = false,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      showNotificationBadge = false,
      shouldSuggestMovingFlow = true,
      travelAddonBannerInfo = null,
      pendingContracts = listOf(previewPendingContract),
    ),
  ),
)

private val previewPendingContract = InsuranceContract.PendingInsuranceContract(
  "1",
  "Test",
  displayName = "Test pending display name",
  exposureDisplayName = "Pending street",
  contractHolderSSN = null,
  contractHolderDisplayName = "Pending holder name",
  productVariant = ProductVariant(
    displayName = "",
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
  displayItems = listOf(),
)

private val previewInsurance = EstablishedInsuranceContract(
  "1",
  "Test123",
  exposureDisplayName = "",
  inceptionDate = LocalDate.fromEpochDays(200),
  terminationDate = LocalDate.fromEpochDays(400),
  currentInsuranceAgreement = InsuranceAgreement(
    activeFrom = LocalDate.fromEpochDays(240),
    activeTo = LocalDate.fromEpochDays(340),
    displayItems = listOf(),
    productVariant = ProductVariant(
      displayName = "",
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
  isTerminated = false,
  contractHolderDisplayName = "Hhhhh Hhhhh",
  contractHolderSSN = "19910913-1893",
  tierName = "Bas",
  supportsTierChange = true,
)
