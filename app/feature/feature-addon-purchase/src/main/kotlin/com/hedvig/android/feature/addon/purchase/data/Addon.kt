package com.hedvig.android.feature.addon.purchase.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate

sealed interface Addon {
  data class TravelPlusAddon(
    val addonOptions: List<TravelAddon>,
    val exposureName: String,
    val description: String,
    val activationDate: LocalDate,
  ) : Addon
}

sealed interface TravelAddon {
  val optionName: String
  val extraAmount: UiMoney

  data class Travel45(
    override val optionName: String,
    override val extraAmount: UiMoney,
  ) : TravelAddon

  data class Travel60(
    override val optionName: String,
    override val extraAmount: UiMoney,
  ) : TravelAddon
}
