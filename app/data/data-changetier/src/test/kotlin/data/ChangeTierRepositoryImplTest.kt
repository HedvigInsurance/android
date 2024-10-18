package data

import app.cash.turbine.Turbine
import arrow.core.Either
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.apollographql.apollo.testing.registerTestResponse
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.test.isLeft
import com.hedvig.android.core.common.test.isRight
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.SELF_SERVICE
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.data.changetier.data.ChangeTierRepositoryImpl
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCase
import com.hedvig.android.data.changetier.database.TierQuoteMapper
import com.hedvig.android.data.chat.database.ChangeTierQuoteEntity
import com.hedvig.android.data.chat.database.TierQuoteDao
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import octopus.ChangeTierDeductibleCommitIntentMutation
import oldTestQuoteDbModel
import org.junit.Rule
import org.junit.Test
import testQuote
import testQuoteDbModel

class ChangeTierRepositoryImplTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.MAP)

  private val testId = "testId"

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithBadResponseToSubmit: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCommitIntentMutation(
          quoteId = testId,
        ),
        errors = listOf(com.apollographql.apollo.api.Error.Builder(message = "Bad message").build()),
      )
    }

  @OptIn(ApolloExperimental::class)
  private val apolloClientWithGoodResponseToSubmit: ApolloClient
    get() = testApolloClientRule.apolloClient.apply {
      registerTestResponse(
        operation = ChangeTierDeductibleCommitIntentMutation(
          quoteId = testId,
        ),
      )
    }

  @Test
  fun `startChangeTierIntentAndGetQuotesId() clears old quotes from DB before proceed`() = runTest {
    val mockDao = TierQuoteDaoFakeImpl()
    val mapper = TierQuoteMapper()
    val repository = ChangeTierRepositoryImpl(
      mapper = mapper,
      tierQuoteDao = mockDao,
      apolloClient = apolloClientWithGoodResponseToSubmit,
      createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseFake()
    )
    mockDao.insertAll(
      listOf(oldTestQuoteDbModel)
    )
    repository.startChangeTierIntentAndGetQuotesId("insurance", SELF_SERVICE)
    val result = repository.getQuotesById(listOf(oldTestQuoteDbModel.id))
    assertk.assertThat(result)
      .isEqualTo(emptyList())
  }

  @Test
  fun `getQuoteById() if got null quote from db returns error`() = runTest {
    val mockDao = TierQuoteDaoFakeImpl()
    val mapper = TierQuoteMapper()
    val repository = ChangeTierRepositoryImpl(
      mapper = mapper,
      tierQuoteDao = mockDao,
      apolloClient = apolloClientWithGoodResponseToSubmit,
      createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseFake()
    )
    mockDao.putFakeNull()
    val result = repository.getQuoteById(testId)
    assertk.assertThat(result)
      .isLeft()
  }

  @Test
  fun `getQuoteById() if got good quoteDbModel from db returns rightly mapped quote`() = runTest {
    val mockDao = TierQuoteDaoFakeImpl()
    val mapper = TierQuoteMapper()
    val repository = ChangeTierRepositoryImpl(
      mapper = mapper,
      tierQuoteDao = mockDao,
      apolloClient = apolloClientWithGoodResponseToSubmit,
      createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseFake()
    )
    mockDao.putFakeQuoteDbModel()
    val result = repository.getQuoteById(testId)
    assertk.assertThat(result)
      .isRight()
      .isNotNull()
      .isEqualTo(testQuote)
  }
}

internal class TierQuoteDaoFakeImpl : TierQuoteDao {

  private val oneQuoteTurbine = Turbine<ChangeTierQuoteEntity?>()

  private val allQuotesTurbine = Turbine<List<ChangeTierQuoteEntity>>()

  fun putFakeNull() {
    oneQuoteTurbine.add(null)
  }
  fun putFakeQuoteDbModel() {
    oneQuoteTurbine.add(testQuoteDbModel)
  }

  override suspend fun insertAll(quotes: List<ChangeTierQuoteEntity>) {
    allQuotesTurbine.add(quotes)
  }

  override suspend fun clearAllQuotes() {
    allQuotesTurbine.add(emptyList())
  }

  override suspend fun getOneQuoteById(id: String): ChangeTierQuoteEntity? {
    return oneQuoteTurbine.awaitItem()
  }

  override suspend fun getQuotesById(ids: List<String>): List<ChangeTierQuoteEntity> {
    return allQuotesTurbine.takeItem().filter { ids.contains(it.id) }
  }

}

internal class CreateChangeTierDeductibleIntentUseCaseFake: CreateChangeTierDeductibleIntentUseCase {

  val intentTurbine = Turbine<Either<ErrorMessage, ChangeTierDeductibleIntent>>()

  override suspend fun invoke(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    return intentTurbine.awaitItem()
  }

}
