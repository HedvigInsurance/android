package com.hedvig.android.feature.claim.details

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import coil.ImageLoader
import com.hedvig.android.feature.claim.details.navigation.ClaimDetailDestinations
import com.hedvig.android.feature.claim.details.ui.AddFilesDestination
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsDestination
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.hedvig.android.navigation.core.AppDestination
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.kiwi.navigationcompose.typed.navigate
import com.kiwi.navigationcompose.typed.navigation
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimDetailsGraph(
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  openChat: (NavBackStackEntry) -> Unit,
  navController: NavHostController,
) {
  navigation<AppDestination.ClaimDetails>(
    startDestination = createRoutePattern<ClaimDetailDestinations.ClaimOverviewDestination>(),
  ) {
    composable<ClaimDetailDestinations.ClaimOverviewDestination> { backStackEntry ->
      val viewModel: ClaimDetailsViewModel = koinViewModel { parametersOf(claimId) }
      ClaimDetailsDestination(
        viewModel = viewModel,
        imageLoader = imageLoader,
        appPackageId = appPackageId,
        navigateUp = navigateUp,
        onChatClick = { openChat(backStackEntry) },
        onUri = { fileUri: Uri, uploadUri: String ->
          navController.navigate(
            ClaimDetailDestinations.AddFilesDestination(
              targetUploadUrl = uploadUri,
              initialFileUri = fileUri.toString(),
            ),
          )
        },
        openUrl = openUrl,
        downloadFromUrl = { //todo
           }
      )
    }
    composable<ClaimDetailDestinations.AddFilesDestination> {
      val viewModel: AddFilesViewModel = koinViewModel { parametersOf(targetUploadUrl, initialFileUri) }
      AddFilesDestination(
        viewModel = viewModel,
        navigateUp = navigateUp,
        appPackageId = appPackageId,
        imageLoader = imageLoader,
      )
    }
  }
}
