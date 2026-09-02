package com.hedvig.android.data.contract

/**
 * [pillowImageSmall] and [pillowImageLarge] are two renders of the same artwork at different resolutions.
 * Pick the one matching the box it is drawn into: the same instance is shown both as a 48.dp list row and as
 * a 140.dp hero, and reusing one URL across both sizes makes Coil serve the larger cached bitmap to the
 * smaller box, which visibly softens its edges.
 */
data class CrossSell(
  val id: String,
  val title: String,
  val subtitle: String,
  val storeUrl: String,
  val pillowImageSmall: ImageAsset,
  val pillowImageLarge: ImageAsset,
  val buttonText: String,
)

data class ImageAsset(
  val id: String,
  val src: String,
  val description: String?,
)
