package com.hedvig.android.data.contract.android

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

fun CrossSell.CrossSellType.iconRes(): Int = when (this) {
  CrossSell.CrossSellType.PET -> R.drawable.ic_pillow_pet
  CrossSell.CrossSellType.HOME -> R.drawable.ic_pillow_home
  CrossSell.CrossSellType.ACCIDENT -> R.drawable.ic_pillow_accident
  CrossSell.CrossSellType.CAR -> R.drawable.ic_pillow_car
  CrossSell.CrossSellType.UNKNOWN -> R.drawable.ic_pillow_home
}
