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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import coil.ImageLoader
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.PreviewContentWithProvidedParametersAnimatedOnClick
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.crosssells.CrossSellItemPlaceholder
import com.hedvig.android.crosssells.CrossSellsSection
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.InsuranceCard
import com.hedvig.android.design.system.hedvig.InsuranceCardPlaceholder
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceScreenEvent
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceUiState
import com.hedvig.android.feature.insurances.insurance.presentation.InsuranceViewModel
import com.hedvig.android.feature.insurances.ui.createChips
import com.hedvig.android.feature.insurances.ui.createPainter
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
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
  Box(Modifier.fillMaxSize()) {
    Column(
      Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .height(64.dp)
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      ) {
        Text(
          text = stringResource(id = R.string.DASHBOARD_SCREEN_TITLE),
          style = HedvigTheme.typography.headlineSmall,
        )
      }
      AnimatedContent(
        targetState = uiState,
        transitionSpec = {
          fadeIn() togetherWith fadeOut()
        },
        label = "uiState",
      ) { state ->
        if (state.hasError) {
          HedvigErrorSection(
            onButtonClick = reload,
            modifier = Modifier.fillMaxSize(),
          )
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
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    PullRefreshIndicator(
      refreshing = isRetrying,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun InsuranceScreenContent(
  uiState: InsuranceUiState,
  imageLoader: ImageLoader,
  showNotificationBadge: Boolean,
  quantityOfCancelledInsurances: Int,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  onNavigateToMovingFlow: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.padding(top = 16.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    val insuranceCardModifier = Modifier
      .padding(horizontal = 16.dp)
      .clip(MaterialTheme.shapes.squircleMedium)
    if (uiState.isLoading) {
      InsuranceCardPlaceholder(
        imageLoader = imageLoader,
        modifier = insuranceCardModifier,
      )
      CrossSellItemPlaceholder()
    } else {
      ContractsSection(
        imageLoader = imageLoader,
        modifier = insuranceCardModifier,
        onInsuranceCardClick = onInsuranceCardClick,
        contracts = uiState.contracts,
      )
      if (uiState.shouldSuggestMovingFlow) {
        MovingFlowSuggestionSection(
          onNavigateToMovingFlow = onNavigateToMovingFlow,
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
      if (uiState.crossSells.isNotEmpty()) {
        CrossSellsSection(
          showNotificationBadge = showNotificationBadge,
          crossSells = uiState.crossSells,
          onCrossSellClick = onCrossSellClick,
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
}

@Composable
private fun ContractsSection(
  contracts: List<InsuranceContract>,
  imageLoader: ImageLoader,
  onInsuranceCardClick: (contractId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (contracts.isEmpty()) {
    HedvigInformationSection(
      title = stringResource(id = R.string.INSURANCES_NO_ACTIVE),
      withDefaultVerticalSpacing = true,
    )
  } else {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      for (contract in contracts) {
        InsuranceCard(
          contract = contract,
          imageLoader = imageLoader,
          modifier = modifier,
          onInsuranceCardClick = onInsuranceCardClick,
        )
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
  InsuranceCard(
    backgroundImageUrl = null,
    chips = contract.createChips(),
    topText = contract.currentInsuranceAgreement.productVariant.displayName,
    bottomText = contract.exposureDisplayName,
    imageLoader = imageLoader,
    modifier = modifier
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
    colors = CardDefaults.outlinedCardColors(),
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth(),
    ) {
      Text(text)
    }
  }
}

@Composable
private fun MovingFlowSuggestionSection(onNavigateToMovingFlow: () -> Unit, modifier: Modifier = Modifier) {
  Column(modifier, Arrangement.spacedBy(8.dp)) {
    HedvigText(
      text = stringResource(R.string.insurances_tab_moving_flow_section_title),
      style = com.hedvig.android.design.system.hedvig.HedvigTheme.typography.headlineSmall,
    )
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

@HedvigPreview
@Composable
private fun PreviewInsuranceScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withContracts: Boolean,
) {
  com.hedvig.android.core.designsystem.theme.HedvigTheme {
    Surface {
      InsuranceScreen(
        InsuranceUiState(
          contracts = if (withContracts) {
            listOf(previewInsurance)
          } else {
            listOf()
          },
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
          shouldSuggestMovingFlow = true,
          hasError = false,
          isLoading = false,
          isRetrying = false,
        ),
        {},
        {},
        {},
        {},
        {},
        rememberPreviewImageLoader(),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewInsuranceDestinationAnimation() {
  val values = InsuranceUiStateProvider().values.toList()
  HedvigTheme {
    Surface {
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
      hasError = false,
      isLoading = true,
      isRetrying = false,
      quantityOfCancelledInsurances = 0,
      showNotificationBadge = false,
      shouldSuggestMovingFlow = true,
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
    ),
  ),
)

private val previewInsurance = InsuranceContract(
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
  contractHolderDisplayName = "Hhhhh Hhhhh",
  contractHolderSSN = "19910913-1893",
  tierName = "Bas",
  supportsTierChange = true,
)
