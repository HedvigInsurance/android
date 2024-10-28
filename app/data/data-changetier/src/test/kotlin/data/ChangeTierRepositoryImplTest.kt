package data

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.right
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
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
import com.hedvig.android.data.changetier.data.ChangeTierQuoteStorage
import com.hedvig.android.data.changetier.data.ChangeTierRepositoryImpl
import com.hedvig.android.data.changetier.data.CreateChangeTierDeductibleIntentUseCase
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import octopus.ChangeTierDeductibleCommitIntentMutation
import oldTestQuote
import org.junit.Rule
import org.junit.Test
import testQuote

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
  fun `if submitChangeTierQuote() get bad response return ErrorMessage`() = runTest {
    val storage = ChangeTierQuoteStorageImpl()
    val repository = ChangeTierRepositoryImpl(
      apolloClient = apolloClientWithBadResponseToSubmit,
      createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseFake(),
      changeTierQuoteStorage = storage,
    )
    val result = repository.submitChangeTierQuote(testId)
    assertk.assertThat(result)
      .isLeft()
  }

  @Test
  fun `startChangeTierIntentAndGetQuotesId() clears old quotes from DB before proceed`() = runTest {
    val useCase = CreateChangeTierDeductibleIntentUseCaseFake()
    val storage = ChangeTierQuoteStorageImpl()
    val repository = ChangeTierRepositoryImpl(
      apolloClient = apolloClientWithGoodResponseToSubmit,
      createChangeTierDeductibleIntentUseCase = useCase,
      changeTierQuoteStorage = storage,
    )
    storage.insertAll(listOf(oldTestQuote))
    val previousState =
      storage.allQuotesTurbine.awaitItem().takeIf { it.any { quote -> quote.id == oldTestQuote.id } }
    assertk.assertThat(previousState).isNotNull()
    useCase.intentTurbine.add(
      ChangeTierDeductibleIntent(
        LocalDate(2024, 11, 11),
        listOf(testQuote),
      ).right(),
    )
    repository.startChangeTierIntentAndGetQuotesId(testId, SELF_SERVICE)
    val result = storage.allQuotesTurbine.awaitItem().takeIf { it.any { quote -> quote.id == oldTestQuote.id } }
    assertk.assertThat(result).isNull()
  }

  @Test
  fun `getQuoteById() if got null quote from db returns error`() = runTest {
    val storage = ChangeTierQuoteStorageImpl()
    val repository = ChangeTierRepositoryImpl(
      apolloClient = apolloClientWithGoodResponseToSubmit,
      changeTierQuoteStorage = storage,
      createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseFake(),
    )
    storage.putFakeNull()
    val result = repository.getQuoteById(testId)
    assertk.assertThat(result)
      .isLeft()
  }

  @Test
  fun `getQuoteById() if got good quoteDbModel from db returns rightly mapped quote`() = runTest {
    val storage = ChangeTierQuoteStorageImpl()
    val repository = ChangeTierRepositoryImpl(
      apolloClient = apolloClientWithGoodResponseToSubmit,
      changeTierQuoteStorage = storage,
      createChangeTierDeductibleIntentUseCase = CreateChangeTierDeductibleIntentUseCaseFake(),
    )
    storage.putFakeQuoteDbModel()
    val result = repository.getQuoteById(testId)
    assertk.assertThat(result)
      .isRight()
      .isNotNull()
      .isEqualTo(testQuote)
  }
}

internal class ChangeTierQuoteStorageImpl : ChangeTierQuoteStorage {
  var oneQuoteTurbine = Turbine<TierDeductibleQuote?>()

  val allQuotesTurbine = Turbine<List<TierDeductibleQuote>>()

  fun putFakeNull() {
    oneQuoteTurbine.add(null)
  }

  fun putFakeQuoteDbModel() {
    oneQuoteTurbine.add(testQuote)
  }

  override suspend fun insertAll(quotes: List<TierDeductibleQuote>) {
    allQuotesTurbine.add(quotes)
  }

  override suspend fun clearAllQuotes() {
    allQuotesTurbine.add(emptyList())
  }

  override suspend fun getOneQuoteById(id: String): TierDeductibleQuote? {
    return oneQuoteTurbine.awaitItem()
  }

  override suspend fun getQuotesById(ids: List<String>): List<TierDeductibleQuote> {
    return allQuotesTurbine.awaitItem().filter { ids.contains(it.id) }
  }
}

internal class CreateChangeTierDeductibleIntentUseCaseFake : CreateChangeTierDeductibleIntentUseCase {
  val intentTurbine = Turbine<Either<ErrorMessage, ChangeTierDeductibleIntent>>()

  override suspend fun invoke(
    insuranceId: String,
    source: ChangeTierCreateSource,
  ): Either<ErrorMessage, ChangeTierDeductibleIntent> {
    return intentTurbine.awaitItem()
  }
}
