package com.hedvig.android.feature.insurances.insurance

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.motion.MotionDefaults
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.card.InsuranceCard
import com.hedvig.android.core.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.preview.rememberPreviewImageLoader
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.insurances.data.CrossSell
import com.hedvig.android.feature.insurances.data.InsuranceAgreement
import com.hedvig.android.feature.insurances.data.InsuranceContract
import com.hedvig.android.feature.insurances.data.iconRes
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

@Composable
internal fun InsuranceDestination(
  viewModel: InsuranceViewModel,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
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
    AnimatedContent(
      targetState = uiState.isLoading,
      transitionSpec = {
        MotionDefaults.fadeThroughEnter togetherWith MotionDefaults.fadeThroughExit
      },
      label = "uiState is Loading",
      modifier = Modifier.fillMaxSize(),
    ) { isLoading ->
      when (isLoading) {
        true -> HedvigFullScreenCenterAlignedProgressDebounced(Modifier.fillMaxSize())
        false -> {
          Column(
            Modifier
              .fillMaxSize()
              .pullRefresh(pullRefreshState)
              .verticalScroll(rememberScrollState())
              .windowInsetsPadding(WindowInsets.safeDrawing),
          ) {
            Spacer(Modifier.height(16.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            ) {
              Text(
                text = stringResource(id = R.string.DASHBOARD_SCREEN_TITLE),
                style = MaterialTheme.typography.titleLarge,
              )
            }

            if (uiState.hasError) {
              HedvigErrorSection(onButtonClick = reload)
            } else {
              InsuranceScreenContent(
                imageLoader = imageLoader,
                contracts = uiState.contracts,
                crossSells = uiState.crossSells,
                showNotificationBadge = uiState.showNotificationBadge,
                onInsuranceCardClick = onInsuranceCardClick,
                onCrossSellClick = onCrossSellClick,
                navigateToCancelledInsurances = navigateToCancelledInsurances,
                quantityOfCancelledInsurances = uiState.quantityOfCancelledInsurances,
              )
            }
            Spacer(Modifier.height(16.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
          }
        }
      }
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
private fun ColumnScope.InsuranceScreenContent(
  imageLoader: ImageLoader,
  contracts: ImmutableList<InsuranceContract>,
  crossSells: ImmutableList<CrossSell>,
  showNotificationBadge: Boolean,
  onInsuranceCardClick: (contractId: String) -> Unit,
  onCrossSellClick: (String) -> Unit,
  navigateToCancelledInsurances: () -> Unit,
  quantityOfCancelledInsurances: Int,
) {
  if (contracts.isEmpty()) {
    HedvigInformationSection(
      title = stringResource(id = R.string.INSURANCES_NO_ACTIVE),
      withDefaultVerticalSpacing = true,
    )
  } else {
    for ((index, contract) in contracts.withIndex()) {
      InsuranceCard(
        backgroundImageUrl = null,
        chips = contract.createChips(),
        topText = contract.currentInsuranceAgreement.productVariant.displayName,
        bottomText = contract.exposureDisplayName,
        imageLoader = imageLoader,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .clip(MaterialTheme.shapes.squircleMedium)
          .clickable {
            onInsuranceCardClick(contract.id)
          },
        shape = MaterialTheme.shapes.squircleMedium,
        fallbackPainter = contract.createPainter(),
      )
      if (index != contracts.lastIndex) {
        Spacer(Modifier.height(8.dp))
      }
    }
  }
  if (crossSells.isNotEmpty()) {
    Spacer(Modifier.height(32.dp))
    NotificationSubheading(
      text = stringResource(R.string.insurance_tab_cross_sells_title),
      showNotification = showNotificationBadge,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
    for ((index, crossSell) in crossSells.withIndex()) {
      CrossSellItem(
        crossSell = crossSell,
        onCrossSellClick = onCrossSellClick,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      if (index != crossSells.lastIndex) {
        Spacer(Modifier.height(16.dp))
      }
    }
  }
  if (quantityOfCancelledInsurances > 0) {
    Spacer(Modifier.height(24.dp))
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

@Composable
private fun CrossSellItem(crossSell: CrossSell, onCrossSellClick: (String) -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.heightIn(64.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(crossSell.type.iconRes()),
      contentDescription = null,
      modifier = Modifier.size(48.dp),
    )
    Spacer(Modifier.width(16.dp))
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.Center,
    ) {
      Text(
        text = crossSell.title,
        style = MaterialTheme.typography.bodyMedium,
      )
      Text(
        text = crossSell.subtitle,
        style = MaterialTheme.typography.bodyMedium.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
      )
    }
    Spacer(Modifier.width(16.dp))
    HedvigContainedSmallButton(
      text = stringResource(R.string.cross_sell_get_price),
      onClick = {
        onCrossSellClick(crossSell.storeUrl)
      },
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.typeContainer,
        contentColor = MaterialTheme.colorScheme.onTypeContainer,
      ),
    )
  }
}

@Composable
private fun NotificationSubheading(text: String, showNotification: Boolean, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // We want the notification to stick until we leave the screen, even after we've "cleared" it.
    var stickyShowNotification by remember { mutableStateOf(showNotification) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
      val observer = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_PAUSE) {
          stickyShowNotification = false
        }
      }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
      }
    }
    AnimatedVisibility(stickyShowNotification) {
      Row {
        Canvas(Modifier.size(8.dp)) {
          drawCircle(Color.Red)
        }
        Spacer(Modifier.width(8.dp))
      }
    }
    Text(text = text)
  }
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

@HedvigPreview
@Composable
private fun PreviewInsuranceScreen(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) withContracts: Boolean,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      InsuranceScreen(
        InsuranceUiState(
          contracts = if (withContracts) {
            persistentListOf(
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
                contractHolderSSN = "19910113-1093",
              ),
            )
          } else {
            persistentListOf()
          },
          crossSells = persistentListOf(
            CrossSell(
              id = "1",
              title = "Pet".repeat(5),
              subtitle = "Unlimited FirstVet calls".repeat(2),
              storeUrl = "",
              type = CrossSell.CrossSellType.HOME,
            ),
          ),
          showNotificationBadge = false,
          quantityOfCancelledInsurances = 1,
          hasError = false,
          isLoading = false,
          isRetrying = false,
        ),
        {},
        {},
        {},
        {},
        rememberPreviewImageLoader(),
      )
    }
  }
}
