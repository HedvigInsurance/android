package com.hedvig.android.feature.claim.details.navigation

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.feature.claim.details.ui.AddFilesDestination
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsDestination
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.claimDetailsGraph(
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateUp: () -> Unit,
  navigateToConversation: (String) -> Unit,
  navigator: Navigator,
  applicationId: String,
) {
  navdestination<ClaimDetailDestination.ClaimOverviewDestination> {
    val viewModel: ClaimDetailsViewModel =
      assistedMetroViewModel<ClaimDetailsViewModel, ClaimDetailsViewModel.Factory> { create(claimId) }
    val context = LocalContext.current
    ClaimDetailsDestination(
      viewModel = viewModel,
      imageLoader = imageLoader,
      appPackageId = appPackageId,
      navigateUp = navigateUp,
      navigateToConversation = dropUnlessResumed { conversationId: String ->
        navigateToConversation(conversationId)
      },
      onFilesToUploadSelected = { filesUri: List<Uri>, uploadUri: String ->
        if (filesUri.isNotEmpty()) {
          navigator.navigate(
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
    val viewModel: AddFilesViewModel =
      assistedMetroViewModel<AddFilesViewModel, AddFilesViewModel.Factory> {
        create(targetUploadUrl, initialFilesUri)
      }
    AddFilesDestination(
      viewModel = viewModel,
      navigateUp = navigateUp,
      onNavigateToImageViewer = onNavigateToImageViewer,
      appPackageId = appPackageId,
      imageLoader = imageLoader,
    )
  }
}
