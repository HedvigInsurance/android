package com.hedvig.android.data.changetier.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.database.TierQuoteMapper
import com.hedvig.android.data.chat.database.TierQuoteDao
import com.hedvig.android.logger.LogPriority.ERROR
import com.hedvig.android.logger.logcat
import octopus.ChangeTierDeductibleCommitIntentMutation

interface ChangeTierRepository {
  suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent>

  suspend fun getQuoteById(id: String): TierDeductibleQuote // TODO: I guess it better to be Either too?

  suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote>

  suspend fun addQuotesToDb(quotes: List<TierDeductibleQuote>)

  suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit>
}

internal class ChangeTierRepositoryImpl(
  private val createChangeTierDeductibleIntentUseCase: CreateChangeTierDeductibleIntentUseCase,
  private val tierQuoteDao: TierQuoteDao,
  private val mapper: TierQuoteMapper,
  private val apolloClient: ApolloClient,
) : ChangeTierRepository {
  override suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    tierQuoteDao.clearAllQuotes()
    val result = createChangeTierDeductibleIntentUseCase.invoke(insuranceId, source)
    result.onRight { intent ->
      val quotes = intent.quotes.map { quote ->
        mapper.quoteToDbModel(quote)
      }
      tierQuoteDao.insertAll(quotes)
    }
    return result
  }

  override suspend fun getQuoteById(id: String): TierDeductibleQuote {
    val dbModel = tierQuoteDao.getOneQuoteById(id)
    return mapper.dbModelToQuote(dbModel)
  }

  override suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote> {
    val list = tierQuoteDao.getQuotesById(ids)
    return list.map { entity ->
      mapper.dbModelToQuote(entity)
    }
  }

  override suspend fun addQuotesToDb(quotes: List<TierDeductibleQuote>) {
    val mapped = quotes.map { quote ->
      mapper.quoteToDbModel(quote)
    }
    tierQuoteDao.insertAll(mapped)
  }

  override suspend fun submitChangeTierQuote(quoteId: String): Either<ErrorMessage, Unit> {
    val result = apolloClient.mutation(ChangeTierDeductibleCommitIntentMutation(quoteId)).safeExecute()
    return either {
      result.fold(
        ifRight = { },
        ifLeft = { left ->
          logcat(ERROR) { "Tried to submit change tier quoteId: $quoteId but got error: $left" }
          raise(ErrorMessage())
        },
      )
    }
  }
}
