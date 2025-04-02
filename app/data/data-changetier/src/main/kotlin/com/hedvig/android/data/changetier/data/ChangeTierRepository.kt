package com.hedvig.android.data.changetier.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import octopus.ChangeTierDeductibleCommitIntentMutation

interface ChangeTierRepository {
  suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent>

  suspend fun getQuoteById(id: String): Either<ErrorMessage, TierDeductibleQuote>

  suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote>

  suspend fun addQuotesToStorage(quotes: List<TierDeductibleQuote>)

  suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit>

  suspend fun getCurrentQuoteId(): String
}

internal class ChangeTierRepositoryImpl(
  private val createChangeTierDeductibleIntentUseCase: CreateChangeTierDeductibleIntentUseCase,
  private val changeTierQuoteStorage: ChangeTierQuoteStorage,
  private val crossSellAfterFlowRepository: CrossSellAfterFlowRepository,
  private val apolloClient: ApolloClient,
) : ChangeTierRepository {
  override suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    changeTierQuoteStorage.clearAllQuotes()
    return createChangeTierDeductibleIntentUseCase.invoke(insuranceId, source)
      .onLeft { left ->
        logcat { "createChangeTierDeductibleIntentUseCase error: $left" }
      }
      .onRight { intent ->
        changeTierQuoteStorage.insertAll(intent.quotes)
      }
  }

  override suspend fun getQuoteById(id: String): Either<ErrorMessage, TierDeductibleQuote> {
    return either {
      ensureNotNull(changeTierQuoteStorage.getOneQuoteById(id)) {
        ErrorMessage("getQuoteById found no quote with id: $id")
      }
    }
  }

  override suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote> {
    return changeTierQuoteStorage.getQuotesById(ids)
  }

  override suspend fun addQuotesToStorage(quotes: List<TierDeductibleQuote>) {
    changeTierQuoteStorage.insertAll(quotes)
  }

  override suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit> {
    return either {
      apolloClient
        .mutation(ChangeTierDeductibleCommitIntentMutation(quoteId))
        .safeExecute()
        .mapLeft { ErrorMessage("Tried to submit change tier quoteId: $quoteId but got error: $it") }
        .onLeft { left ->
          logcat(ERROR) { "Tried to submit change tier quoteId: $quoteId but got error: $left" }
        }
        .bind()
      crossSellAfterFlowRepository.completedCrossSellTriggeringSelfServiceSuccessfully()
    }
  }

  override suspend fun getCurrentQuoteId(): String {
    return TierConstants.CURRENT_ID
  }
}
