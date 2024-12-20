package data

import arrow.core.nonEmptyListOf
import arrow.core.raise.either
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.feature.addon.purchase.data.Addon.TravelAddonOffer
import com.hedvig.android.feature.addon.purchase.data.CurrentTravelAddon
import com.hedvig.android.feature.addon.purchase.data.GetTravelAddonOfferUseCaseImpl
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.UpsellAddonOfferMutation
import octopus.type.CurrencyCode
import octopus.type.buildAddonVariant
import octopus.type.buildMoney
import octopus.type.buildUpsellTravelAddonCurrentAddon
import octopus.type.buildUpsellTravelAddonDisplayItem
import octopus.type.buildUpsellTravelAddonOffer
import octopus.type.buildUpsellTravelAddonOfferOutput
import octopus.type.buildUpsellTravelAddonQuote
import octopus.type.buildUserError
import org.junit.Rule
import org.junit.Test

class GetTravelAddonOfferUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  val testId = "testId"

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellAddonOfferMutation(testId),
        errors = listOf(Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithNullData: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellAddonOfferMutation(testId),
        data = UpsellAddonOfferMutation.Data(OctopusFakeResolver) {
          upsellTravelAddonOffer = buildUpsellTravelAddonOfferOutput {
            offer = null
            userError = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullResponseEmptyQuotes: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellAddonOfferMutation(testId),
        data = UpsellAddonOfferMutation.Data(OctopusFakeResolver) {
          upsellTravelAddonOffer = buildUpsellTravelAddonOfferOutput {
            offer = buildUpsellTravelAddonOffer {
              activationDate = mockWithoutUpgrade.activationDate
              descriptionDisplayName = mockWithoutUpgrade.description
              titleDisplayName = mockWithoutUpgrade.title
              currentAddon = null
              quotes = listOf()
            }
            userError = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithUserError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellAddonOfferMutation(testId),
        data = UpsellAddonOfferMutation.Data(OctopusFakeResolver) {
          upsellTravelAddonOffer = buildUpsellTravelAddonOfferOutput {
            userError = buildUserError {
              message = "You have 2 insurances"
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullResponseNoCurrentAddon: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellAddonOfferMutation(testId),
        data = UpsellAddonOfferMutation.Data(OctopusFakeResolver) {
          upsellTravelAddonOffer = buildUpsellTravelAddonOfferOutput {
            offer = buildUpsellTravelAddonOffer {
              activationDate = mockWithoutUpgrade.activationDate
              descriptionDisplayName = mockWithoutUpgrade.description
              titleDisplayName = mockWithoutUpgrade.title
              currentAddon = null
              quotes = buildList {
                add(
                  buildUpsellTravelAddonQuote {
                    addonId = mockWithoutUpgrade.addonOptions[0].addonId
                    displayName = mockWithoutUpgrade.addonOptions[0].displayName
                    quoteId = mockWithoutUpgrade.addonOptions[0].quoteId
                    displayItems = buildList {
                      add(
                        buildUpsellTravelAddonDisplayItem {
                          displayTitle = mockWithoutUpgrade.addonOptions[0].displayDetails[0].first
                          displayValue = mockWithoutUpgrade.addonOptions[0].displayDetails[0].second
                        },
                      )
                    }
                    premium = buildMoney {
                      amount = mockWithoutUpgrade.addonOptions[0].price.amount
                      currencyCode = CurrencyCode.SEK
                    }
                    addonVariant = buildAddonVariant {
                      termsVersion = ""
                      documents = listOf()
                      displayName = "45 days"
                      product = ""
                      perils = listOf()
                      insurableLimits = listOf()
                    }
                  },
                )

                add(
                  buildUpsellTravelAddonQuote {
                    addonId = mockWithoutUpgrade.addonOptions[1].addonId
                    displayName = mockWithoutUpgrade.addonOptions[1].displayName
                    quoteId = mockWithoutUpgrade.addonOptions[1].quoteId
                    displayItems = buildList {
                      add(
                        buildUpsellTravelAddonDisplayItem {
                          displayTitle = mockWithoutUpgrade.addonOptions[1].displayDetails[0].first
                          displayValue = mockWithoutUpgrade.addonOptions[1].displayDetails[0].second
                        },
                      )
                    }
                    premium = buildMoney {
                      amount = mockWithoutUpgrade.addonOptions[1].price.amount
                      currencyCode = CurrencyCode.SEK
                    }
                    addonVariant = buildAddonVariant {
                      termsVersion = ""
                      documents = listOf()
                      displayName = "60 days"
                      product = ""
                      perils = listOf()
                      insurableLimits = listOf()
                    }
                  },
                )
              }
            }
            userError = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullResponseWithCurrentAddon: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = UpsellAddonOfferMutation(testId),
        data = UpsellAddonOfferMutation.Data(OctopusFakeResolver) {
          upsellTravelAddonOffer = buildUpsellTravelAddonOfferOutput {
            offer = buildUpsellTravelAddonOffer {
              activationDate = mockWithUpgrade.activationDate
              descriptionDisplayName = mockWithUpgrade.description
              titleDisplayName = mockWithUpgrade.title
              currentAddon = buildUpsellTravelAddonCurrentAddon {
                displayItems = buildList {
                  add(
                    buildUpsellTravelAddonDisplayItem {
                      displayTitle = mockWithUpgrade.currentTravelAddon!!.displayDetails[0].first
                      displayValue = mockWithUpgrade.currentTravelAddon.displayDetails[0].second
                    },
                  )
                }
                premium = buildMoney {
                  amount = mockWithUpgrade.currentTravelAddon!!.price.amount
                  currencyCode = CurrencyCode.SEK
                }
              }
              quotes = buildList {
                add(
                  buildUpsellTravelAddonQuote {
                    addonId = mockWithUpgrade.addonOptions[0].addonId
                    displayName = mockWithUpgrade.addonOptions[0].displayName
                    quoteId = mockWithUpgrade.addonOptions[0].quoteId
                    displayItems = buildList {
                      add(
                        buildUpsellTravelAddonDisplayItem {
                          displayTitle = mockWithUpgrade.addonOptions[0].displayDetails[0].first
                          displayValue = mockWithUpgrade.addonOptions[0].displayDetails[0].second
                        },
                      )
                    }
                    premium = buildMoney {
                      amount = mockWithUpgrade.addonOptions[0].price.amount
                      currencyCode = CurrencyCode.SEK
                    }
                    addonVariant = buildAddonVariant {
                      termsVersion = ""
                      documents = listOf()
                      displayName = "45 days"
                      product = ""
                      perils = listOf()
                      insurableLimits = listOf()
                    }
                  },
                )
              }
            }
            userError = null
          }
        },
      )
    }

  @Test
  fun `if FF for addons is off return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TRAVEL_ADDON to false))
    val sut = GetTravelAddonOfferUseCaseImpl(apolloClientWithFullResponseWithCurrentAddon, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isNull()
  }

  @Test
  fun `if quotes list is empty return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonOfferUseCaseImpl(apolloClientWithFullResponseEmptyQuotes, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft()
      .prop(ErrorMessage::message).isEqualTo(null)
  }

  @Test
  fun `if BE gives error return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonOfferUseCaseImpl(apolloClientWithError, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isEqualTo(null)
  }

  @Test
  fun `if BE gives UserError return ErrorMessage with proper message`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonOfferUseCaseImpl(apolloClientWithUserError, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isEqualTo("You have 2 insurances")
  }

  @Test
  fun `if BE gives data but it's null return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetTravelAddonOfferUseCaseImpl(apolloClientWithNullData, featureManager)
    val result = sut.invoke(testId).leftOrNull().toString()
    assertThat(result)
      .isEqualTo("ErrorMessage(message=null, throwable=null)")
  }

  @Test
  fun `if BE gives full data map it correctly`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut1 = GetTravelAddonOfferUseCaseImpl(apolloClientWithFullResponseNoCurrentAddon, featureManager)
    val result1 = sut1.invoke(testId)
    assertThat(result1)
      .isEqualTo(either { mockWithoutUpgrade })
    val sut2 = GetTravelAddonOfferUseCaseImpl(apolloClientWithFullResponseWithCurrentAddon, featureManager)
    val result2 = sut2.invoke(testId)
    assertThat(result2)
      .isEqualTo(either { mockWithUpgrade })
  }
}

private val mockWithoutUpgrade = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    TravelAddonQuote(
      quoteId = "id",
      addonId = "addonId1",
      displayName = "45 days",
      displayDetails = listOf("Coverage" to "45 days"),
      addonVariant = AddonVariant(
        termsVersion = "",
        documents = listOf(),
        displayName = "45 days",
        product = "",
        perils = listOf(),
        insurableLimits = listOf(),
      ),
      price = UiMoney(
        49.0,
        UiCurrencyCode.SEK,
      ),
    ),
    TravelAddonQuote(
      displayName = "60 days",
      addonId = "addonId1",
      quoteId = "id",
      displayDetails = listOf("Coverage" to "60 days"),
      addonVariant = AddonVariant(
        termsVersion = "",
        documents = listOf(),
        displayName = "60 days",
        product = "",
        perils = listOf(),
        insurableLimits = listOf(),
      ),
      price = UiMoney(
        60.0,
        UiCurrencyCode.SEK,
      ),
    ),
  ),
  title = "Travel plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2025, 1, 1),
  currentTravelAddon = null,
)

private val mockWithUpgrade = TravelAddonOffer(
  addonOptions = nonEmptyListOf(
    TravelAddonQuote(
      displayName = "60 days",
      addonId = "addonId1",
      quoteId = "id",
      displayDetails = listOf("Coverage" to "60 days"),
      addonVariant = AddonVariant(
        termsVersion = "",
        documents = listOf(),
        displayName = "45 days",
        product = "",
        perils = listOf(),
        insurableLimits = listOf(),
      ),
      price = UiMoney(
        60.0,
        UiCurrencyCode.SEK,
      ),
    ),
  ),
  title = "Travel plus",
  description = "For those who travel often: luggage protection and 24/7 assistance worldwide",
  activationDate = LocalDate(2025, 1, 1),
  currentTravelAddon = CurrentTravelAddon(
    UiMoney(49.0, UiCurrencyCode.SEK),
    listOf("Coverage" to "45 days"),
  ),
)
