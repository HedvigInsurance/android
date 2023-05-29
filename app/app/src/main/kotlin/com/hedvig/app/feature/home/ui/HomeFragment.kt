package com.hedvig.app.feature.home.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.app.navigation.TopLevelDestination
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.theme.SerifBookSmall
import com.hedvig.android.core.designsystem.theme.lavender_200
import com.hedvig.android.core.designsystem.theme.lavender_900
import com.hedvig.android.core.designsystem.theme.onWarning
import com.hedvig.android.core.designsystem.theme.warning
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithActions
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.grid.HedvigGrid
import com.hedvig.android.core.ui.grid.InsideGridSpace
import com.hedvig.android.feature.changeaddress.ChangeAddressActivity
import com.hedvig.android.feature.travelcertificate.GenerateTravelCertificateActivity
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.ClaimDetailActivity
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimActivity
import com.hedvig.app.feature.claims.ui.commonclaim.CommonClaimsData
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyActivity
import com.hedvig.app.feature.claims.ui.commonclaim.EmergencyData
import com.hedvig.app.feature.claims.ui.startClaimsFlow
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.home.model.CommonClaim
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.app.feature.home.ui.claimstatus.composables.ClaimStatusCards
import com.hedvig.app.feature.home.ui.component.ChatTooltip
import com.hedvig.app.feature.home.ui.connectpayincard.ConnectPayinCard
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.getLastEpochDayWhenChatTooltipWasShown
import com.hedvig.app.util.extensions.openUri
import com.hedvig.app.util.extensions.setLastEpochDayWhenChatTooltipWasShown
import com.hedvig.app.util.extensions.startChat
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType
import giraffe.HomeQuery
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

internal fun NavGraphBuilder.homeGraph(
  marketManager: MarketManager,
  imageLoader: ImageLoader,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
) {
  animatedComposable<TopLevelDestination.HOME> {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    HomeDestination(
      uiState = uiState,
      reload = viewModel::reload,
      onClaimDetailCardClicked = viewModel::onClaimDetailCardClicked,
      onClaimDetailCardShown = viewModel::onClaimDetailCardShown,
      onPaymentCardClicked = onPaymentCardClicked@{ paymentType ->
        viewModel.onPaymentCardClicked()
        val market = marketManager.market ?: return@onPaymentCardClicked
        context.startActivity(
          connectPayinIntent(
            context,
            paymentType,
            market,
            false,
          ),
        )
      },
      onPaymentCardShown = viewModel::onPaymentCardShown,
      imageLoader = imageLoader,
      featureManager = featureManager,
      hAnalytics = hAnalytics,
      fragmentManager = fragmentManager,
    )
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun HomeDestination(
  uiState: HomeUiState,
  reload: () -> Unit,
  onClaimDetailCardClicked: (String) -> Unit,
  onClaimDetailCardShown: (String) -> Unit,
  onPaymentCardClicked: (PaymentType) -> Unit,
  onPaymentCardShown: () -> Unit,
  imageLoader: ImageLoader,
  featureManager: FeatureManager,
  hAnalytics: HAnalytics,
  fragmentManager: FragmentManager,
) {
  LocalConfiguration.current
  val context = LocalContext.current
  val resources = context.resources
  val coroutineScope = rememberCoroutineScope()
  val registerForActivityResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
    reload()
  }

  val isLoading = uiState.isLoading
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = isLoading,
    onRefresh = reload,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  Box() {
    Column(
      Modifier
        .matchParentSize()
        .pullRefresh(pullRefreshState)
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
      Spacer(Modifier.height(64.dp))
      when (uiState) {
        HomeUiState.Loading -> {}
        is HomeUiState.Error -> {
          GenericErrorScreen(
            onRetryButtonClick = reload,
            modifier = Modifier
              .padding(16.dp)
              .padding(top = (80 - 16).dp),
          )
        }
        is HomeUiState.Success -> {
          HomeScreenSuccess(
            homeItems = uiState.homeItems,
            imageLoader = imageLoader,
            onStartMovingFlow = {
              coroutineScope.launch {
                if (featureManager.isFeatureEnabled(Feature.NEW_MOVING_FLOW)) {
                  context.startActivity(
                    Intent(
                      context,
                      ChangeAddressActivity::class.java,
                    ),
                  )
                } else {
                  context.startActivity(
                    com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity.newInstance(context),
                  )
                }
              }
            },
            onClaimDetailCardClicked = { claimId ->
              onClaimDetailCardClicked(claimId)
              context.startActivity(ClaimDetailActivity.newInstance(context, claimId))
            },
            onClaimDetailCardShown = onClaimDetailCardShown,
            onPaymentCardClicked = onPaymentCardClicked,
            onPaymentCardShown = onPaymentCardShown,
            onEmergencyClaimClicked = { emergencyData ->
              context.startActivity(
                EmergencyActivity.newInstance(
                  context = context,
                  data = emergencyData,
                ),
              )
            },
            onGenerateTravelCertificateClicked = {
              context.startActivity(GenerateTravelCertificateActivity.newInstance(context))
            },
            onCommonClaimClicked = { commonClaimsData ->
              context.startActivity(
                CommonClaimActivity.newInstance(
                  context = context,
                  data = commonClaimsData,
                ),
              )
            },
            onHowClaimsWorkClick = { howClaimsWorkList ->
              val howClaimsWorkData = howClaimsWorkList.mapIndexed { index, howClaimsWork ->
                DismissiblePagerModel.NoTitlePage(
                  imageUrls = ThemedIconUrls.from(
                    howClaimsWork.illustration.variants.fragments.iconVariantsFragment,
                  ),
                  paragraph = howClaimsWork.body,
                  buttonText = resources.getString(
                    if (index == howClaimsWorkList.lastIndex) {
                      hedvig.resources.R.string.claims_explainer_button_start_claim
                    } else {
                      hedvig.resources.R.string.claims_explainer_button_next
                    },
                  ),
                )
              }
              HowClaimsWorkDialog
                .newInstance(howClaimsWorkData)
                .show(fragmentManager, HowClaimsWorkDialog.TAG)
            },
            onStartClaimClicked = {
              coroutineScope.launch {
                hAnalytics.beginClaim(AppScreen.HOME)
                startClaimsFlow(
                  fragmentManager = fragmentManager,
                  registerForResult = { intent ->
                    registerForActivityResult.launch(intent)
                  },
                  featureManager = featureManager,
                  context = context,
                  commonClaimId = null,
                )
              }
            },
            onPsaClicked = { uri ->
              if (context.canOpenUri(uri)) {
                context.openUri(uri)
              }
            },
            onUpcomingRenewalClick = { uri ->
              if (context.canOpenUri(uri)) {
                context.openUri(uri)
              }
            },
          )
        }
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
    Column {
      TopAppBarWithActions {
        ToolbarChatIcon(
          onClick = { context.startChat() },
        )
      }
      val shouldShowTooltip by produceState(false) {
        val currentEpochDay = LocalDate.now().toEpochDay()
        val lastEpochDayOpened = context.getLastEpochDayWhenChatTooltipWasShown()
        val diff = currentEpochDay - lastEpochDayOpened
        value = diff >= 30
      }
      ChatTooltip(
        showTooltip = shouldShowTooltip,
        tooltipShown = {
          context.setLastEpochDayWhenChatTooltipWasShown(LocalDate.now().toEpochDay())
        },
        modifier = Modifier
          .align(Alignment.End)
          .padding(horizontal = 16.dp),
      )
    }
    PullRefreshIndicator(
      refreshing = isLoading,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.HomeScreenSuccess(
  homeItems: List<HomeModel>,
  imageLoader: ImageLoader,
  onStartMovingFlow: () -> Unit,
  onClaimDetailCardClicked: (claimId: String) -> Unit,
  onClaimDetailCardShown: (claimId: String) -> Unit,
  onPaymentCardClicked: (PaymentType) -> Unit,
  onPaymentCardShown: () -> Unit,
  onEmergencyClaimClicked: (EmergencyData) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onCommonClaimClicked: (CommonClaimsData) -> Unit,
  onHowClaimsWorkClick: (List<HomeQuery.HowClaimsWork>) -> Unit,
  onStartClaimClicked: () -> Unit,
  onPsaClicked: (Uri) -> Unit,
  onUpcomingRenewalClick: (Uri) -> Unit,
) {
  for (homeModel in homeItems) {
    when (homeModel) {
      is HomeModel.BigText -> {
        BigTextRenderer(homeModel)
      }
      is HomeModel.BodyText -> {
        BodyTextRenderer(homeModel)
      }
      HomeModel.ChangeAddress -> {
        Row(
          Modifier
            .fillMaxWidth()
            .clickable(onClick = onStartMovingFlow)
            .padding(16.dp),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_apartment),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
          )
          Spacer(Modifier.width(16.dp))
          Text(stringResource(hedvig.resources.R.string.home_tab_editing_section_change_address_label))
        }
      }
      is HomeModel.ClaimStatus -> {
        ClaimStatusCards(
          goToDetailScreen = onClaimDetailCardClicked,
          onClaimCardShown = onClaimDetailCardShown,
          claimStatusCardsUiState = homeModel.claimStatusCardsUiState,
        )
      }
      is HomeModel.CommonClaims -> {
        CommonClaimsRenderer(
          homeModel = homeModel,
          onEmergencyClaimClicked = onEmergencyClaimClicked,
          onGenerateTravelCertificateClicked = onGenerateTravelCertificateClicked,
          onCommonClaimClicked = onCommonClaimClicked,
          imageLoader = imageLoader,
        )
      }
      is HomeModel.ConnectPayin -> {
        ConnectPayinCard(
          onActionClick = { onPaymentCardClicked(homeModel.payinType) },
          onShown = onPaymentCardShown,
        )
      }
      is HomeModel.Header -> {
        Text(
          text = stringResource(homeModel.stringRes),
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp, bottom = 4.dp),
        )
      }
      is HomeModel.HowClaimsWork -> {
        TextButton(
          onClick = { onHowClaimsWorkClick(homeModel.pages) },
          shape = MaterialTheme.shapes.squircle,
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(),
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_info_claims),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
          Spacer(Modifier.width(4.dp))
          Text(
            text = stringResource(hedvig.resources.R.string.home_tab_claim_explainer_button),
          )
        }
      }
      is HomeModel.PSA -> {
        Surface(
          onClick = { onPsaClicked(Uri.parse(homeModel.inner.link)) },
          color = androidx.compose.material.MaterialTheme.colors.warning,
          contentColor = androidx.compose.material.MaterialTheme.colors.onWarning,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
          ) {
            Text(
              text = homeModel.inner.message ?: "",
              modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(16.dp))
            Icon(
              painter = painterResource(R.drawable.ic_forward),
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
      is HomeModel.PendingAddressChange -> {
        PurpleInfoCard(
          title = stringResource(hedvig.resources.R.string.home_tab_moving_info_card_title),
          body = stringResource(
            hedvig.resources.R.string.home_tab_moving_info_card_description,
            homeModel.address,
          ),
          buttonText = stringResource(hedvig.resources.R.string.home_tab_moving_info_card_button_text),
          buttonAction = onStartMovingFlow,
        )
      }
      is HomeModel.Space -> {
        Spacer(Modifier.height(homeModel.height))
      }
      is HomeModel.StartClaim -> {
        when (homeModel) {
          HomeModel.StartClaim.FirstClaim -> {
            LargeContainedTextButton(
              text = stringResource(homeModel.textId),
              onClick = onStartClaimClicked,
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 18.dp),
            )
          }
          HomeModel.StartClaim.NewClaim -> {
            LargeOutlinedTextButton(
              text = stringResource(homeModel.textId),
              onClick = onStartClaimClicked,
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 18.dp),
            )
          }
        }
      }
      is HomeModel.UpcomingRenewal -> {
        PurpleInfoCard(
          title = stringResource(
            hedvig.resources.R.string.DASHBOARD_RENEWAL_PROMPTER_TITLE,
            homeModel.contractDisplayName,
          ),
          body = stringResource(
            hedvig.resources.R.string.DASHBOARD_RENEWAL_PROMPTER_BODY,
            ChronoUnit.DAYS
              .between(LocalDate.now(), homeModel.upcomingRenewal.renewalDate)
              .toInt(),
          ),
          buttonText = stringResource(hedvig.resources.R.string.home_tab_moving_info_card_button_text),
          buttonAction = {
            onUpcomingRenewalClick(Uri.parse(homeModel.upcomingRenewal.draftCertificateUrl))
          },
        )
      }
    }
  }
}

@Composable
private fun BigTextRenderer(bigText: HomeModel.BigText) {
  val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) }
  val headlineText = when (bigText) {
    is HomeModel.BigText.Pending -> stringResource(
      hedvig.resources.R.string.home_tab_pending_unknown_title,
      bigText.name,
    )
    is HomeModel.BigText.ActiveInFuture -> stringResource(
      hedvig.resources.R.string.home_tab_active_in_future_welcome_title,
      bigText.name,
      formatter.format(bigText.inception),
    )
    is HomeModel.BigText.Active -> stringResource(
      hedvig.resources.R.string.home_tab_welcome_title,
      bigText.name,
    )
    is HomeModel.BigText.Terminated -> stringResource(
      hedvig.resources.R.string.home_tab_terminated_welcome_title,
      bigText.name,
    )
    is HomeModel.BigText.Switching -> stringResource(
      hedvig.resources.R.string.home_tab_pending_switchable_welcome_title,
      bigText.name,
    )
  }
  Text(
    text = headlineText,
    style = MaterialTheme.typography.headlineLarge.copy(
      fontFamily = SerifBookSmall,
    ),
    textAlign = TextAlign.Center,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp)
      .padding(top = 48.dp, bottom = 24.dp),
  )
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.BodyTextRenderer(bigText: HomeModel.BodyText) {
  val bodyTextRes = when (bigText) {
    HomeModel.BodyText.Pending -> hedvig.resources.R.string.home_tab_pending_unknown_body
    HomeModel.BodyText.ActiveInFuture -> hedvig.resources.R.string.home_tab_active_in_future_body
    HomeModel.BodyText.Terminated -> hedvig.resources.R.string.home_tab_terminated_body
    HomeModel.BodyText.Switching -> hedvig.resources.R.string.home_tab_pending_switchable_body
  }
  Text(
    text = stringResource(bodyTextRes),
    style = MaterialTheme.typography.bodyLarge,
    modifier = Modifier
      .padding(horizontal = 24.dp)
      .padding(top = 24.dp),
  )
}

@Composable
private fun CommonClaimsRenderer(
  homeModel: HomeModel.CommonClaims,
  onEmergencyClaimClicked: (EmergencyData) -> Unit,
  onGenerateTravelCertificateClicked: () -> Unit,
  onCommonClaimClicked: (CommonClaimsData) -> Unit,
  imageLoader: ImageLoader,
) {
  HedvigGrid(
    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
    insideGridSpace = InsideGridSpace(8.dp),
  ) {
    for (commonClaim in homeModel.claims) {
      HedvigCard(
        elevation = HedvigCardElevation.Elevated(1.dp),
        onClick = when (commonClaim) {
          is CommonClaim.Emergency -> {
            { onEmergencyClaimClicked(commonClaim.inner) }
          }
          is CommonClaim.GenerateTravelCertificate -> onGenerateTravelCertificateClicked
          is CommonClaim.TitleAndBulletPoints -> {
            { onCommonClaimClicked(commonClaim.inner) }
          }
        },
      ) {
        Column(
          Modifier
            .padding(16.dp)
            .heightIn(100.dp),
        ) {
          if (commonClaim !is CommonClaim.GenerateTravelCertificate) {
            val context = LocalContext.current
            val density = LocalDensity.current
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(
                  when (commonClaim) {
                    is CommonClaim.Emergency -> commonClaim.inner.iconUrls
                    is CommonClaim.TitleAndBulletPoints -> commonClaim.inner.iconUrls
                    else -> error("Impossible")
                  }.themedIcon,
                )
                .size(with(density) { 24.dp.roundToPx() })
                .build(),
              contentDescription = null,
              imageLoader = imageLoader,
              modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(8.dp))
          }
          Spacer(Modifier.weight(1f))
          Text(
            text = when (commonClaim) {
              is CommonClaim.Emergency -> commonClaim.inner.title
              is CommonClaim.GenerateTravelCertificate -> "Generate travel certificate" // todo string resource
              is CommonClaim.TitleAndBulletPoints -> commonClaim.inner.title
            },
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      }
    }
  }
}

@Composable
private fun PurpleInfoCard(
  title: String,
  body: String,
  buttonText: String,
  buttonAction: () -> Unit,
) {
  HedvigCard(
    colors = CardDefaults.outlinedCardColors(
      containerColor = if (isSystemInDarkTheme()) {
        lavender_900
      } else {
        lavender_200
      },
    ),
    border = BorderStroke(1.dp, LocalContentColor.current.copy(alpha = 0.2f)),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp)
      .padding(top = 16.dp),
  ) {
    Column {
      Row(Modifier.padding(16.dp)) {
        Icon(
          painter = painterResource(R.drawable.ic_apartment),
          contentDescription = null,
          modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
          Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
          )
          Spacer(Modifier.height(8.dp))
          Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
      Divider(color = LocalContentColor.current.copy(alpha = 0.2f))
      TextButton(
        onClick = buttonAction,
        shape = MaterialTheme.shapes.squircle,
        modifier = Modifier
          .align(Alignment.End)
          .padding(horizontal = 16.dp, vertical = 4.dp),
      ) {
        Text(
          text = buttonText,
          style = MaterialTheme.typography.bodyLarge,
        )
      }
    }
  }
}
