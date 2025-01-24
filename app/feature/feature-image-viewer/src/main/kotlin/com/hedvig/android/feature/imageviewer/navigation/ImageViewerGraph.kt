package com.hedvig.android.feature.imageviewer.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import coil.ImageLoader
import com.hedvig.android.feature.imageviewer.ImageViewerDestination
import com.hedvig.android.navigation.compose.navdestination

fun NavGraphBuilder.imageViewerGraph(navController: NavController, imageLoader: ImageLoader) {
  navdestination<ImageViewer> { backStackEntry ->
    val imageViewer = backStackEntry.toRoute<ImageViewer>()
    ImageViewerDestination(
      imageLoader = imageLoader,
      imageUrl = imageViewer.imageUrl,
      navigateUp = navController::navigateUp,
    )
  }
}
