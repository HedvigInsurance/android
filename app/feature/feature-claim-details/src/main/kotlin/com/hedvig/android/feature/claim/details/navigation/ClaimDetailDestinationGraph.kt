package com.hedvig.android.feature.claim.details.navigation

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.hedvig.android.core.common.android.sharePDF
import com.hedvig.android.feature.claim.details.ui.AddFilesDestination
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsDestination
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.hedvig.android.navigation.core.Navigator
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimDetailsGraph(
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  navigateToConversation: (NavBackStackEntry, String) -> Unit,
  navigator: Navigator,
  applicationId: String,
) {
  navigation<AppDestination.ClaimDetails>(
    startDestination = createRoutePattern<ClaimDetailDestinations.ClaimOverviewDestination>(),
  ) {
    composable<ClaimDetailDestinations.ClaimOverviewDestination> { backStackEntry ->
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
              ClaimDetailDestinations.AddFilesDestination(
                targetUploadUrl = uploadUri,
                initialFilesUri = filesUri.map { it.toString() },
              ),
            )
          }
        },
        openUrl = openUrl,
        sharePdf = {
          context.sharePDF(it, applicationId)
        },
      )
    }
    composable<ClaimDetailDestinations.AddFilesDestination> {
      val viewModel: AddFilesViewModel = koinViewModel { parametersOf(targetUploadUrl, initialFilesUri) }
      AddFilesDestination(
        viewModel = viewModel,
        navigateUp = navigateUp,
        appPackageId = appPackageId,
        imageLoader = imageLoader,
      )
    }
  }
}
