package com.hedvig.android.feature.product.inapp.navigation

import androidx.navigation3.runtime.EntryProviderScope
import coil3.ImageLoader
import com.hedvig.android.compose.ui.dropUnlessResumed
import com.hedvig.android.feature.product.inapp.ui.CrossSellOptionsSheetContent
import com.hedvig.android.feature.product.inapp.ui.CrossSellProductDestination
import com.hedvig.android.feature.product.inapp.ui.CrossSellProductHeader
import com.hedvig.android.feature.product.inapp.ui.CrossSellProductViewModel
import com.hedvig.android.feature.product.inapp.ui.CrossSellProductViewModelFactory
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.BottomSheetSceneStrategy
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

fun EntryProviderScope<HedvigNavKey>.productInAppEntries(
  backstack: Backstack,
  imageLoader: ImageLoader,
  openCrossSellUrl: (String) -> Boolean,
) {
  entry<CrossSellProductKey> { key ->
    val viewModel: CrossSellProductViewModel =
      assistedMetroViewModel<CrossSellProductViewModel, CrossSellProductViewModelFactory> {
        create(key.crossSellId)
      }
    CrossSellProductDestination(
      viewModel = viewModel,
      header = CrossSellProductHeader(
        title = key.title,
        subtitle = key.subtitle,
        pillowImageSrc = key.pillowImageSrc,
        discountText = key.discountText,
      ),
      imageLoader = imageLoader,
      close = backstack::popBackstack,
      openStoreUrl = { openCrossSellUrl(key.storeUrl) },
    )
  }

  entry<CrossSellOptionsKey>(metadata = BottomSheetSceneStrategy.bottomSheet()) { key ->
    CrossSellOptionsSheetContent(
      options = key.options,
      imageLoader = imageLoader,
      onOptionChosen = dropUnlessResumed { option: CrossSellProductKey -> backstack.navigateToSelectedOption(option) },
      onClose = backstack::popBackstack,
    )
  }
}
