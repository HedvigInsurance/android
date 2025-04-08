package com.hedvig.android.data.contract

data class CrossSell(
  val id: String,
  val title: String,
  val subtitle: String,
  val storeUrl: String,
  val type: CrossSellType,
) {
  enum class CrossSellType {
    PET,
    HOME,
    ACCIDENT,
    CAR,
    UNKNOWN,
  }
}
