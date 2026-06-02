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
import com.hedvig.core.common.android.sharePDF
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.claimDetailsGraph(
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  onNavigateToImageViewer: (imageUrl: String, cacheKey: String) -> Unit,
  navigateUp: () -> Unit,
  navigateToConversation: (String) -> Unit,
  backStack: MutableList<HedvigNavKey>,
  applicationId: String,
) {
  entry<ClaimDetailsKey> { key ->
    val viewModel: ClaimDetailsViewModel =
      assistedMetroViewModel<ClaimDetailsViewModel, ClaimDetailsViewModel.Factory> { create(key.claimId) }
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
          backStack.add(
            AddFilesKey(
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
  entry<AddFilesKey> { key ->
    val viewModel: AddFilesViewModel =
      assistedMetroViewModel<AddFilesViewModel, AddFilesViewModel.Factory> {
        create(key.targetUploadUrl, key.initialFilesUri)
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
