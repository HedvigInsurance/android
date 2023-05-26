package com.hedvig.app.feature.insurance.ui.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.google.android.material.transition.platform.MaterialSharedAxis
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.ToolbarChatIcon
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithActions
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.app.databinding.InsuranceContractCardBinding
import com.hedvig.app.databinding.InsuranceTerminatedContractsBinding
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.detail.CrossSellDetailActivity
import com.hedvig.app.feature.insurance.ui.CrossSellCard
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.insurance.ui.NotificationSubheading
import com.hedvig.app.feature.insurance.ui.Subheading
import com.hedvig.app.feature.insurance.ui.bindTo
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.extensions.getActivity
import com.hedvig.app.util.extensions.openWebBrowser
import com.hedvig.app.util.extensions.startChat
import com.hedvig.app.util.extensions.view.setHapticClickListener
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class InsuranceFragment : Fragment() {
  private val viewModel: InsuranceViewModel by activityViewModel()
  private val imageLoader: ImageLoader by inject()

  @OptIn(ExperimentalMaterialApi::class)
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return ComposeView(requireContext()).apply {
      setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
      setContent {
        HedvigTheme {
          val uiState by viewModel.uiState.collectAsStateWithLifecycle()
          val isLoading = uiState.loading
          val storeUrl = uiState.storeUrl
          LaunchedEffect(storeUrl) {
            if (storeUrl != null) {
              viewModel.crossSellActionOpened()
              activity?.openWebBrowser(storeUrl)
            }
          }
          Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxSize(),
          ) {
            val systemBarInsetTopDp = with(LocalDensity.current) {
              WindowInsets.systemBars.getTop(this).toDp()
            }
            val pullRefreshState = rememberPullRefreshState(
              refreshing = isLoading,
              onRefresh = viewModel::load,
              refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
            )
            Box {
              Column(
                Modifier
                  .pullRefresh(pullRefreshState)
                  .verticalScroll(rememberScrollState())
                  .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
              ) {
                Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                Spacer(Modifier.height(64.dp))
                Text(
                  text = stringResource(hedvig.resources.R.string.DASHBOARD_SCREEN_TITLE),
                  style = MaterialTheme.typography.headlineLarge,
                  modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(24.dp))
                val insuranceModels = uiState.insuranceModels
                when {
                  uiState.hasError -> {
                    GenericErrorScreen(
                      description = stringResource(hedvig.resources.R.string.home_tab_error_body),
                      onRetryButtonClick = viewModel::load,
                      modifier = Modifier
                        .padding(16.dp)
                        .padding(top = (40 - 16).dp),
                    )
                  }
                  insuranceModels != null -> {
                    InsuranceModelsRenderer(
                      insuranceModels = insuranceModels,
                      imageLoader = imageLoader,
                      onClickCrossSellCard = {
                        viewModel.onClickCrossSellCard(it)
                        context.startActivity(CrossSellDetailActivity.newInstance(context, it))
                      },
                      onClickCrossSellAction = viewModel::onClickCrossSellAction,
                    )
                  }
                }
                Spacer(Modifier.height(16.dp))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
              }
              TopAppBarWithActions {
                ToolbarChatIcon(
                  onClick = { requireContext().startChat() },
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
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.load()
  }

  override fun onPause() {
    super.onPause()
    viewModel.markCardCrossSellsAsSeen()
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.InsuranceModelsRenderer(
  insuranceModels: List<InsuranceModel>,
  imageLoader: ImageLoader,
  onClickCrossSellCard: (CrossSellData) -> Unit,
  onClickCrossSellAction: (CrossSellData) -> Unit,
) {
  for (insuranceModel in insuranceModels) {
    when (insuranceModel) {
      is InsuranceModel.Contract -> {
        AndroidViewBinding(
          factory = InsuranceContractCardBinding::inflate,
          update = bindInsuranceContract(insuranceModel, imageLoader),
        )
      }
      is InsuranceModel.CrossSellCard -> {
        CrossSellCard(
          data = insuranceModel.inner,
          imageLoader = imageLoader,
          onCardClick = {
            onClickCrossSellCard(insuranceModel.inner)
          },
          onCtaClick = {
            onClickCrossSellAction(insuranceModel.inner)
          },
        )
      }
      is InsuranceModel.CrossSellHeader -> {
        NotificationSubheading(
          text = stringResource(hedvig.resources.R.string.insurance_tab_cross_sells_title),
          showNotification = insuranceModel.showNotificationBadge,
        )
      }
      is InsuranceModel.TerminatedContracts -> {
        AndroidViewBinding(
          factory = InsuranceTerminatedContractsBinding::inflate,
          update = bindInsuranceTerminatedContracts(insuranceModel),
        )
      }
      InsuranceModel.TerminatedContractsHeader -> {
        Subheading(stringResource(hedvig.resources.R.string.insurances_tab_more_title))
      }
      InsuranceModel.Error -> {} // not applicable
    }
  }
}

private fun bindInsuranceContract(
  insuranceModel: InsuranceModel.Contract,
  imageLoader: ImageLoader,
): InsuranceContractCardBinding.() -> Unit = {
  insuranceModel.contractCardViewState.bindTo(this, imageLoader)
  card.setHapticClickListener {
    card.context.getActivity()?.let { activity ->
      if (activity is LoggedInActivity) {
        activity.window.reenterTransition = null
        activity.window.exitTransition = null
      }
      card.context.startActivity(
        ContractDetailActivity.newInstance(
          card.context,
          insuranceModel.contractCardViewState.id,
        ),
      )
    }
  }
}

private fun bindInsuranceTerminatedContracts(
  insuranceModel: InsuranceModel.TerminatedContracts,
): InsuranceTerminatedContractsBinding.() -> Unit = {
  caption.text = caption.resources.getQuantityString(
    hedvig.resources.R.plurals.insurances_tab_terminated_insurance_subtitile,
    insuranceModel.quantity,
    insuranceModel.quantity,
  )
  root.setHapticClickListener {
    root.context.getActivity()?.let { activity ->
      activity.window.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
      activity.window.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
      root.context.startActivity(
        TerminatedContractsActivity.newInstance(root.context),
        ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle(),
      )
    }
  }
}
