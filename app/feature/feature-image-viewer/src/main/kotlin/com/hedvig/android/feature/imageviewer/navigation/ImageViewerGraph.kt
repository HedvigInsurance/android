package com.hedvig.android.feature.imageviewer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.feature.imageviewer.ImageViewerDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Navigator
import com.hedvig.android.navigation.compose.navdestination

fun EntryProviderScope<HedvigNavKey>.imageViewerGraph(navigator: Navigator, imageLoader: ImageLoader) {
  navdestination<ImageViewer> {
    ImageViewerDestination(
      imageLoader = imageLoader,
      imageUrl = imageUrl,
      cacheKey = cacheKey,
      navigateUp = navigator::navigateUp,
    )
  }
}
