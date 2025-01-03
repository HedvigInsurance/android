package data

import assertk.assertions.first
import assertk.assertions.index
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCaseImpl
import com.hedvig.android.data.changetier.data.TierConstants
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlin.collections.List
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.ChangeTierDeductibleCreateIntentMutation
import octopus.type.ChangeTierDeductibleSource.SELF_SERVICE
import octopus.type.CurrencyCode.SEK
import octopus.type.buildAddonVariant
import octopus.type.buildChangeTierDeductibleAddonQuote
import octopus.type.buildChangeTierDeductibleCreateIntentOutput
import octopus.type.buildChangeTierDeductibleDisplayItem
import octopus.type.buildChangeTierDeductibleFromAgreement
import octopus.type.buildChangeTierDeductibleIntent
import octopus.type.buildChangeTierDeductibleQuote
import octopus.type.buildDeductible
import octopus.type.buildMoney
import octopus.type.buildProductVariant
import org.junit.Rule
import org.junit.Test

class CreateChangeTierDeductibleIntentUseCaseImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val testId = "testId"
  private val testSource = SELF_SERVICE
  private val activationDateNovember = LocalDate(2024, 11, 15)

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithBadResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCreateIntentMutation(
          contractId = testId,
          source = testSource,
          addonsFlagOn = true,
        ),
        errors = listOf(com.apollographql.apollo.api.Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodButNullResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCreateIntentMutation(
          contractId = testId,
          source = testSource,
          addonsFlagOn = true,
        ),
        data = ChangeTierDeductibleCreateIntentMutation.Data(OctopusFakeResolver) {
          changeTierDeductibleCreateIntent = buildChangeTierDeductibleCreateIntentOutput {
            intent = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseButNullTierNameInExisting: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCreateIntentMutation(
          contractId = testId,
          source = testSource,
          addonsFlagOn = true,
        ),
        data = ChangeTierDeductibleCreateIntentMutation.Data(OctopusFakeResolver) {
          changeTierDeductibleCreateIntent = buildChangeTierDeductibleCreateIntentOutput {
            intent = buildChangeTierDeductibleIntent {
              activationDate = activationDateNovember
              agreementToChange = buildChangeTierDeductibleFromAgreement {
                premium = buildMoney {
                  amount = 169.0
                  currencyCode = SEK
                }
                deductible = buildDeductible {
                  displayText = "A very good deductible"
                  percentage = 0
                  amount = buildMoney {
                    amount = 3000.0
                    currencyCode = SEK
                  }
                }
                displayItems = listOf()
                tierLevel = 1
                tierName = null
                productVariant = buildProductVariant {
                  displayName = "Variant"
                  typeOfContract = "SE_APARTMENT_RENT"
                  partner = null
                  perils = listOf()
                  insurableLimits = listOf()
                  documents = listOf()
                  displayNameTier = "Standard"
                  tierDescription = "Our standard coverage"
                }
              }
              quotes = List(1) {
                buildChangeTierDeductibleQuote {
                  id = "id"
                  premium = buildMoney {
                    amount = 500.0
                    currencyCode = SEK
                  }
                  deductible = buildDeductible {
                    displayText = "A very good deductible"
                    percentage = 0
                    amount = buildMoney {
                      amount = 500.0
                      currencyCode = SEK
                    }
                  }
                  displayItems = listOf()
                  tierLevel = 1
                  tierName = "STANDARD"
                  productVariant = buildProductVariant {
                    displayName = "Variant"
                    typeOfContract = "SE_APARTMENT_RENT"
                    partner = null
                    perils = listOf()
                    insurableLimits = listOf()
                    documents = listOf()
                    displayNameTier = "Standard"
                    tierDescription = "Our standard coverage"
                  }
                }
              }
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseButNullTierNameInOneQuote: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCreateIntentMutation(
          contractId = testId,
          source = testSource,
          addonsFlagOn = true,
        ),
        data = ChangeTierDeductibleCreateIntentMutation.Data(OctopusFakeResolver) {
          changeTierDeductibleCreateIntent = buildChangeTierDeductibleCreateIntentOutput {
            intent = buildChangeTierDeductibleIntent {
              activationDate = activationDateNovember
              agreementToChange = buildChangeTierDeductibleFromAgreement {
                premium = buildMoney {
                  amount = 169.0
                  currencyCode = SEK
                }
                deductible = buildDeductible {
                  displayText = "A very good deductible"
                  percentage = 0
                  amount = buildMoney {
                    amount = 3000.0
                    currencyCode = SEK
                  }
                }
                displayItems = listOf()
                tierLevel = 1
                tierName = "STANDARD"
                productVariant = buildProductVariant {
                  displayName = "Variant"
                  typeOfContract = "SE_APARTMENT_RENT"
                  partner = null
                  perils = listOf()
                  insurableLimits = listOf()
                  documents = listOf()
                  displayNameTier = "Standard"
                  tierDescription = "Our standard coverage"
                }
              }
              quotes = List(1) {
                buildChangeTierDeductibleQuote {
                  id = "id"
                  premium = buildMoney {
                    amount = 500.0
                    currencyCode = SEK
                  }
                  deductible = buildDeductible {
                    displayText = "A very good deductible"
                    percentage = 0
                    amount = buildMoney {
                      amount = 500.0
                      currencyCode = SEK
                    }
                  }
                  displayItems = listOf()
                  tierLevel = 1
                  tierName = null
                  productVariant = buildProductVariant {
                    displayName = "Variant"
                    typeOfContract = "SE_APARTMENT_RENT"
                    partner = null
                    perils = listOf()
                    insurableLimits = listOf()
                    documents = listOf()
                    displayNameTier = "Standard"
                    tierDescription = "Our standard coverage"
                  }
                }
              }
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseButEmptyQuotes: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCreateIntentMutation(
          contractId = testId,
          source = testSource,
          addonsFlagOn = true,
        ),
        data = ChangeTierDeductibleCreateIntentMutation.Data(OctopusFakeResolver) {
          changeTierDeductibleCreateIntent = buildChangeTierDeductibleCreateIntentOutput {
            intent = buildChangeTierDeductibleIntent {
              activationDate = activationDateNovember
              agreementToChange = buildChangeTierDeductibleFromAgreement {
                premium = buildMoney {
                  amount = 169.0
                  currencyCode = SEK
                }
                deductible = buildDeductible {
                  displayText = "A very good deductible"
                  percentage = 0
                  amount = buildMoney {
                    amount = 3000.0
                    currencyCode = SEK
                  }
                }
                displayItems = listOf()
                tierLevel = 1
                tierName = "STANDARD"
                productVariant = buildProductVariant {
                  displayName = "Variant"
                  typeOfContract = "SE_APARTMENT_RENT"
                  partner = null
                  perils = listOf()
                  insurableLimits = listOf()
                  documents = listOf()
                  displayNameTier = "Standard"
                  tierDescription = "Our standard coverage"
                }
              }
              quotes = listOf()
            }
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponse: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCreateIntentMutation(
          contractId = testId,
          source = testSource,
          addonsFlagOn = false,
        ),
        data = ChangeTierDeductibleCreateIntentMutation.Data(OctopusFakeResolver) {
          changeTierDeductibleCreateIntent = buildChangeTierDeductibleCreateIntentOutput {
            intent = buildChangeTierDeductibleIntent {
              activationDate = activationDateNovember
              agreementToChange = buildChangeTierDeductibleFromAgreement {
                premium = buildMoney {
                  amount = 169.0
                  currencyCode = SEK
                }
                deductible = buildDeductible {
                  displayText = "A very good deductible"
                  percentage = 0
                  amount = buildMoney {
                    amount = 3000.0
                    currencyCode = SEK
                  }
                }
                displayItems = listOf()
                tierLevel = 1
                tierName = "STANDARD"
                productVariant = buildProductVariant {
                  displayName = "Variant"
                  typeOfContract = "SE_APARTMENT_RENT"
                  partner = null
                  perils = listOf()
                  insurableLimits = listOf()
                  documents = listOf()
                  displayNameTier = "Standard"
                  tierDescription = "Our standard coverage"
                }
              }
              quotes = List(1) {
                buildChangeTierDeductibleQuote {
                  id = "id"
                  premium = buildMoney {
                    amount = 500.0
                    currencyCode = SEK
                  }
                  deductible = buildDeductible {
                    displayText = "A very good deductible"
                    percentage = 0
                    amount = buildMoney {
                      amount = 500.0
                      currencyCode = SEK
                    }
                  }
                  displayItems = listOf()
                  tierLevel = 1
                  tierName = "STANDARD"
                  productVariant = buildProductVariant {
                    displayName = "Variant"
                    typeOfContract = "SE_APARTMENT_RENT"
                    partner = null
                    perils = listOf()
                    insurableLimits = listOf()
                    documents = listOf()
                    displayNameTier = "Standard"
                    tierDescription = "Our standard coverage"
                  }
                }
              }
            }
          }
        },
      )

      registerTestResponse(
        operation = ChangeTierDeductibleCreateIntentMutation(
          contractId = testId,
          source = testSource,
          addonsFlagOn = true,
        ),
        data = ChangeTierDeductibleCreateIntentMutation.Data(OctopusFakeResolver) {
          changeTierDeductibleCreateIntent = buildChangeTierDeductibleCreateIntentOutput {
            intent = buildChangeTierDeductibleIntent {
              activationDate = activationDateNovember
              agreementToChange = buildChangeTierDeductibleFromAgreement {
                premium = buildMoney {
                  amount = 169.0
                  currencyCode = SEK
                }
                deductible = buildDeductible {
                  displayText = "A very good deductible"
                  percentage = 0
                  amount = buildMoney {
                    amount = 3000.0
                    currencyCode = SEK
                  }
                }
                displayItems = listOf()
                tierLevel = 1
                tierName = "STANDARD"
                productVariant = buildProductVariant {
                  displayName = "Variant"
                  typeOfContract = "SE_APARTMENT_RENT"
                  partner = null
                  perils = listOf()
                  insurableLimits = listOf()
                  documents = listOf()
                  displayNameTier = "Standard"
                  tierDescription = "Our standard coverage"
                }
              }
              quotes = List(1) {
                buildChangeTierDeductibleQuote {
                  id = "id"
                  premium = buildMoney {
                    amount = 500.0
                    currencyCode = SEK
                  }
                  deductible = buildDeductible {
                    displayText = "A very good deductible"
                    percentage = 0
                    amount = buildMoney {
                      amount = 500.0
                      currencyCode = SEK
                    }
                  }
                  displayItems = listOf()
                  tierLevel = 1
                  tierName = "STANDARD"
                  addons = List(1) {
                    buildChangeTierDeductibleAddonQuote {
                      addonId = "addonId"
                      displayName = "Travel Plus"
                      displayItems = List(1) {
                        buildChangeTierDeductibleDisplayItem {
                          displayTitle = "Coinsured people"
                          displaySubtitle = null
                          displayValue = "Only you"
                        }
                      }
                      previousPremium = buildMoney {
                        currencyCode = SEK
                        amount = 29.0
                      }
                      premium = buildMoney {
                        currencyCode = SEK
                        amount = 30.0
                      }
                      addonVariant = buildAddonVariant {
                        termsVersion = "terms"
                        displayName = "addonVariantDisplayName"
                        product = "product"
                        perils = emptyList()
                        insurableLimits = emptyList()
                        documents = emptyList()
                      }
                    }
                  }
                  productVariant = buildProductVariant {
                    displayName = "Variant"
                    typeOfContract = "SE_APARTMENT_RENT"
                    partner = null
                    perils = listOf()
                    insurableLimits = listOf()
                    documents = listOf()
                    displayNameTier = "Standard"
                    tierDescription = "Our standard coverage"
                  }
                }
              }
            }
          }
        },
      )
    }

  @Test
  fun `when BE response has empty quotes return intent with empty quotes`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponseButEmptyQuotes,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)
    assertk.assertThat(result)
      .isNotNull()
      .isRight()
      .prop(ChangeTierDeductibleIntent::quotes)
      .isEmpty()
  }

  @Test
  fun `when response is fine and tier feature flag is on get a good result`() = runTest {
    val featureManager = FakeFeatureManager2(
      fixedMap = mapOf(
        Feature.TIER to true,
        Feature.TRAVEL_ADDON to true,
      ),
    )
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isRight()
      .prop(ChangeTierDeductibleIntent::activationDate)
      .isEqualTo(activationDateNovember)

    val ids = result.getOrNull()?.quotes?.map { it.id }
    assertk.assertThat(ids)
      .isNotNull()
      .isEqualTo(listOf(TierConstants.CURRENT_ID, "id"))

    val deductibleAmount =
      result.getOrNull()?.quotes?.first { it.id != TierConstants.CURRENT_ID }?.deductible?.deductibleAmount?.amount

    assertk.assertThat(deductibleAmount)
      .isNotNull()
      .isEqualTo(500.0)
  }

  @Test
  fun `when response is fine but tier feature flag is off the result is ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to false, Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isLeft()
  }

  @Test
  fun `when response is fine and addon feature flag is off the result has empty addons list`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to false))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isRight()
      .prop(ChangeTierDeductibleIntent::quotes)
      .index(1) // index(0) is agreement to change
      .prop(TierDeductibleQuote::addons)
      .isEmpty()
  }

  @Test
  fun `when response is fine and addon feature flag is on the result has addons list properly populated`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isRight()
      .prop(ChangeTierDeductibleIntent::quotes)
      .index(1) // index(0) is agreement to change
      .prop(TierDeductibleQuote::addons)
      .first()
      .isEqualTo(
        com.hedvig.android.data.changetier.data.ChangeTierDeductibleAddonQuote(
          addonId = "addonId",
          displayName = "Travel Plus",
          displayItems = listOf(
            ChangeTierDeductibleDisplayItem(
              displayTitle = "Coinsured people",
              displaySubtitle = null,
              displayValue = "Only you",
            ),
          ),
          previousPremium = UiMoney(29.0, com.hedvig.android.core.uidata.UiCurrencyCode.SEK),
          premium = UiMoney(30.0, com.hedvig.android.core.uidata.UiCurrencyCode.SEK),
          addonVariant = com.hedvig.android.data.productvariant.AddonVariant(
            termsVersion = "terms",
            displayName = "addonVariantDisplayName",
            product = "product",
            perils = emptyList(),
            insurableLimits = emptyList(),
            documents = emptyList(),
          ),
        ),
      )
  }

  @Test
  fun `when response is bad and tier feature flag is on the result is ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithBadResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isLeft()
  }

  @Test
  fun `when response is otherwise good but the intent is null the result is ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodButNullResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isLeft()
  }

  @Test
  fun `when response is otherwise good but the tierName in existing agreement is null the result is ErrorMessage`() =
    runTest {
      val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to true))
      val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseButNullTierNameInExisting,
        featureManager = featureManager,
      )
      val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

      assertk.assertThat(result)
        .isNotNull()
        .isLeft()
    }

  @Test
  fun `when response is otherwise good but the tierName in one of the quotes is null the result is ErrorMessage`() =
    runTest {
      val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to true))
      val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
        apolloClient = apolloClientWithGoodResponseButNullTierNameInOneQuote,
        featureManager = featureManager,
      )
      val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

      assertk.assertThat(result)
        .isNotNull()
        .isLeft()
    }

  @Test
  fun `in good response one of the quotes should have the current const id`() = runTest {
    val featureManager = FakeFeatureManager2(fixedMap = mapOf(Feature.TIER to true, Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)
      .getOrNull()?.quotes?.filter { it.id == TierConstants.CURRENT_ID }
    val resultSize = result?.size
    val resultFirst = result?.first()

    assertk.assertThat(resultSize)
      .isNotNull()
      .isEqualTo(1)

    assertk.assertThat(resultFirst)
      .isNotNull()
      .prop(TierDeductibleQuote::id)
      .isEqualTo(TierConstants.CURRENT_ID)
  }
}
