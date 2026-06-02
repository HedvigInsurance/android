package com.hedvig.android.feature.imageviewer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.feature.imageviewer.ImageViewerDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navdestination
import com.hedvig.android.navigation.compose.navigateUp

fun EntryProviderScope<HedvigNavKey>.imageViewerGraph(backStack: MutableList<HedvigNavKey>, imageLoader: ImageLoader) {
  navdestination<ImageViewerKey> {
    ImageViewerDestination(
      imageLoader = imageLoader,
      imageUrl = imageUrl,
      cacheKey = cacheKey,
      navigateUp = backStack::navigateUp,
    )
  }
}
