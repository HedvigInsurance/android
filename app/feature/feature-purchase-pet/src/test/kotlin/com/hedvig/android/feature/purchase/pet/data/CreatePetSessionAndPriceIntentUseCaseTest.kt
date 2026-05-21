package com.hedvig.android.feature.purchase.pet.data

import assertk.assertThat
import assertk.assertions.isEqualTo
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
import octopus.PetMemberContactInfoQuery
import octopus.PetPriceIntentCreateMutation
import octopus.PetShopSessionCreateMutation
import octopus.type.CountryCode
import octopus.type.buildMember
import octopus.type.buildPriceIntent
import octopus.type.buildShopSession
import org.junit.Rule
import org.junit.Test

class CreatePetSessionAndPriceIntentUseCaseTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  @OptIn(ApolloExperimental::class)
  @Test
  fun `successful session + intent + member returns SessionAndIntent`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetShopSessionCreateMutation(CountryCode.SE),
        data = PetShopSessionCreateMutation.Data(OctopusFakeResolver) {
          shopSessionCreate = buildShopSession { id = "session-1" }
        },
      )
      registerTestResponse(
        operation = PetPriceIntentCreateMutation(
          shopSessionId = "session-1",
          productName = PRODUCT_NAME_DOG,
        ),
        data = PetPriceIntentCreateMutation.Data(OctopusFakeResolver) {
          priceIntentCreate = buildPriceIntent { id = "intent-1" }
        },
      )
      registerTestResponse(
        operation = PetMemberContactInfoQuery(),
        data = PetMemberContactInfoQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            id = "member-1"
            ssn = "199001011234"
            email = "user@example.com"
          }
        },
      )
    }

    val sut = CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke(PRODUCT_NAME_DOG)

    assertThat(result).isRight().isEqualTo(
      SessionAndIntent(
        shopSessionId = "session-1",
        priceIntentId = "intent-1",
        ssn = "199001011234",
        email = "user@example.com",
      ),
    )
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `member with null ssn returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetShopSessionCreateMutation(CountryCode.SE),
        data = PetShopSessionCreateMutation.Data(OctopusFakeResolver) {
          shopSessionCreate = buildShopSession { id = "session-2" }
        },
      )
      registerTestResponse(
        operation = PetPriceIntentCreateMutation(
          shopSessionId = "session-2",
          productName = PRODUCT_NAME_CAT,
        ),
        data = PetPriceIntentCreateMutation.Data(OctopusFakeResolver) {
          priceIntentCreate = buildPriceIntent { id = "intent-2" }
        },
      )
      registerTestResponse(
        operation = PetMemberContactInfoQuery(),
        data = PetMemberContactInfoQuery.Data(OctopusFakeResolver) {
          currentMember = buildMember {
            id = "member-2"
            ssn = null
            email = "x@example.com"
          }
        },
      )
    }

    val sut = CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke(PRODUCT_NAME_CAT)
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }

  @OptIn(ApolloExperimental::class)
  @Test
  fun `network error on session creation returns ErrorMessage`() = runTest {
    val apolloClient = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = PetShopSessionCreateMutation(CountryCode.SE),
        data = null,
        errors = listOf(Error.Builder(message = "Network error").build()),
      )
    }

    val sut = CreatePetSessionAndPriceIntentUseCaseImpl(apolloClient)
    val result = sut.invoke(PRODUCT_NAME_DOG)
    assertThat(result).isLeft().prop(ErrorMessage::message).isNull()
  }
}
