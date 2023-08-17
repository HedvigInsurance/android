package com.hedvig.android.feature.home.claimdetail

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.feature.home.claimdetail.ui.ClaimDetailScreen
import com.hedvig.android.feature.home.claimdetail.ui.ClaimDetailViewModel
import com.hedvig.android.feature.home.home.navigation.HomeDestinations
import com.hedvig.android.navigation.compose.typed.animatedComposable
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimDetailGraph(
  navigateUp: () -> Unit,
  navigateToChat: () -> Unit,
) {
  animatedComposable<HomeDestinations.ClaimDetailDestination> {
    val viewModel: ClaimDetailViewModel = koinViewModel { parametersOf(claimId) }
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()
    ClaimDetailScreen(
      viewState = viewState,
      locale = getLocale(),
      retry = viewModel::retry,
      onUpClick = navigateUp,
      onChatClick = {
        viewModel.onChatClick()
        navigateToChat()
      },
      onPlayClick = viewModel::onPlayClick,
    )
  }
}
