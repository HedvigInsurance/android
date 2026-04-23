package com.hedvig.android.feature.partner.claim.details.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.feature.partner.claim.details.ui.PartnerClaimDetailUiState
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import octopus.PartnerClaimDetailQuery
import octopus.type.InsuranceDocumentType

internal class GetPartnerClaimDetailUseCase(
  private val apolloClient: ApolloClient,
) {
  fun invoke(claimId: String): Flow<Either<Error, PartnerClaimDetailUiState.Content>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        emitAll(
          apolloClient.query(PartnerClaimDetailQuery(claimId))
            .fetchPolicy(FetchPolicy.NetworkOnly)
            .safeFlow { Error }
            .map { result ->
              either {
                val data = result.bind()
                val claim = data.partnerClaim
                ensureNotNull(claim) { Error }
                PartnerClaimDetailUiState.Content(
                  claimStatusCardUiState = ClaimStatusCardUiState.fromPartnerClaim(claim),
                  claimStatus = claim.status,
                  displayItems = claim.displayItems.map {
                    DisplayItem.fromStrings(it.displayTitle, it.displayValue)
                  },
                  handlerEmail = claim.handlerEmail,
                  termsConditionsUrl = claim.productVariant?.documents
                    ?.firstOrNull { it.type == InsuranceDocumentType.TERMS_AND_CONDITIONS }?.url,
                )
              }
            },
        )
        delay(10.seconds)
      }
    }
  }

  data object Error
}
