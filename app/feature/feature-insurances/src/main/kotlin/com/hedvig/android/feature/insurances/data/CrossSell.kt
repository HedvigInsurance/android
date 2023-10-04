package com.hedvig.android.feature.insurances.data

data class CrossSell(
  val id: String,
  val title: String,
  val subtitle: String,
  val storeUrl: String,
  val type: CrossSellType,
) {
  enum class CrossSellType {
    PET, HOME, ACCIDENT, CAR, UNKNOWN
  }
}

fun CrossSell.CrossSellType.iconRes(): Int = when (this) {
  CrossSell.CrossSellType.PET -> com.hedvig.android.core.ui.R.drawable.ic_pillow_pet
  CrossSell.CrossSellType.HOME -> com.hedvig.android.core.ui.R.drawable.ic_pillow_home
  CrossSell.CrossSellType.ACCIDENT -> com.hedvig.android.core.ui.R.drawable.ic_pillow_accident
  CrossSell.CrossSellType.CAR -> com.hedvig.android.core.ui.R.drawable.ic_pillow_car
  CrossSell.CrossSellType.UNKNOWN -> com.hedvig.android.core.ui.R.drawable.ic_pillow_home
}

