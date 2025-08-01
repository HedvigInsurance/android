package com.hedvig.android.feature.claim.details.navigation

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.hedvig.android.feature.claim.details.ui.AddFilesDestination
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsDestination
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.hedvig.android.navigation.compose.navDeepLinks
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.Navigator
import com.hedvig.core.common.android.sharePDF
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimDetailsGraph(
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateUp: () -> Unit,
  navigateToConversation: (NavBackStackEntry, String) -> Unit,
  navigator: Navigator,
  applicationId: String,
  hedvigDeepLinkContainer: HedvigDeepLinkContainer,
) {
  navdestination<ClaimDetailDestination.ClaimOverviewDestination>(
    deepLinks = navDeepLinks(hedvigDeepLinkContainer.claimDetails),
  ) { backStackEntry ->
    val viewModel: ClaimDetailsViewModel = koinViewModel { parametersOf(claimId) }
    val context = LocalContext.current
    ClaimDetailsDestination(
      viewModel = viewModel,
      imageLoader = imageLoader,
      appPackageId = appPackageId,
      navigateUp = navigateUp,
      navigateToConversation = { conversationId -> navigateToConversation(backStackEntry, conversationId) },
      onFilesToUploadSelected = { filesUri: List<Uri>, uploadUri: String ->
        if (filesUri.isNotEmpty()) {
          navigator.navigateUnsafe(
            ClaimDetailInternalDestination.AddFilesDestination(
              targetUploadUrl = uploadUri,
              initialFilesUri = filesUri.map { it.toString() },
            ),
          )
        }
      },
      openUrl = openUrl,
      onNavigateToImageViewer = onNavigateToImageViewer,
      sharePdf = {
        context.sharePDF(it, applicationId)
      },
    )
  }
  navdestination<ClaimDetailInternalDestination.AddFilesDestination> {
    val viewModel: AddFilesViewModel = koinViewModel { parametersOf(targetUploadUrl, initialFilesUri) }
    AddFilesDestination(
      viewModel = viewModel,
      navigateUp = navigateUp,
      onNavigateToImageViewer = onNavigateToImageViewer,
      appPackageId = appPackageId,
      imageLoader = imageLoader,
    )
  }
}
