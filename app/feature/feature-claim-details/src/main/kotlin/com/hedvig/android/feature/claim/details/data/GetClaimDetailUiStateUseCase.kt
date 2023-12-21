package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import octopus.ClaimsQuery
import octopus.fragment.ClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus

internal class GetClaimDetailUiStateUseCase(
  private val apolloClient: ApolloClient,
) {
  operator fun invoke(claimId: String, forceNetworkFetch: Boolean): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return flow {
      while (currentCoroutineContext().isActive) {
        val queryFlow = queryFlow(forceNetworkFetch, claimId)
        emitAll(queryFlow)
        delay(POLL_INTERVAL)
      }
    }
  }

  private fun queryFlow(forceNetworkFetch: Boolean, claimId: String): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return apolloClient
      .query(ClaimsQuery())
      .apply {
        if (forceNetworkFetch) {
          fetchPolicy(FetchPolicy.NetworkOnly)
        } else {
          fetchPolicy(FetchPolicy.CacheAndNetwork)
        }
      }
      .safeFlow { _, _ -> Error.NetworkError }
      .map { response: Either<Error.NetworkError, ClaimsQuery.Data> ->
        either {
          val claimsQueryData = response.bind()
          val claim: ClaimsQuery.Data.CurrentMember.Claim? =
            claimsQueryData.currentMember.claims.firstOrNull { it.id == claimId }
          ensureNotNull(claim) { Error.NoClaimFound }
          ClaimDetailUiState.Content.fromClaim(claim)
        }
      }
  }

  private fun ClaimDetailUiState.Content.Companion.fromClaim(claim: ClaimFragment): ClaimDetailUiState.Content {
    val audioUrl = claim.audioUrl
    val memberFreeText = claim.memberFreeText
    return ClaimDetailUiState.Content(
      claimId = claim.id,
      submittedContent = when {
        audioUrl != null -> {
          ClaimDetailUiState.Content.SubmittedContent.Audio(SignedAudioUrl.fromSignedAudioUrlString(audioUrl))
        }

        memberFreeText != null -> ClaimDetailUiState.Content.SubmittedContent.FreeText(memberFreeText)
        else -> null
      },
      files = claim.files.map {
        ClaimDetailUiState.Content.ClaimFile(
          id = it.id,
          name = it.name,
          mimeType = it.mimeType,
          url = it.url,
          thumbnailUrl = it.thumbnailUrl,
        )
      },
      claimStatusCardUiState = ClaimStatusCardUiState.fromClaimStatusCardsQuery(claim),
      claimStatus = when (claim.status) {
        ClaimStatus.CREATED -> ClaimDetailUiState.Content.ClaimStatus.CREATED
        ClaimStatus.IN_PROGRESS -> ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS
        ClaimStatus.CLOSED -> ClaimDetailUiState.Content.ClaimStatus.CLOSED
        ClaimStatus.REOPENED -> ClaimDetailUiState.Content.ClaimStatus.REOPENED
        ClaimStatus.UNKNOWN__,
        null,
        -> ClaimDetailUiState.Content.ClaimStatus.UNKNOWN
      },
      claimOutcome = when (claim.outcome) {
        ClaimOutcome.PAID -> ClaimDetailUiState.Content.ClaimOutcome.PAID
        ClaimOutcome.NOT_COMPENSATED -> ClaimDetailUiState.Content.ClaimOutcome.NOT_COMPENSATED
        ClaimOutcome.NOT_COVERED -> ClaimDetailUiState.Content.ClaimOutcome.NOT_COVERED
        ClaimOutcome.UNKNOWN__,
        null,
        -> ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN
      },
      uploadUri = claim.targetFileUploadUri,
      isUploadingFile = false,
      uploadError = null,
    )
  }

  companion object {
    private val POLL_INTERVAL = 30.seconds
  }
}

sealed interface Error {
  object NetworkError : Error

  object NoClaimFound : Error
}
