package com.hedvig.app.util.compose.preview

import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.offer.ui.OfferItems
import org.javamoney.moneta.Money
import java.util.UUID
import javax.money.CurrencyContext
import javax.money.CurrencyUnit

fun OfferItems.InsurelyCard.Retrieved.Companion.previewData(): OfferItems.InsurelyCard.Retrieved {
  val fakeSekCurrency = object : CurrencyUnit {
    override fun compareTo(other: CurrencyUnit?): Int = 0
    override fun getCurrencyCode(): String = "SEK"
    override fun getNumericCode(): Int = 0
    override fun getDefaultFractionDigits(): Int = 0
    override fun getContext(): CurrencyContext? = null
  }
  return OfferItems.InsurelyCard.Retrieved(
    id = UUID.randomUUID().toString(),
    insuranceProviderDisplayName = "Some Insurance",
    currentInsurances = List(2) {
      OfferItems.InsurelyCard.Retrieved.CurrentInsurance(
        "SmthInsrnce",
        Money.of(
          (it + 1) * 12,
          fakeSekCurrency,
        ),
      )
    },
    savedWithHedvig = Money.of(19, fakeSekCurrency),
  )
}

fun DanishAddress.Companion.previewData(): DanishAddress {
  return DanishAddress(
    address = "Asagården 20",
    id = "0a3f50bd-ea90-32b8-ef44-0003ba298018",
    postalCode = null,
    city = null,
    streetName = "Asagården",
    streetNumber = "20",
    floor = null,
    apartment = null,
  )
}

fun DanishAddress.Companion.previewList(): List<DanishAddress> {
  return listOf(
    DanishAddress(
      address = "Asagården 20, 2. tv, 7500 Holstebro",
      id = "0a3f50bd-ea90-32b8-e044-0003ba298018",
      postalCode = "7500",
      city = "Holstebro",
      streetName = "Asagården",
      streetNumber = "20",
      floor = "2",
      apartment = "tv",
    ),
    DanishAddress(
      address = "Asagården 20, 2. th, 7500 Holstebro",
      id = "0a3f50bd-ea8f-32b8-e044-0003ba298018",
      postalCode = "7500",
      city = "Holstebro",
      streetName = "Asagården",
      streetNumber = "20",
      floor = "2",
      apartment = "th",
    ),
    DanishAddress(
      address = "Asagården 21, 2. tv, 7500 Holstebro",
      id = "0a3f50bd-ea96-32b8-e044-0003ba298018",
      postalCode = "7500",
      city = "Holstebro",
      streetName = "Asagården",
      streetNumber = "21",
      floor = "2",
      apartment = "tv",
    ),
    DanishAddress(
      address = "Asagården 21, 2. th, 7500 Holstebro",
      id = "0a3f50bd-ea95-32b8-e044-0003ba298018",
      postalCode = "7500",
      city = "Holstebro",
      streetName = "Asagården",
      streetNumber = "21",
      floor = "2",
      apartment = "th",
    ),
    DanishAddress(
      address = "Asagården 22, 2. tv, 7500 Holstebro",
      id = "0a3f50bd-ea9c-32b8-e044-0003ba298018",
      postalCode = "7500",
      city = "Holstebro",
      streetName = "Asagården",
      streetNumber = "22",
      floor = "2",
      apartment = "tv",
    ),
    DanishAddress(
      address = "Asagården 22, 2. th, 7500 Holstebro",
      id = "0a3f50bd-ea9b-32b8-e044-0003ba298018",
      postalCode = "7500",
      city = "Holstebro",
      streetName = "Asagården",
      streetNumber = "22",
      floor = "2",
      apartment = "th",
    ),
  )
}
