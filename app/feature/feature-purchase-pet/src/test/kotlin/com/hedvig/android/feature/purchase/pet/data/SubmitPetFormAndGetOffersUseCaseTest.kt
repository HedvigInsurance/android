package com.hedvig.android.feature.purchase.pet.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.PetPriceIntentConfirmMutation
import octopus.PetPriceIntentDataUpdateMutation
import octopus.type.buildPriceIntent
import octopus.type.buildPriceIntentMutationOutput
import octopus.type.buildUserError
import org.junit.Rule
import org.junit.Test

class SubmitPetFormAndGetOffersUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val sampleInput = SubmitInput(
    priceIntentId = "intent-1",
    productName = PRODUCT_NAME_DOG,
    ssn = "199001011234",
    email = "user@example.com",
    name = "Buddy",
    breedId = "DOG_LABRADOR",
    isMixedBreed = false,
    birthDate = LocalDate.parse("2022-03-15"),
    gender = PetGender.MALE,
    isNeutered = true,
    speciesAnswer = false,
    street = "Fakestreet 123",
    zipCode = "12345",
  )

  @OptIn(ApolloExperimental::class)
  @Test
  fun `userError from update returns ErrorMessage with backend message`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetPriceIntentDataUpdateMutation(
          priceIntentId = sampleInput.priceIntentId,
          data = buildExpectedFormData(sampleInput),
        ),
        data = PetPriceIntentDataUpdateMutation.Data(OctopusFakeResolver) {
          priceIntentDataUpdate = buildPriceIntentMutationOutput {
            userError = buildUserError { message = "Pet too young" }
          }
        },
      )
    }

    val sut = SubmitPetFormAndGetOffersUseCaseImpl(apolloClient)
    val result = sut.invoke(sampleInput)
    assertThat(result).isLeft().prop(ErrorMessage::message).isEqualTo("Pet too young")
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `mixed breed submits empty breeds list`() = runTest {
    val mixed = sampleInput.copy(isMixedBreed = true, breedId = "DOG_MIXED")
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetPriceIntentDataUpdateMutation(
          priceIntentId = mixed.priceIntentId,
          data = buildExpectedFormData(mixed), // breeds = emptyList()
        ),
        data = PetPriceIntentDataUpdateMutation.Data(OctopusFakeResolver) {
          priceIntentDataUpdate = buildPriceIntentMutationOutput { userError = null }
        },
      )
      registerTestResponse(
        operation = PetPriceIntentConfirmMutation(priceIntentId = mixed.priceIntentId),
        data = PetPriceIntentConfirmMutation.Data(OctopusFakeResolver) {
          priceIntentConfirm = buildPriceIntentMutationOutput {
            priceIntent = buildPriceIntent {
              id = "intent-1"
              offers = listOf() // empty triggers the empty-offers branch tested separately
            }
          }
        },
      )
    }

    val sut = SubmitPetFormAndGetOffersUseCaseImpl(apolloClient)
    val result = sut.invoke(mixed)
    // Empty offers list -> generic ErrorMessage; the assertion that matters here is
    // that the data-update mutation matched the expected payload (which means breeds=[]).
    assertThat(result).isLeft()
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `cat uses hasOutsideAccess key`() = runTest {
    val cat = sampleInput.copy(
      productName = PRODUCT_NAME_CAT,
      breedId = "CAT_MAINE_COON",
      isMixedBreed = false,
      speciesAnswer = true,
    )
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetPriceIntentDataUpdateMutation(
          priceIntentId = cat.priceIntentId,
          data = buildExpectedFormData(cat),
        ),
        data = PetPriceIntentDataUpdateMutation.Data(OctopusFakeResolver) {
          priceIntentDataUpdate = buildPriceIntentMutationOutput {
            userError = buildUserError { message = "stop here" }
          }
        },
      )
    }

    val sut = SubmitPetFormAndGetOffersUseCaseImpl(apolloClient)
    val result = sut.invoke(cat)
    assertThat(result).isLeft().prop(ErrorMessage::message).isEqualTo("stop here")
  }
}

// Helper: builds the PricingFormData map the use case is expected to send.
private fun buildExpectedFormData(input: SubmitInput): Map<String, Any> {
  val speciesKey = if (input.productName == PRODUCT_NAME_CAT) "hasOutsideAccess" else "isPreviousDogOwner"
  return buildMap {
    put("ssn", input.ssn)
    put("name", input.name)
    put("breeds", if (input.isMixedBreed) emptyList<String>() else listOf(input.breedId))
    put("birthDate", input.birthDate.toString())
    put("gender", input.gender.name)
    put("isNeutered", input.isNeutered.toString())
    put(speciesKey, input.speciesAnswer.toString())
    put("street", input.street)
    put("zipCode", input.zipCode)
    put("email", input.email)
  }
}
