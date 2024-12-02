package com.hedvig.android.feature.addon.purchase.data

import arrow.core.NonEmptyList
import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate

sealed interface Addon {
  data class TravelPlusAddon(
    val addonOptions: NonEmptyList<TravelAddonOption>,
    val exposureName: String,
    val description: String,
    val additionalInfo: String,
    val activationDate: LocalDate,
  ) : Addon
}

sealed interface TravelAddonOption {
  val optionName: String
  val extraAmount: UiMoney

  data class TravelOption45(
    override val optionName: String,
    override val extraAmount: UiMoney,
  ) : TravelAddonOption

  data class TravelOption60(
    override val optionName: String,
    override val extraAmount: UiMoney,
  ) : TravelAddonOption
}
