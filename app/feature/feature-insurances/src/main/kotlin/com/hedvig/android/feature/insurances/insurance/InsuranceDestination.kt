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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import arrow.core.nonEmptyListOf
import coil3.ImageLoader
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.crosssells.CrossSellItemPlaceholder
import com.hedvig.android.crosssells.CrossSellsSection
import com.hedvig.android.data.addons.data.AddonBannerInfo
import com.hedvig.android.data.addons.data.FlowType
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
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
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.data.InsuranceContract.EstablishedInsuranceContract
import com.hedvig.android.feature.insurances.data.MonthlyCost
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceScreenEvent
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceUiState
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.ui.createChips
import com.hedvig.android.feature.insurances.ui.createPainter
import com.hedvig.android.feature.insurances.ui.imageContentScale
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.PullRefreshState
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.A11Y_VIEW_DETAILS
import hedvig.resources.ADDON_FLOW_SEE_PRICE_BUTTON
import hedvig.resources.DASHBOARD_SCREEN_TITLE
import hedvig.resources.INSURANCES_NO_ACTIVE
import hedvig.resources.INSURANCE_ADDONS_SUBHEADING
import hedvig.resources.INSURANCE_OFFERS_SUBHEADING
import hedvig.resources.Res
import hedvig.resources.insurances_tab_moving_flow_info_button_title
import hedvig.resources.insurances_tab_moving_flow_info_title
import hedvig.resources.insurances_tab_terminated_insurance_subtitile
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InsuranceDestination(
  viewModel: InsuranceViewModel,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  onNavigateToMovingFlow: () -> Unit,
  imageLoader: ImageLoader,
  onNavigateToAddonPurchaseFlow: (List<ContractId>) -> Unit,
) {
  val uiState: InsuranceUiState by viewModel.uiState.collectAsStateWithLifecycle()
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
  onNavigateToAddonPurchaseFlow: (List<ContractId>) -> Unit,
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

@Composable
private fun InsuranceScreenContent(
  uiState: InsuranceUiState,
  imageLoader: ImageLoader,
  pullRefreshState: PullRefreshState,
  quantityOfCancelledInsurances: Int,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  onNavigateToMovingFlow: () -> Unit,
  onNavigateToAddonPurchaseFlow: (List<ContractId>) -> Unit,
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
        text = stringResource(Res.string.DASHBOARD_SCREEN_TITLE),
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
        CrossSellItemPlaceholder(
          imageLoader,
          Modifier.padding(horizontal = 16.dp),
        )
      } else {
        Column {
          ContractsSection(
            imageLoader = imageLoader,
            onInsuranceCardClick = onInsuranceCardClick,
            contracts = uiState.contracts + uiState.pendingContracts,
          )
          if (uiState.crossSells.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            CrossSellsSection(
              title = stringResource(Res.string.INSURANCE_OFFERS_SUBHEADING),
              crossSells = uiState.crossSells,
              onCrossSellClick = onCrossSellClick,
              modifier = Modifier.padding(horizontal = 16.dp),
              onSheetDismissed = {},
              imageLoader = imageLoader,
              hasCrossSellDiscounts = uiState.hasCrossSellDiscounts,
            )
          }
          if (uiState.addonBannerInfoList.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Row(
              modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .semantics { heading() },
              verticalAlignment = Alignment.CenterVertically,
            ) {
              HedvigText(text = stringResource(Res.string.INSURANCE_ADDONS_SUBHEADING))
            }
            Spacer(Modifier.height(16.dp))
            uiState.addonBannerInfoList.forEachIndexed { index, bannerInfo ->
              TravelAddonBanner(
                addonBannerInfo = bannerInfo,
                launchAddonPurchaseFlow = {
                  onNavigateToAddonPurchaseFlow(bannerInfo.eligibleInsurancesIds.map(::ContractId))
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp),
              )
              if (index != uiState.addonBannerInfoList.lastIndex) {
                Spacer(Modifier.height(16.dp))
              }
            }
          }
          if (uiState.shouldSuggestMovingFlow) {
            Spacer(Modifier.height(8.dp))
            MovingFlowSuggestionSection(
              onNavigateToMovingFlow = onNavigateToMovingFlow,
              modifier = Modifier.padding(horizontal = 16.dp),
            )
          }
        }
        if (quantityOfCancelledInsurances > 0) {
          TerminatedContractsButton(
            text = pluralStringResource(
              Res.plurals.insurances_tab_terminated_insurance_subtitile,
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
        text = stringResource(Res.string.INSURANCES_NO_ACTIVE),
        description = null,
      )
    } else {
      InsuranceCardsPile(
        contracts = contracts,
        imageLoader = imageLoader,
        onInsuranceCardClick = onInsuranceCardClick,
      )
    }
  }
}

@Composable
private fun InsuranceCardsPile(
  contracts: List<InsuranceContract>,
  imageLoader: ImageLoader,
  onInsuranceCardClick: (contractId: String) -> Unit,
) {
  val peekTopPadding = 10.dp
  val peekBottomPadding = 16.dp
  val textSpacer = 4.dp

  SubcomposeLayout(
    modifier = Modifier.fillMaxWidth(),
  ) { constraints ->

    val peekHeights = contracts.indices.drop(1).map { idx ->
      val contract = contracts[idx]
      subcompose("peek_$idx") {
        Column(Modifier.padding(horizontal = 32.dp)) {
          Spacer(Modifier.height(peekTopPadding))
          HedvigText(contract.topText())
          Spacer(Modifier.height(textSpacer))
          HedvigText(contract.exposureDisplayName)
          Spacer(Modifier.height(peekBottomPadding))
        }
      }.first().measure(constraints).height
    }


    val cardPlaceables = contracts.mapIndexed { idx, contract ->
      subcompose("card_$idx") {
        InsuranceCardWrapper(
          contract = contract,
          imageLoader = imageLoader,
          onInsuranceCardClick = onInsuranceCardClick,
        )
      }.first().measure(constraints)
    }


    val firstCardHeight = cardPlaceables[0].height
    val totalHeight = firstCardHeight + peekHeights.sum()

    layout(constraints.maxWidth, totalHeight) {
      cardPlaceables[0].placeRelative(0, 0, zIndex = contracts.size.toFloat())

      var peekAccumulated = 0
      for (i in 1 until contracts.size) {
        peekAccumulated += peekHeights[i - 1]
        val y = firstCardHeight + peekAccumulated - cardPlaceables[i].height
        cardPlaceables[i].placeRelative(0, y, zIndex = (contracts.size - i).toFloat())
      }
    }
  }
}

private fun InsuranceContract.topText(): String = when (this) {
  is EstablishedInsuranceContract -> currentInsuranceAgreement.productVariant.displayName
  is InsuranceContract.PendingInsuranceContract -> displayName
}

@Composable
private fun InsuranceCardWrapper(
  contract: InsuranceContract,
  imageLoader: ImageLoader,
  modifier: Modifier = Modifier,
  onInsuranceCardClick: (contractId: String) -> Unit,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .hedvigDropShadow()
      .clip(HedvigTheme.shapes.cornerXLarge)
      .clickable{
        onInsuranceCardClick(contract.id)
      },
  ) {
    val description = stringResource(Res.string.A11Y_VIEW_DETAILS)
    InsuranceCard(
      chips = contract.createChips(),
      topText = contract.topText(),
      bottomText = contract.exposureDisplayName,
      imageLoader = imageLoader,
      isLoading = false,
      fallbackPainter = contract.createPainter(),
      imageContentScale = contract.imageContentScale(),
      modifier = Modifier.semantics {
        role = Role.Button
        onClick(label = description) {
          onInsuranceCardClick(contract.id)
          true
        }
      },
    )
  }
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
      message = stringResource(Res.string.insurances_tab_moving_flow_info_title),
      priority = NotificationPriority.Campaign,
      style = InfoCardStyle.Button(
        stringResource(Res.string.insurances_tab_moving_flow_info_button_title),
        dropUnlessResumed { onNavigateToMovingFlow() },
      ),
      modifier = Modifier.hedvigDropShadow(),
    )
  }
}

@Composable
private fun TravelAddonBanner(
  addonBannerInfo: AddonBannerInfo,
  launchAddonPurchaseFlow: (ids: List<String>) -> Unit,
  modifier: Modifier = Modifier,
) {
  FeatureAddonBanner(
    modifier = modifier,
    title = addonBannerInfo.title,
    description = addonBannerInfo.description,
    buttonText = stringResource(Res.string.ADDON_FLOW_SEE_PRICE_BUTTON),
    labels = addonBannerInfo.labels,
    onButtonClick = dropUnlessResumed { launchAddonPurchaseFlow(addonBannerInfo.eligibleInsurancesIds) },
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
          crossSells = List(2) { index ->
            CrossSell(
              id = index.toString(),
              title = "Pet#$index",
              subtitle = "Unlimited FirstVet calls#$index",
              storeUrl = "",
              ImageAsset("", "", ""),
            )
          },
          quantityOfCancelledInsurances = 1,
          shouldSuggestMovingFlow = true,
          hasError = false,
          isLoading = false,
          isRetrying = false,
          addonBannerInfoList = listOf(
            AddonBannerInfo(
              "Title",
              "description",
              eligibleInsurancesIds = nonEmptyListOf(""),
              labels = listOf("Great"),
              flowType = FlowType.APP_CAR_PLUS,
            ),
          ),
          pendingContracts = listOf(previewPendingContract),
          hasCrossSellDiscounts = true,
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
      shouldSuggestMovingFlow = true,
      addonBannerInfoList = emptyList(),
      pendingContracts = listOf(previewPendingContract),
      hasCrossSellDiscounts = true,
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = false,
      isLoading = true,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = true,
      addonBannerInfoList = emptyList(),
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
          ImageAsset("", "", ""),
        ),
      ),
      quantityOfCancelledInsurances = 1,
      hasError = false,
      isLoading = false,
      isRetrying = false,
      shouldSuggestMovingFlow = true,
      addonBannerInfoList = listOf(
        AddonBannerInfo(
          title = "Travel Plus",
          description = "Extended travel insurance with extra coverage for your travels",
          labels = listOf("Popular"),
          eligibleInsurancesIds = nonEmptyListOf("id"),
          flowType = FlowType.APP_TRAVEL_PLUS_SELL_OR_UPGRADE,
        ),
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
      shouldSuggestMovingFlow = true,
      addonBannerInfoList = emptyList(),
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
          ImageAsset("", "", ""),
        ),
        CrossSell(
          id = "2",
          title = "Pet",
          subtitle = "Unlimited FirstVet calls".repeat(2),
          storeUrl = "",
          ImageAsset("", "", ""),
        ),
      ),
      hasError = false,
      isLoading = false,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = true,
      addonBannerInfoList = emptyList(),
      pendingContracts = listOf(previewPendingContract),
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = false,
      isLoading = true,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = true,
      addonBannerInfoList = emptyList(),
      pendingContracts = listOf(previewPendingContract),
    ),
    InsuranceUiState(
      contracts = listOf(),
      crossSells = listOf(),
      hasError = true,
      isLoading = false,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      shouldSuggestMovingFlow = true,
      addonBannerInfoList = emptyList(),
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
  addons = emptyList(),
  basePremium = UiMoney(89.0, UiCurrencyCode.SEK),
  cost = MonthlyCost(
    UiMoney(89.0, UiCurrencyCode.SEK),
    UiMoney(89.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
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
    coOwners = listOf(),
    creationCause = InsuranceAgreement.CreationCause.NEW_CONTRACT,
    addons = null,
    basePremium = UiMoney(89.0, UiCurrencyCode.SEK),
    cost = MonthlyCost(
      UiMoney(89.0, UiCurrencyCode.SEK),
      UiMoney(89.0, UiCurrencyCode.SEK),
      discounts = emptyList(),
    ),
  ),
  upcomingInsuranceAgreement = null,
  renewalDate = LocalDate.fromEpochDays(500),
  supportsAddressChange = false,
  supportsEditCoInsured = true,
  supportsEditCoOwners = false,
  isTerminated = false,
  contractHolderDisplayName = "Hhhhh Hhhhh",
  contractHolderSSN = "19910913-1893",
  tierName = "Bas",
  supportsTierChange = true,
  existingAddons = emptyList(),
  availableAddons = emptyList(),
)
