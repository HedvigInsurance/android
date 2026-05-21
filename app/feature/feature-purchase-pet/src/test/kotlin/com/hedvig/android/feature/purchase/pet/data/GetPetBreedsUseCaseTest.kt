package com.hedvig.android.feature.purchase.pet.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNull
import assertk.assertions.prop
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.PetAvailableBreedsQuery
import octopus.type.PriceIntentAnimal
import octopus.type.buildPriceIntentAnimalBreed
import org.junit.Rule
import org.junit.Test

class GetPetBreedsUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  @Test
  fun `successful breeds query returns mapped breeds`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetAvailableBreedsQuery(animal = PriceIntentAnimal.DOG),
        data = PetAvailableBreedsQuery.Data(OctopusFakeResolver) {
          priceIntentAvailableBreeds = listOf(
            buildPriceIntentAnimalBreed {
              id = "DOG_MIXED"
              displayName = "Mixed breed"
              isMixedBreed = true
            },
            buildPriceIntentAnimalBreed {
              id = "DOG_LABRADOR"
              displayName = "Labrador"
              isMixedBreed = false
            },
          )
        },
      )
    }

    val sut = GetPetBreedsUseCaseImpl(apolloClient)
    val result = sut.invoke(PriceIntentAnimal.DOG)

    assertThat(result).isRight().prop(List<Breed>::toList).containsExactly(
      Breed(id = "DOG_MIXED", displayName = "Mixed breed", isMixedBreed = true),
      Breed(id = "DOG_LABRADOR", displayName = "Labrador", isMixedBreed = false),
    )
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `network error returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetAvailableBreedsQuery(animal = PriceIntentAnimal.CAT),
        data = null,
        errors = listOf(Error.Builder(message = "Network error").build()),
      )
    }

    val sut = GetPetBreedsUseCaseImpl(apolloClient)
    val result = sut.invoke(PriceIntentAnimal.CAT)
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }
}
