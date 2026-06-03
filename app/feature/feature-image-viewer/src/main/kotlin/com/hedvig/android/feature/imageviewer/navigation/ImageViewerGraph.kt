package com.hedvig.android.feature.imageviewer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.feature.imageviewer.ImageViewerDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateUp

fun EntryProviderScope<HedvigNavKey>.imageViewerGraph(backstack: Backstack, imageLoader: ImageLoader) {
  entry<ImageViewerKey> { key ->
    ImageViewerDestination(
      imageLoader = imageLoader,
      imageUrl = key.imageUrl,
      cacheKey = key.cacheKey,
      navigateUp = backstack::navigateUp,
    )
  }
}
