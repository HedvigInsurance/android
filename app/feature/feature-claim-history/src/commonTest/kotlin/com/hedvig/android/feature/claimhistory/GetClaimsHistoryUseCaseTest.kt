package com.hedvig.android.feature.claimhistory

import arrow.core.Either
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isInstanceOf
import com.apollographql.apollo.ApolloClient
import com.apollographql.ktor.ktorClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class GetClaimsHistoryUseCaseTest {
  @Test
  fun `regular and partner claims are merged newest first and carry the partner flag`() = runTest {
    val useCase = GetClaimsHistoryUseCase(apolloClientResponding(regularAndPartnerClaimsJson))

    val result = useCase.invoke().first()

    assertThat(result).isInstanceOf<Either.Right<List<ClaimHistory>>>()
    val claims = (result as Either.Right).value
    assertThat(claims.map { it.id }).containsExactly("partner-1", "claim-1")
    assertThat(claims.map { it.isPartnerClaim }).containsExactly(true, false)
    assertThat(claims.map { it.outcome }).containsExactly(
      ClaimHistory.ClaimOutcome.UNKNOWN,
      ClaimHistory.ClaimOutcome.PAID,
    )
  }

  @Test
  fun `a GraphQL error is a Left`() = runTest {
    val useCase = GetClaimsHistoryUseCase(apolloClientResponding(graphQlErrorJson))

    val result = useCase.invoke().first()

    assertThat(result).isInstanceOf<Either.Left<*>>()
  }

  private fun apolloClientResponding(json: String): ApolloClient {
    val engine = MockEngine {
      respond(
        content = json,
        status = HttpStatusCode.OK,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
      )
    }
    return ApolloClient.Builder()
      .serverUrl("https://example.test/graphql")
      .ktorClient(HttpClient(engine))
      .build()
  }
}

internal val regularAndPartnerClaimsJson = """
  {
    "data": {
      "currentMember": {
        "__typename": "Member",
        "id": "member-1",
        "claimsHistory": [
          {
            "__typename": "Claim",
            "id": "claim-1",
            "outcome": "PAID",
            "status": null,
            "payoutAmount": null,
            "audioUrl": null,
            "memberFreeText": null,
            "targetFileUploadUri": "https://example.test/upload",
            "files": [],
            "claimType": null,
            "productVariant": null,
            "showClaimClosedFlow": false,
            "appealInstructionsUrl": null,
            "isUploadingFilesEnabled": false,
            "infoText": null,
            "submittedAt": "2026-10-01T10:00:00Z",
            "displayItems": [],
            "contractId": null
          }
        ],
        "partnerClaimsHistory": [
          {
            "__typename": "PartnerClaim",
            "id": "partner-1",
            "externalId": "external-1",
            "exposureDisplayName": null,
            "status": null,
            "submittedAt": "2026-10-05",
            "payoutAmount": null,
            "associatedTypeOfContract": null,
            "claimType": null,
            "handlerEmail": null,
            "displayItems": [],
            "productVariant": null
          }
        ]
      }
    }
  }
""".trimIndent()

internal val graphQlErrorJson = """
  {
    "errors": [
      {
        "message": "Something went wrong"
      }
    ]
  }
""".trimIndent()
