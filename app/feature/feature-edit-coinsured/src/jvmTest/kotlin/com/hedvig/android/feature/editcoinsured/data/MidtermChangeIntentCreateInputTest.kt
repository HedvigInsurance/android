package com.hedvig.android.feature.editcoinsured.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.apollographql.apollo.api.Optional
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import kotlinx.datetime.LocalDate
import octopus.type.CoInsuredInput
import octopus.type.CoOwnersInput
import org.junit.Test

class MidtermChangeIntentCreateInputTest {
  private val withName = coInsured(firstName = "First", lastName = "Last", ssn = "199001011234")
  private val withBlankNames = coInsured(firstName = "", lastName = " ", birthDate = LocalDate(2016, 7, 28))
  private val withMissingNames = coInsured(firstName = null, lastName = null, birthDate = LocalDate(2016, 7, 28))

  @Test
  fun `blank co-insured names are sent as absent, like missing ones`() {
    val input = listOf(withName, withBlankNames, withMissingNames)
      .toMidtermChangeIntentCreateInput(CoInsuredFlowType.CoInsured)

    assertThat(input.coInsuredInputs.getOrThrow()).isEqualTo(
      listOf(
        CoInsuredInput(
          firstName = Optional.present("First"),
          lastName = Optional.present("Last"),
          ssn = Optional.present("199001011234"),
          birthdate = Optional.absent(),
        ),
        CoInsuredInput(birthdate = Optional.present(LocalDate(2016, 7, 28))),
        CoInsuredInput(birthdate = Optional.present(LocalDate(2016, 7, 28))),
      ),
    )
    assertThat(input.coOwnersInputs).isEqualTo(Optional.absent())
  }

  @Test
  fun `blank co-owner names are sent as absent, like missing ones`() {
    val input = listOf(withBlankNames).toMidtermChangeIntentCreateInput(CoInsuredFlowType.CoOwners)

    assertThat(input.coOwnersInputs.getOrThrow())
      .isEqualTo(listOf(CoOwnersInput(birthdate = Optional.present(LocalDate(2016, 7, 28)))))
    assertThat(input.coInsuredInputs).isEqualTo(Optional.absent())
  }

  private fun coInsured(firstName: String?, lastName: String?, ssn: String? = null, birthDate: LocalDate? = null) =
    CoInsured(
      firstName = firstName,
      lastName = lastName,
      birthDate = birthDate,
      ssn = ssn,
      hasMissingInfo = firstName.isNullOrBlank(),
      activatesOn = null,
      terminatesOn = null,
    )
}
