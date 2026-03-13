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
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.addon.purchase.data.AddonOffer.Selectable
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.feature.addon.purchase.data.GenerateAddonOfferResult
import com.hedvig.android.feature.addon.purchase.data.GetAddonOfferUseCaseImpl
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuoteInsuranceDocument
import com.hedvig.android.feature.addon.purchase.data.UmbrellaAddonQuote
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.AddonGenerateOfferMutation
import octopus.type.CurrencyCode
import octopus.type.buildActiveAddon
import octopus.type.buildAddonContractQuote
import octopus.type.buildAddonDisplayItem
import octopus.type.buildAddonOffer
import octopus.type.buildAddonOfferQuote
import octopus.type.buildAddonOfferSelectable
import octopus.type.buildAddonVariant
import octopus.type.buildItemCost
import octopus.type.buildItemDiscount
import octopus.type.buildMoney
import octopus.type.buildOtherAddonOfferOutput
import octopus.type.buildProductVariant
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
        operation = AddonGenerateOfferMutation(testId),
        errors = listOf(Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithNullData: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonGenerateOfferMutation(testId),
        data = AddonGenerateOfferMutation.Data(OctopusFakeResolver) {
          addonGenerateOffer = buildOtherAddonOfferOutput("Unknown") {}
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullResponseEmptyQuotes: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonGenerateOfferMutation(testId),
        data = AddonGenerateOfferMutation.Data(OctopusFakeResolver) {
          addonGenerateOffer = buildAddonOffer {
            pageTitle = "Extend your coverage"
            pageDescription = "Get extra coverage when you travel abroad"
            whatsIncludedPageTitle = "What is Travel Insurance Plus?"
            whatsIncludedPageDescription = "Travel Insurance Plus is extended coverage"
            infoMessage = null
            currentTotalCost = buildItemCost {
              monthlyNet = buildMoney {
                amount = 500.0
                currencyCode = CurrencyCode.SEK
              }
              monthlyGross = buildMoney {
                amount = 500.0
                currencyCode = CurrencyCode.SEK
              }
              discounts = listOf()
            }
            quote = buildAddonContractQuote {
              quoteId = "quoteId1"
              displayTitle = "Travel plus"
              displayDescription = "For those who travel often: luggage protection and 24/7 assistance worldwide"
              activationDate = LocalDate(2025, 1, 1)
              activeAddons = listOf()
              addonOffer = buildAddonOfferSelectable {
                fieldTitle = "Maximum travel limit"
                selectionTitle = "Choose your maximum travel limit"
                selectionDescription = "Days covered when travelling"
                quotes = listOf()
              }
              baseQuoteCost = buildItemCost {
                monthlyNet = buildMoney {
                  amount = 100.0
                  currencyCode = CurrencyCode.SEK
                }
                monthlyGross = buildMoney {
                  amount = 100.0
                  currencyCode = CurrencyCode.SEK
                }
                discounts = listOf()
              }
              productVariant = buildProductVariant {
                displayName = "Rental"
                typeOfContract = "SE_APARTMENT_RENT"
                termsVersion = "2023-01-01"
                displayNameTier = null
                tierDescription = null
                partner = null
                perils = listOf()
                insurableLimits = listOf()
                documents = listOf()
              }
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithUserError: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonGenerateOfferMutation(testId),
        data = AddonGenerateOfferMutation.Data(OctopusFakeResolver) {
          addonGenerateOffer = buildUserError {
            message = "You have 2 insurances"
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullResponseNoCurrentAddon: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonGenerateOfferMutation(testId),
        data = AddonGenerateOfferMutation.Data(OctopusFakeResolver) {
          addonGenerateOffer = buildAddonOffer {
            pageTitle = mockWithoutUpgrade.pageTitle
            pageDescription = mockWithoutUpgrade.pageDescription
            whatsIncludedPageTitle = mockWithoutUpgrade.whatsIncludedPageTitle
            whatsIncludedPageDescription = mockWithoutUpgrade.whatsIncludedPageDescription
            infoMessage = mockWithoutUpgrade.notificationMessage
            currentTotalCost = buildItemCost {
              monthlyNet = buildMoney {
                amount = mockWithoutUpgrade.currentTotalCost.monthlyNet.amount
                currencyCode = CurrencyCode.SEK
              }
              monthlyGross = buildMoney {
                amount = mockWithoutUpgrade.currentTotalCost.monthlyGross.amount
                currencyCode = CurrencyCode.SEK
              }
              discounts = buildList {
                mockWithoutUpgrade.currentTotalCost.discounts.forEach {
                  add(
                    buildItemDiscount {
                      campaignCode = it.campaignCode
                      displayName = it.displayName
                      displayValue = it.displayValue
                      explanation = it.explanation
                    },
                  )
                }
              }
            }
            quote = buildAddonContractQuote {
              quoteId = mockWithoutUpgrade.umbrellaAddonQuote.quoteId
              displayTitle = mockWithoutUpgrade.umbrellaAddonQuote.displayTitle
              displayDescription = mockWithoutUpgrade.umbrellaAddonQuote.displayDescription
              activationDate = mockWithoutUpgrade.umbrellaAddonQuote.activationDate
              activeAddons = listOf()
              val selectableOffer = mockWithoutUpgrade.umbrellaAddonQuote.addonOffer as Selectable
              addonOffer = buildAddonOfferSelectable {
                fieldTitle = selectableOffer.fieldTitle
                selectionTitle = selectableOffer.selectionTitle
                selectionDescription = selectableOffer.selectionDescription
                quotes = buildList {
                  selectableOffer.addonOptions.forEach { addonQuote ->
                    add(
                      buildAddonOfferQuote {
                        id = addonQuote.addonId
                        displayTitle = addonQuote.displayTitle
                        displayDescription = addonQuote.displayDescription
                        subtype = addonQuote.addonSubtype
                        displayItems = buildList {
                          addonQuote.displayDetails.forEach {
                            add(
                              buildAddonDisplayItem {
                                displayTitle = it.first
                                displayValue = it.second
                              },
                            )
                          }
                        }
                        cost = buildItemCost {
                          monthlyNet = buildMoney {
                            amount = addonQuote.itemCost.monthlyNet.amount
                            currencyCode = CurrencyCode.SEK
                          }
                          monthlyGross = buildMoney {
                            amount = addonQuote.itemCost.monthlyGross.amount
                            currencyCode = CurrencyCode.SEK
                          }
                          discounts = buildList {
                            addonQuote.itemCost.discounts.forEach {
                              add(
                                buildItemDiscount {
                                  campaignCode = it.campaignCode
                                  displayName = it.displayName
                                  displayValue = it.displayValue
                                  explanation = it.explanation
                                },
                              )
                            }
                          }
                        }
                        addonVariant = buildAddonVariant {
                          termsVersion = addonQuote.addonVariant.termsVersion
                          displayName = addonQuote.addonVariant.displayName
                          product = addonQuote.addonVariant.product
                          addonPerils = listOf()
                          documents = listOf()
                        }
                      },
                    )
                  }
                }
              }
              baseQuoteCost = buildItemCost {
                monthlyNet = buildMoney {
                  amount = mockWithoutUpgrade.umbrellaAddonQuote.baseInsuranceCost.monthlyNet.amount
                  currencyCode = CurrencyCode.SEK
                }
                monthlyGross = buildMoney {
                  amount = mockWithoutUpgrade.umbrellaAddonQuote.baseInsuranceCost.monthlyGross.amount
                  currencyCode = CurrencyCode.SEK
                }
                discounts = listOf()
              }
              productVariant = buildProductVariant {
                displayName = mockWithoutUpgrade.umbrellaAddonQuote.productVariant.displayName
                typeOfContract = "SE_APARTMENT_RENT"
                termsVersion = mockWithoutUpgrade.umbrellaAddonQuote.productVariant.termsVersion
                displayNameTier = mockWithoutUpgrade.umbrellaAddonQuote.productVariant.displayTierName
                tierDescription = mockWithoutUpgrade.umbrellaAddonQuote.productVariant.tierDescription
                partner = mockWithoutUpgrade.umbrellaAddonQuote.productVariant.partner
                perils = listOf()
                insurableLimits = listOf()
                documents = listOf()
              }
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithFullResponseWithCurrentAddon: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = AddonGenerateOfferMutation(testId),
        data = AddonGenerateOfferMutation.Data(OctopusFakeResolver) {
          addonGenerateOffer = buildAddonOffer {
            pageTitle = mockWithUpgrade.pageTitle
            pageDescription = mockWithUpgrade.pageDescription
            whatsIncludedPageTitle = mockWithUpgrade.whatsIncludedPageTitle
            whatsIncludedPageDescription = mockWithUpgrade.whatsIncludedPageDescription
            infoMessage = mockWithUpgrade.notificationMessage
            currentTotalCost = buildItemCost {
              monthlyNet = buildMoney {
                amount = mockWithUpgrade.currentTotalCost.monthlyNet.amount
                currencyCode = CurrencyCode.SEK
              }
              monthlyGross = buildMoney {
                amount = mockWithUpgrade.currentTotalCost.monthlyGross.amount
                currencyCode = CurrencyCode.SEK
              }
              discounts = listOf()
            }
            quote = buildAddonContractQuote {
              quoteId = mockWithUpgrade.umbrellaAddonQuote.quoteId
              displayTitle = mockWithUpgrade.umbrellaAddonQuote.displayTitle
              displayDescription = mockWithUpgrade.umbrellaAddonQuote.displayDescription
              activationDate = mockWithUpgrade.umbrellaAddonQuote.activationDate
              activeAddons = buildList {
                mockWithUpgrade.umbrellaAddonQuote.activeAddons.forEach { activeAddon ->
                  add(
                    buildActiveAddon {
                      displayTitle = activeAddon.displayTitle
                      displayDescription = activeAddon.displayDescription
                      cost = buildItemCost {
                        monthlyNet = buildMoney {
                          amount = activeAddon.cost.monthlyNet.amount
                          currencyCode = CurrencyCode.SEK
                        }
                        monthlyGross = buildMoney {
                          amount = activeAddon.cost.monthlyGross.amount
                          currencyCode = CurrencyCode.SEK
                        }
                        discounts = listOf()
                      }
                    },
                  )
                }
              }
              val selectableOffer = mockWithUpgrade.umbrellaAddonQuote.addonOffer as Selectable
              addonOffer = buildAddonOfferSelectable {
                fieldTitle = selectableOffer.fieldTitle
                selectionTitle = selectableOffer.selectionTitle
                selectionDescription = selectableOffer.selectionDescription
                quotes = buildList {
                  selectableOffer.addonOptions.forEach { addonQuote ->
                    add(
                      buildAddonOfferQuote {
                        id = addonQuote.addonId
                        displayTitle = addonQuote.displayTitle
                        displayDescription = addonQuote.displayDescription
                        subtype = addonQuote.addonSubtype
                        displayItems = buildList {
                          addonQuote.displayDetails.forEach {
                            add(
                              buildAddonDisplayItem {
                                displayTitle = it.first
                                displayValue = it.second
                              },
                            )
                          }
                        }
                        cost = buildItemCost {
                          monthlyNet = buildMoney {
                            amount = addonQuote.itemCost.monthlyNet.amount
                            currencyCode = CurrencyCode.SEK
                          }
                          monthlyGross = buildMoney {
                            amount = addonQuote.itemCost.monthlyGross.amount
                            currencyCode = CurrencyCode.SEK
                          }
                          discounts = listOf()
                        }
                        addonVariant = buildAddonVariant {
                          termsVersion = addonQuote.addonVariant.termsVersion
                          displayName = addonQuote.addonVariant.displayName
                          product = addonQuote.addonVariant.product
                          addonPerils = listOf()
                          documents = listOf()
                        }
                      },
                    )
                  }
                }
              }
              baseQuoteCost = buildItemCost {
                monthlyNet = buildMoney {
                  amount = mockWithUpgrade.umbrellaAddonQuote.baseInsuranceCost.monthlyNet.amount
                  currencyCode = CurrencyCode.SEK
                }
                monthlyGross = buildMoney {
                  amount = mockWithUpgrade.umbrellaAddonQuote.baseInsuranceCost.monthlyGross.amount
                  currencyCode = CurrencyCode.SEK
                }
                discounts = listOf()
              }
              productVariant = buildProductVariant {
                displayName = mockWithUpgrade.umbrellaAddonQuote.productVariant.displayName
                typeOfContract = "SE_APARTMENT_RENT"
                termsVersion = mockWithUpgrade.umbrellaAddonQuote.productVariant.termsVersion
                displayNameTier = mockWithUpgrade.umbrellaAddonQuote.productVariant.displayTierName
                tierDescription = mockWithUpgrade.umbrellaAddonQuote.productVariant.tierDescription
                partner = mockWithUpgrade.umbrellaAddonQuote.productVariant.partner
                perils = listOf()
                insurableLimits = listOf()
                documents = listOf()
              }
            }
          }
        },
      )
    }

  @Test
  fun `if FF for addons is off return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to false))
    val sut = GetAddonOfferUseCaseImpl(apolloClientWithFullResponseWithCurrentAddon, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isNull()
  }

  @Test
  fun `if quotes list is empty return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetAddonOfferUseCaseImpl(apolloClientWithFullResponseEmptyQuotes, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft()
      .prop(ErrorMessage::message).isEqualTo(null)
  }

  @Test
  fun `if BE gives error return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetAddonOfferUseCaseImpl(apolloClientWithError, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isEqualTo(null)
  }

  @Test
  fun `if BE gives UserError return ErrorMessage with proper message`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetAddonOfferUseCaseImpl(apolloClientWithUserError, featureManager)
    val result = sut.invoke(testId)
    assertThat(result)
      .isLeft().prop(ErrorMessage::message).isEqualTo("You have 2 insurances")
  }

  @Test
  fun `if BE gives data but it's null return ErrorMessage with null message`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut = GetAddonOfferUseCaseImpl(apolloClientWithNullData, featureManager)
    val result = sut.invoke(testId).leftOrNull().toString()
    assertThat(result)
      .isEqualTo("ErrorMessage(message=null, throwable=null)")
  }

  @Test
  fun `if BE gives full data map it correctly`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val sut1 = GetAddonOfferUseCaseImpl(apolloClientWithFullResponseNoCurrentAddon, featureManager)
    val result1 = sut1.invoke(testId)
    assertThat(result1)
      .isEqualTo(either { mockWithoutUpgrade })
    val sut2 = GetAddonOfferUseCaseImpl(apolloClientWithFullResponseWithCurrentAddon, featureManager)
    val result2 = sut2.invoke(testId)
    assertThat(result2)
      .isEqualTo(either { mockWithUpgrade })
  }
}

private val mockProductVariant = ProductVariant(
  displayName = "Rental",
  contractGroup = ContractGroup.RENTAL,
  contractType = ContractType.SE_APARTMENT_RENT,
  partner = null,
  perils = emptyList(),
  insurableLimits = emptyList(),
  documents = emptyList(),
  displayTierName = null,
  tierDescription = null,
  termsVersion = "2023-01-01",
)

private val mockWithoutUpgrade = GenerateAddonOfferResult.AddonOfferResult(
  pageTitle = "Extend your coverage",
  pageDescription = "Get extra coverage when you travel abroad",
  contractId = "testId",
  notificationMessage = null,
  whatsIncludedPageTitle = "What is Travel Insurance Plus?",
  whatsIncludedPageDescription = "Travel Insurance Plus is extended coverage",
  currentTotalCost = ItemCost(
    monthlyGross = UiMoney(500.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(500.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  umbrellaAddonQuote = UmbrellaAddonQuote(
    quoteId = "quoteId1",
    displayTitle = "Travel plus",
    displayDescription = "For those who travel often: luggage protection and 24/7 assistance worldwide",
    activationDate = LocalDate(2025, 1, 1),
    addonOffer = Selectable(
      fieldTitle = "Maximum travel limit",
      selectionTitle = "Choose your maximum travel limit",
      selectionDescription = "Days covered when travelling",
      addonOptions = nonEmptyListOf(
        AddonQuote(
          addonId = "addonId1",
          displayTitle = "45 days",
          displayDescription = "Mock quote 45 days",
          displayDetails = listOf("Coverage" to "45 days"),
          addonVariant = AddonVariant(
            termsVersion = "",
            displayName = "45 days",
            product = "",
            documents = emptyList(),
            perils = emptyList(),
          ),
          addonSubtype = "45_DAYS",
          documents = emptyList(),
          itemCost = ItemCost(
            monthlyGross = UiMoney(49.0, UiCurrencyCode.SEK),
            monthlyNet = UiMoney(49.0, UiCurrencyCode.SEK),
            discounts = emptyList(),
          ),
        ),
        AddonQuote(
          addonId = "addonId2",
          displayTitle = "60 days",
          displayDescription = "Mock quote 60 days",
          displayDetails = listOf("Coverage" to "60 days"),
          addonVariant = AddonVariant(
            termsVersion = "",
            displayName = "60 days",
            product = "",
            documents = emptyList(),
            perils = emptyList(),
          ),
          addonSubtype = "60_DAYS",
          documents = emptyList(),
          itemCost = ItemCost(
            monthlyGross = UiMoney(59.0, UiCurrencyCode.SEK),
            monthlyNet = UiMoney(59.0, UiCurrencyCode.SEK),
            discounts = emptyList(),
          ),
        ),
      ),
    ),
    activeAddons = emptyList(),
    baseInsuranceCost = ItemCost(
      monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
      monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
      discounts = emptyList(),
    ),
    productVariant = mockProductVariant,
  ),
)

private val mockWithUpgrade = GenerateAddonOfferResult.AddonOfferResult(
  pageTitle = "Extend your coverage",
  pageDescription = "Get extra coverage when you travel abroad",
  contractId = "testId",
  notificationMessage = null,
  whatsIncludedPageTitle = "What is Travel Insurance Plus?",
  whatsIncludedPageDescription = "Travel Insurance Plus is extended coverage",
  currentTotalCost = ItemCost(
    monthlyGross = UiMoney(500.0, UiCurrencyCode.SEK),
    monthlyNet = UiMoney(500.0, UiCurrencyCode.SEK),
    discounts = emptyList(),
  ),
  umbrellaAddonQuote = UmbrellaAddonQuote(
    quoteId = "quoteId1",
    displayTitle = "Travel plus",
    displayDescription = "For those who travel often: luggage protection and 24/7 assistance worldwide",
    activationDate = LocalDate(2025, 1, 1),
    addonOffer = Selectable(
      fieldTitle = "Maximum travel limit",
      selectionTitle = "Choose your maximum travel limit",
      selectionDescription = "Days covered when travelling",
      addonOptions = nonEmptyListOf(
        AddonQuote(
          addonId = "addonId1",
          displayTitle = "60 days",
          displayDescription = "Mock quote 60 days",
          displayDetails = listOf("Coverage" to "60 days"),
          addonVariant = AddonVariant(
            termsVersion = "",
            displayName = "60 days",
            product = "",
            documents = emptyList(),
            perils = emptyList(),
          ),
          addonSubtype = "60_DAYS",
          documents = emptyList(),
          itemCost = ItemCost(
            monthlyGross = UiMoney(59.0, UiCurrencyCode.SEK),
            monthlyNet = UiMoney(59.0, UiCurrencyCode.SEK),
            discounts = emptyList(),
          ),
        ),
      ),
    ),
    activeAddons = listOf(
      CurrentlyActiveAddon(
        displayTitle = "Current Travel Addon",
        displayDescription = null,
        cost = ItemCost(
          monthlyGross = UiMoney(49.0, UiCurrencyCode.SEK),
          monthlyNet = UiMoney(49.0, UiCurrencyCode.SEK),
          discounts = emptyList(),
        ),
      ),
    ),
    baseInsuranceCost = ItemCost(
      monthlyGross = UiMoney(100.0, UiCurrencyCode.SEK),
      monthlyNet = UiMoney(100.0, UiCurrencyCode.SEK),
      discounts = emptyList(),
    ),
    productVariant = mockProductVariant,
  ),
)
