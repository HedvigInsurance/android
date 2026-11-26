package com.hedvig.android.feature.movingflow.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.single
import com.apollographql.apollo.api.Optional
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.movingflow.MovingSource
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.ContractDiscount
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.DisplayItem
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.testMoveAddonQuote
import com.hedvig.android.feature.movingflow.testMoveHomeQuote
import com.hedvig.android.feature.movingflow.testMoveIntentQuotesFragment
import com.hedvig.android.feature.movingflow.testMoveMtaQuote
import kotlinx.datetime.LocalDate
import octopus.type.MoveIntentSourceInput
import org.junit.Test

class MovingFlowQuotesTest {
  @Test
  fun `home quotes are mapped with their costs, deductible and addons`() {
    val quotes = testMoveIntentQuotesFragment(
      homeQuotes = listOf(
        testMoveHomeQuote(
          id = "home",
          amount = 200.0,
          defaultChoice = true,
          tierLevel = 2,
          addons = listOf(testMoveAddonQuote("addon", amount = 49.0)),
        ),
      ),
    ).toMovingFlowQuotes()

    val homeQuote = quotes.homeQuotes.single()
    assertThat(homeQuote.id).isEqualTo("home")
    assertThat(homeQuote.premium).isEqualTo(UiMoney(200.0, UiCurrencyCode.SEK))
    assertThat(homeQuote.netPremiumWithAddons).isEqualTo(UiMoney(180.0, UiCurrencyCode.SEK))
    assertThat(homeQuote.grossPremiumWithAddons).isEqualTo(UiMoney(200.0, UiCurrencyCode.SEK))
    assertThat(homeQuote.startDate).isEqualTo(LocalDate(2026, 2, 1))
    assertThat(homeQuote.discounts).containsExactly(ContractDiscount("Bundle discount", "-20 kr"))
    assertThat(homeQuote.displayItems).containsExactly(DisplayItem("Address", null, "New street 1"))
    assertThat(homeQuote.tierName).isEqualTo("Tier home")
    assertThat(homeQuote.tierLevel).isEqualTo(2)
    assertThat(homeQuote.defaultChoice).isEqualTo(true)
    assertThat(homeQuote.relatedAddonQuotes).single().transform { it.relatedQuoteId to it.addonId }
      .isEqualTo("home" to AddonId("addon"))
    assertThat(homeQuote.relatedAddonQuotes.single().isExcludedByUser).isEqualTo(false)
    assertThat(homeQuote.relatedAddonQuotes.single().premium).isEqualTo(UiMoney(49.0, UiCurrencyCode.SEK))
  }

  @Test
  fun `a zero deductible percentage is treated as no percentage`() {
    val deductible = testMoveIntentQuotesFragment(homeQuotes = listOf(testMoveHomeQuote(id = "home")))
      .toMovingFlowQuotes()
      .homeQuotes
      .single()
      .deductible

    assertThat(deductible).isEqualTo(
      Deductible(amount = UiMoney(1500.0, UiCurrencyCode.SEK), percentage = null, displayText = "1500 kr"),
    )
  }

  @Test
  fun `mta quotes are mapped`() {
    val quotes = testMoveIntentQuotesFragment(
      homeQuotes = listOf(testMoveHomeQuote(id = "home")),
      mtaQuotes = listOf(testMoveMtaQuote(exposureName = "Car ABC123")),
    ).toMovingFlowQuotes()

    val mtaQuote = quotes.mtaQuotes.single()
    assertThat(mtaQuote.exposureName).isEqualTo("Car ABC123")
    assertThat(mtaQuote.premium).isEqualTo(UiMoney(99.0, UiCurrencyCode.SEK))
    assertThat(mtaQuote.discounts).isEqualTo(emptyList())
  }

  @Test
  fun `addons excluded by the user are not included`() {
    val homeQuote = testMoveIntentQuotesFragment(
      homeQuotes = listOf(
        testMoveHomeQuote(
          id = "home",
          addons = listOf(testMoveAddonQuote("kept"), testMoveAddonQuote("excluded")),
        ),
      ),
    ).toMovingFlowQuotes().homeQuotes.single()
    val withExclusion = homeQuote.copy(
      relatedAddonQuotes = homeQuote.relatedAddonQuotes.map {
        it.copy(isExcludedByUser = it.addonId == AddonId("excluded"))
      },
    )

    assertThat(withExclusion.includedRelatedAddonQuotes.map { it.addonId })
      .containsExactly(AddonId("kept"))
  }

  @Test
  fun `moving source maps to the backend input, sending OTHER as absent`() {
    assertThat(MovingSource.INSURANCE.toMoveIntentSourceInput())
      .isEqualTo(Optional.present(MoveIntentSourceInput.INSURANCE))
    assertThat(MovingSource.TERMINATION.toMoveIntentSourceInput())
      .isEqualTo(Optional.present(MoveIntentSourceInput.TERMINATION))
    assertThat(MovingSource.OTHER.toMoveIntentSourceInput()).isEqualTo(Optional.absent())
  }

  @Test
  fun `missing quote lists map to empty lists`() {
    val quotes = testMoveIntentQuotesFragment(homeQuotes = emptyList()).toMovingFlowQuotes()

    assertThat(quotes.homeQuotes).isEqualTo(emptyList())
    assertThat(quotes.mtaQuotes).isEqualTo(emptyList())
  }
}
