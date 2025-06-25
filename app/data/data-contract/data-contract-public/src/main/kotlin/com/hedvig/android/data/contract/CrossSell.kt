package com.hedvig.android.data.contract

data class CrossSell(
  val id: String,
  val title: String,
  val subtitle: String,
  val storeUrl: String,
  val pillowImage: ImageAsset,
)

data class ImageAsset(
  val id: String,
  val src: String,
  val description: String?,
)
