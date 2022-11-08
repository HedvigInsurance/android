package com.hedvig.app.feature.addressautocompletion.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

internal class DanishAddressTest {

  @Test
  fun `output with all fields populated`() {
    val input = DanishAddress(
      address = "Willemoesgade 4, st. tv, 2100 København Ø",
      id = null,
      postalCode = "2100",
      city = "København Ø",
      streetName = "Willemoesgade",
      streetNumber = "4",
      floor = "st",
      apartment = "tv",
    )

    val output = input.toPresentableTextPair()

    val (top, bot) = output
    assertThat(top).isEqualTo("Willemoesgade 4, st. tv")
    assertThat(bot).isEqualTo("2100 København Ø")
  }

  @Test
  fun `output when apartment is null`() {
    val input = DanishAddress(
      address = "Willemoesgade 2, st., 2100 København Ø",
      id = null,
      postalCode = "2100",
      city = "København Ø",
      streetName = "Willemoesgade",
      streetNumber = "2",
      floor = "st",
      apartment = null,
    )

    val output = input.toPresentableTextPair()

    val (top, bot) = output
    assertThat(top).isEqualTo("Willemoesgade 2, st.")
    assertThat(bot).isEqualTo("2100 København Ø")
  }

  @Test
  fun `output when floor is null`() {
    val input = DanishAddress(
      address = "Aabygade 1C, 1, 8300 Odder",
      id = "0a3f50c0-900f-32b8-e044-0003ba298018",
      postalCode = "8300",
      city = "Odder",
      streetName = "Aabygade",
      streetNumber = "1C",
      floor = null,
      apartment = "1",
    )

    val output = input.toPresentableTextPair()

    val (top, bot) = output
    assertThat(top).isEqualTo("Aabygade 1C, 1")
    assertThat(bot).isEqualTo("8300 Odder")
  }

  @Test
  fun `output when floor and apartment are null`() {
    val input = DanishAddress(
      address = "Willemoesgade 1B, 9000 Aalborg",
      id = null,
      postalCode = "9000",
      city = "Aalborg",
      streetName = "Willemoesgade",
      streetNumber = "1B",
      floor = null,
      apartment = null,
    )

    val output = input.toPresentableTextPair()

    val (top, bot) = output
    assertThat(top).isEqualTo("Willemoesgade 1B")
    assertThat(bot).isEqualTo("9000 Aalborg")
  }

  @Test
  fun `mapping from and to a key value map happens correctly`() {
    val input = DanishAddress(
      address = "Willemoesgade 1B, 9000 Aalborg",
      id = null,
      postalCode = "9000",
      city = "Aalborg",
      streetName = "Willemoesgade",
      streetNumber = "1B",
      floor = null,
      apartment = null,
    )

    val map = input.toValueStoreKeys()
    val result = DanishAddress.fromValueStoreKeys(map::get)

    assertThat(result).isEqualTo(input)
  }
}
