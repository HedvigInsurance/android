package com.hedvig.app.feature.home.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.SerifBookSmall
import com.hedvig.android.core.designsystem.theme.lavender_200
import com.hedvig.android.core.designsystem.theme.lavender_900
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.core.ui.grid.HedvigGrid
import com.hedvig.android.core.ui.grid.InsideGridSpace
import com.hedvig.android.feature.travelcertificate.GenerateTravelCertificateActivity
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
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
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.home.ui.claimstatus.composables.ClaimStatusCards
import com.hedvig.app.feature.home.ui.connectpayincard.ConnectPayinCard
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.extensions.canOpenUri
import com.hedvig.app.util.extensions.openUri
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType
import giraffe.HomeQuery
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HomeFragment : Fragment() {
  private val viewModel: HomeViewModel by viewModel()
  private val imageLoader: ImageLoader by inject()
  private val marketManager: MarketManager by inject()
  private val hAnalytics: HAnalytics by inject()
  private val featureManager: FeatureManager by inject()

  private val registerForActivityResult: ActivityResultLauncher<Intent> =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
      viewModel.reload()
    }

  @OptIn(ExperimentalMaterialApi::class)
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        HedvigTheme {
          Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
          ) {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
            val isLoading = uiState.isLoading

            val pullRefreshState = rememberPullRefreshState(
              refreshing = isLoading,
              onRefresh = viewModel::reload,
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
                      onRetryButtonClick = viewModel::reload,
                      modifier = Modifier
                        .padding(16.dp)
                        .padding(top = (80 - 16).dp),
                    )
                  }
                  is HomeUiState.Success -> {
                    HomeScreenSuccess(
                      homeItems = uiState.homeItems,
                      imageLoader = imageLoader,
                      onStartMovingFlow = ::onStartMovingFlow,
                      onClaimDetailCardClicked = { claimId ->
                        viewModel.onClaimDetailCardClicked(claimId)
                        startActivity(ClaimDetailActivity.newInstance(requireContext(), claimId))
                      },
                      onClaimDetailCardShown = viewModel::onClaimDetailCardShown,
                      onPaymentCardClicked = ::onPaymentCardClicked,
                      onPaymentCardShown = viewModel::onPaymentCardShown,
                      onEmergencyClaimClicked = { emergencyData ->
                        startActivity(
                          EmergencyActivity.newInstance(
                            context = requireContext(),
                            data = emergencyData,
                          ),
                        )
                      },
                      onGenerateTravelCertificateClicked = {
                        startActivity(Intent(requireContext(), GenerateTravelCertificateActivity::class.java))
                      },
                      onCommonClaimClicked = { commonClaimsData ->
                        startActivity(
                          CommonClaimActivity.newInstance(
                            context = requireContext(),
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
                            buttonText = getString(
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
                          .show(parentFragmentManager, HowClaimsWorkDialog.TAG)
                      },
                      onStartClaimClicked = ::onStartClaimClicked,
                      onPsaClicked = { uri ->
                        if (requireContext().canOpenUri(uri)) {
                          requireContext().openUri(uri)
                        }
                      },
                    )
                  }
                }
                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
              }
              PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                scale = true,
                modifier = Modifier.align(Alignment.TopCenter),
              )
            }
          }
        }
      }
    }
  }

  private fun onStartClaimClicked() {
    lifecycleScope.launch {
      hAnalytics.beginClaim(AppScreen.HOME)
      startClaimsFlow(
        fragmentManager = parentFragmentManager,
        registerForResult = ::registerForResult,
        featureManager = featureManager,
        context = requireContext(),
        commonClaimId = null,
      )
    }
  }

  private fun onPaymentCardClicked(paymentType: PaymentType) {
    viewModel.onPaymentCardClicked()
    val market = marketManager.market ?: return
    startActivity(
      connectPayinIntent(
        requireContext(),
        paymentType,
        market,
        false,
      ),
    )
  }

  private fun onStartMovingFlow() {
    lifecycleScope.launch {
      if (featureManager.isFeatureEnabled(Feature.NEW_MOVING_FLOW)) {
        context?.startActivity(
          Intent(
            requireContext(),
            com.hedvig.android.feature.changeaddress.ChangeAddressActivity::class.java,
          ),
        )
      } else {
        context?.startActivity(ChangeAddressActivity.newInstance(requireContext()))
      }
    }
  }

  private fun registerForResult(intent: Intent) {
    registerForActivityResult.launch(intent)
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
          modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(),
        ) {
          CompositionLocalProvider(LocalContentColor.provides(MaterialTheme.colorScheme.onSurfaceVariant)) {
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
      }
      is HomeModel.PSA -> {
//          todo replace with warning colors once the dark theme one exists
//          color = MaterialTheme.colorScheme.warningContainer,
//          contentColor = MaterialTheme.colorScheme.onWarningContainer,
        Surface(
          onClick = { onPsaClicked(Uri.parse(homeModel.inner.link)) },
          color = if (isSystemInDarkTheme()) {
            Color(0xFFE3B945)
          } else {
            Color(0xFFFAE098)
          },
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            Image(
              painter = painterResource(R.drawable.ic_forward),
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
      is HomeModel.PendingAddressChange -> {
        HedvigCard(
          colors = CardDefaults.outlinedCardColors(
            containerColor = if (isSystemInDarkTheme()) {
              lavender_900
            } else {
              lavender_200
            },
          ),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        ) {
          Column {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
              Icon(
                painter = painterResource(R.drawable.ic_apartment),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
              )
              Column(Modifier.weight(1f)) {
                Text(
                  text = stringResource(hedvig.resources.R.string.home_tab_moving_info_card_title),
                  style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                  text = stringResource(
                    hedvig.resources.R.string.home_tab_moving_info_card_description,
                    homeModel.address,
                  ),
                  style = MaterialTheme.typography.bodyMedium,
                )
              }
            }
            Divider()
            TextButton(
              onClick = onStartMovingFlow,
              modifier = Modifier.align(Alignment.End).padding(horizontal = 16.dp),
            ) {
              Text(text = stringResource(hedvig.resources.R.string.home_tab_moving_info_card_button_text))
            }
          }
        }
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
      is HomeModel.UpcomingRenewal -> TODO()
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
    contentPadding = PaddingValues(horizontal = 16.dp),
    insideGridSpace = InsideGridSpace(8.dp),
  ) {
    for (commonClaim in homeModel.claims) {
      HedvigCard(
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
            .heightIn(100.dp)
            .padding(16.dp),
        ) {
          if (commonClaim !is CommonClaim.GenerateTravelCertificate) {
            val context = LocalContext.current
            val density = LocalDensity.current
            AsyncImage(
              model = ImageRequest.Builder(context)
                .data(
                  Uri.parse(
                    when (commonClaim) {
                      is CommonClaim.Emergency -> commonClaim.inner.iconUrls
                      is CommonClaim.TitleAndBulletPoints -> commonClaim.inner.iconUrls
                      else -> error("Impossible")
                    }.themedIcon,
                  ),
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
