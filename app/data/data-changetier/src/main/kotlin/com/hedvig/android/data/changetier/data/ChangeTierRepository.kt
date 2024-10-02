package com.hedvig.android.data.changetier.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.changetier.database.TierQuoteDao
import com.hedvig.android.data.changetier.database.TierQuoteMapper

interface ChangeTierRepository {
  suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent>

  suspend fun clearQuotes()

  suspend fun getQuoteById(id: String): TierDeductibleQuote // TODO: I guess it better to be Either too?

  suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote>
}

internal class ChangeTierRepositoryImpl(
  private val createChangeTierDeductibleIntentUseCase: CreateChangeTierDeductibleIntentUseCase,
  private val tierQuoteDao: TierQuoteDao,
  private val mapper: TierQuoteMapper,
) : ChangeTierRepository {
  override suspend fun startChangeTierIntentAndGetQuotesId(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    val result = createChangeTierDeductibleIntentUseCase.invoke(insuranceId, source)
    result.onRight { intent ->
      val quotes = intent.quotes.map { quote ->
        mapper.quoteToDbModel(quote)
      }
      tierQuoteDao.insertAll(quotes)
    }
    return result
  }

  override suspend fun clearQuotes() {
    tierQuoteDao.clearAllQuotes()
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
}
