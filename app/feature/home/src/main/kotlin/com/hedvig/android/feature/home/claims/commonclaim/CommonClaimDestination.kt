package com.hedvig.android.feature.home.claims.commonclaim

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.hedvig.android.core.common.android.disable
import com.hedvig.android.core.common.android.enable
import com.hedvig.android.core.common.android.setupToolbarScrollListener
import com.hedvig.android.feature.home.claims.commonclaim.bulletpoint.BulletPointsAdapter
import com.hedvig.android.feature.home.databinding.ActivityCommonClaimBinding
import com.hedvig.android.feature.home.home.navigation.HomeDestinations
import com.hedvig.android.navigation.compose.typed.animatedComposable
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

fun NavGraphBuilder.commonClaimGraph(
  navController: NavController,
  imageLoader: ImageLoader,
  hAnalytics: HAnalytics,
  startClaimsFlow: () -> Unit,
) {
  animatedComposable<HomeDestinations.CommonClaimDestination> {
    val viewModel = koinViewModel<CommonClaimViewModel>()
    CommonClaimDestination(
      viewModel = viewModel,
      commonClaimsData = claimsData,
      imageLoader = imageLoader,
      hAnalytics = hAnalytics,
      startClaimsFlow = startClaimsFlow,
      navigateUp = navController::navigateUp,
    )
  }
}

@Composable
private fun CommonClaimDestination(
  viewModel: CommonClaimViewModel,
  commonClaimsData: CommonClaimsData,
  imageLoader: ImageLoader,
  hAnalytics: HAnalytics,
  startClaimsFlow: () -> Unit,
  navigateUp: () -> Unit,
) {
  val context = LocalContext.current
  val updatedNavigateUp by rememberUpdatedState(navigateUp)
  val coroutineScope = rememberCoroutineScope()
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
      AndroidViewBinding(
        ActivityCommonClaimBinding::inflate,
      ) {
        toolbar.setNavigationOnClickListener {
          updatedNavigateUp()
        }
        root.setupToolbarScrollListener(toolbar = toolbar)
        toolbar.title = commonClaimsData.title

        bulletPointsRecyclerView.adapter = BulletPointsAdapter(imageLoader).apply {
          submitList(commonClaimsData.bulletPoints)
        }

        firstMessage.commonClaimFirstMessage.text = commonClaimsData.layoutTitle
        firstMessage.commonClaimCreateClaimButton.text = commonClaimsData.buttonText
        if (commonClaimsData.isFirstVet()) {
          firstMessage.commonClaimCreateClaimButton.enable()
          firstMessage.commonClaimCreateClaimButton.setOnClickListener {
            context.startActivity(getFirstVetIntent(context))
          }
        } else if (commonClaimsData.eligibleToClaim) {
          firstMessage.commonClaimCreateClaimButton.enable()
          firstMessage.commonClaimCreateClaimButton.setOnClickListener {
            coroutineScope.launch {
              hAnalytics.beginClaim(AppScreen.COMMON_CLAIM_DETAIL)
              startClaimsFlow()
            }
          }
        } else {
          firstMessage.commonClaimCreateClaimButton.disable()
        }
      }
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.statusBars))
    }
  }
}
