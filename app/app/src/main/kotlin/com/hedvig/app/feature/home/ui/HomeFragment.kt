package com.hedvig.app.feature.home.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.SerifBookSmall
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.claims.ui.startClaimsFlow
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.payment.connectPayinIntent
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType
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
                    HomeScreenSuccess(uiState.homeItems)
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
private fun ColumnScope.HomeScreenSuccess(homeItems: List<HomeModel>) {
  for (homeModel in homeItems) {
    when (homeModel) {
      is HomeModel.BigText -> {
        BigTextRenderer(homeModel)
      }
      is HomeModel.BodyText -> {
        BodyTextRenderer(homeModel)
      }
      HomeModel.ChangeAddress -> TODO()
      is HomeModel.ClaimStatus -> TODO()
      is HomeModel.CommonClaim.Emergency -> TODO()
      is HomeModel.CommonClaim.GenerateTravelCertificate -> TODO()
      is HomeModel.CommonClaim.TitleAndBulletPoints -> TODO()
      is HomeModel.ConnectPayin -> TODO()
      is HomeModel.Header -> TODO()
      is HomeModel.HowClaimsWork -> TODO()
      is HomeModel.PSA -> TODO()
      is HomeModel.PendingAddressChange -> TODO()
      is HomeModel.Space -> TODO()
      HomeModel.StartClaimContained.FirstClaim -> TODO()
      HomeModel.StartClaimContained.NewClaim -> TODO()
      HomeModel.StartClaimOutlined.FirstClaim -> TODO()
      HomeModel.StartClaimOutlined.NewClaim -> TODO()
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

@Composable
private fun BodyTextRenderer(bigText: HomeModel.BodyText) {
  val bodyTextRes = when (bigText) {
    HomeModel.BodyText.Pending -> hedvig.resources.R.string.home_tab_pending_unknown_body
    HomeModel.BodyText.ActiveInFuture -> hedvig.resources.R.string.home_tab_active_in_future_body
    HomeModel.BodyText.Terminated -> hedvig.resources.R.string.home_tab_terminated_body
    HomeModel.BodyText.Switching -> hedvig.resources.R.string.home_tab_pending_switchable_body
  }
  Text(
    text = stringResource(bodyTextRes),
    style = MaterialTheme.typography.bodyLarge,
    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp),
  )
}
