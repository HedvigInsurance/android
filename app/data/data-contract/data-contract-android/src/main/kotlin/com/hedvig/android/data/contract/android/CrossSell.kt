package com.hedvig.android.data.contract.android

import com.hedvig.android.data.contract.CrossSell

fun CrossSell.CrossSellType.iconRes(): Int = when (this) {
  CrossSell.CrossSellType.PET -> R.drawable.ic_pillow_pet
  CrossSell.CrossSellType.HOME -> R.drawable.ic_pillow_home
  CrossSell.CrossSellType.ACCIDENT -> R.drawable.ic_pillow_accident
  CrossSell.CrossSellType.CAR -> R.drawable.ic_pillow_car
  CrossSell.CrossSellType.UNKNOWN -> R.drawable.ic_pillow_home
}
