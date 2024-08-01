package com.hedvig.android.feature.claim.details.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.feature.claim.details.ui.ClaimDetailUiState
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.audio.player.data.SignedAudioUrl
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.ClaimsQuery
import octopus.ClaimsQuery.Data.CurrentMember.Claim.Conversation
import octopus.fragment.ClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus
import octopus.type.InsuranceDocumentType

internal class GetClaimDetailUiStateUseCase(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) {
  operator fun invoke(claimId: String, forceNetworkFetch: Boolean): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return featureManager.isFeatureEnabled(Feature.ENABLE_CBM).transformLatest { isCbmEnabled ->
      while (currentCoroutineContext().isActive) {
        val queryFlow = queryFlow(forceNetworkFetch, claimId, isCbmEnabled)
        emitAll(queryFlow)
        delay(POLL_INTERVAL)
      }
    }
  }

  private fun queryFlow(
    forceNetworkFetch: Boolean,
    claimId: String,
    isCbmEnabled: Boolean,
  ): Flow<Either<Error, ClaimDetailUiState.Content>> {
    return apolloClient
      .query(ClaimsQuery(isCbmEnabled))
      .apply {
        if (forceNetworkFetch) {
          fetchPolicy(FetchPolicy.NetworkOnly)
        } else {
          fetchPolicy(FetchPolicy.CacheAndNetwork)
        }
      }.safeFlow { _, _ -> Error.NetworkError }
      .map { response: Either<Error.NetworkError, ClaimsQuery.Data> ->
        either {
          val claimsQueryData = response.bind()
          val claim: ClaimsQuery.Data.CurrentMember.Claim? =
            claimsQueryData.currentMember.claims.firstOrNull { it.id == claimId }
          ensureNotNull(claim) { Error.NoClaimFound }
          ClaimDetailUiState.Content.fromClaim(claim, claim.conversation)
        }
      }
  }

  private fun ClaimDetailUiState.Content.Companion.fromClaim(
    claim: ClaimFragment,
    conversation: Conversation?,
  ): ClaimDetailUiState.Content {
    val audioUrl = claim.audioUrl
    val memberFreeText = claim.memberFreeText

    val claimType: String? = claim.claimType
    val incidentDate = claim.incidentDate
    val submittedAt = claim.submittedAt.toLocalDateTime(TimeZone.currentSystemDefault())
    val insuranceDisplayName = claim.productVariant?.displayName
    val termsConditionsUrl =
      claim.productVariant
        ?.documents
        ?.firstOrNull { it.type == InsuranceDocumentType.TERMS_AND_CONDITIONS }
        ?.url

    return ClaimDetailUiState.Content(
      claimId = claim.id,
      conversationId = conversation?.id,
      submittedContent = when {
        audioUrl != null -> {
          ClaimDetailUiState.Content.SubmittedContent.Audio(SignedAudioUrl.fromSignedAudioUrlString(audioUrl))
        }

        memberFreeText != null -> ClaimDetailUiState.Content.SubmittedContent.FreeText(memberFreeText)
        else -> null
      },
      files = claim.files.map {
        UiFile(
          id = it.id,
          name = it.name,
          mimeType = it.mimeType,
          url = it.url,
          localPath = null,
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
      claimType = claimType,
      incidentDate = incidentDate,
      insuranceDisplayName = insuranceDisplayName,
      submittedAt = submittedAt,
      termsConditionsUrl = termsConditionsUrl,
      savedFileUri = null,
      downloadError = null,
      isLoadingPdf = false,
    )
  }

  companion object {
    private val POLL_INTERVAL = 30.seconds
  }
}

sealed interface Error {
  data object NetworkError : Error

  data object NoClaimFound : Error
}
