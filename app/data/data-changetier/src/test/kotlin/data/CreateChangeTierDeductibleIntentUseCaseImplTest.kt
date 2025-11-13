package data

import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCaseImpl
import com.hedvig.android.data.changetier.data.IntentOutput
import com.hedvig.android.data.changetier.data.TierConstants
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.ChangeTierDeductibleCreateIntentMutation
import octopus.type.ChangeTierDeductibleSource.SELF_SERVICE
import octopus.type.CurrencyCode.SEK
import octopus.type.buildChangeTierDeductibleCreateIntentOutput
import octopus.type.buildChangeTierDeductibleFromAgreement
import octopus.type.buildChangeTierDeductibleIntent
import octopus.type.buildChangeTierDeductibleQuote
import octopus.type.buildChangeTierDeflectOutput
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
            deflectOutput = null
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
            deflectOutput = null
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
            deflectOutput = null
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
            deflectOutput = null
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
            deflectOutput = null
          }
        },
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithDeflectOutput: ApolloClient
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
            deflectOutput = buildChangeTierDeflectOutput {
              title = "Deflect title"
              message = "Deflect message"
            }
          }
        },
      )
    }

  @Test
  fun `when BE response has empty quotes return intent with empty quotes`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponseButEmptyQuotes,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)
    assertk.assertThat(result)
      .isNotNull()
      .isRight()
      .prop(ChangeTierDeductibleIntent::intentOutput)
      .isNotNull()
      .prop(IntentOutput::quotes)
      .isEmpty()
  }

  @Test
  fun `when response is fine get a good result`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isRight()
      .prop(ChangeTierDeductibleIntent::intentOutput)
      .isNotNull()
      .prop(IntentOutput::activationDate)
      .isEqualTo(activationDateNovember)

    val ids = result.getOrNull()?.intentOutput?.quotes?.map { it.id }
    assertk.assertThat(ids)
      .isNotNull()
      .isEqualTo(listOf(TierConstants.CURRENT_ID, "id"))

    val deductibleAmount =
      result.getOrNull()?.intentOutput?.quotes?.first { it.id != TierConstants.CURRENT_ID }?.deductible?.deductibleAmount?.amount

    assertk.assertThat(deductibleAmount)
      .isNotNull()
      .isEqualTo(500.0)
  }

  @Test
  fun `when response is otherwise good but the intent and deflect are null the result is ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
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
      val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
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
      val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
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
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithGoodResponse,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)
      .getOrNull()?.intentOutput?.quotes?.filter { it.id == TierConstants.CURRENT_ID }
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

  @Test
  fun `when response is bad the result is ErrorMessage`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
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
  fun `when result's deflectOutput is not null intentOutput should be null and deflectOutput should be populated`() =
    runTest {
      val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
      val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
        apolloClient = apolloClientWithDeflectOutput,
        featureManager = featureManager,
      )
      val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

      assertk.assertThat(result)
        .isNotNull()
        .isRight()

      val intent = result.getOrNull()
      assertk.assertThat(intent?.intentOutput)
        .isNull()

      assertk.assertThat(intent?.deflectOutput)
        .isNotNull()

      assertk.assertThat(intent?.deflectOutput?.title)
        .isEqualTo("Deflect title")
    }

  @Test
  fun `when deflectOutput is present intent field is ignored`() = runTest {
    val featureManager = FakeFeatureManager(fixedMap = mapOf(Feature.TRAVEL_ADDON to true))
    val apolloClientWithBothSet = testApolloClientRule.apolloClient.apply {
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
            deflectOutput = buildChangeTierDeflectOutput {
              title = "Deflect title"
              message = "Deflect message"
            }
          }
        },
      )
    }

    val createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseImpl(
      apolloClient = apolloClientWithBothSet,
      featureManager = featureManager,
    )
    val result = createChangeTierDeductibleIntentUseCase.invoke(testId, ChangeTierCreateSource.SELF_SERVICE)

    assertk.assertThat(result)
      .isNotNull()
      .isRight()

    val intent = result.getOrNull()

    assertk.assertThat(intent?.intentOutput)
      .isNull()

    assertk.assertThat(intent?.deflectOutput)
      .isNotNull()

    assertk.assertThat(intent?.deflectOutput?.title)
      .isEqualTo("Deflect title")
  }
}
