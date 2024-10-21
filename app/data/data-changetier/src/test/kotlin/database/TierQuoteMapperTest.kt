package database

import assertk.assertions.isEqualTo
import com.hedvig.android.data.changetier.database.TierQuoteMapper
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import testQuote
import testQuoteDbModel

class TierQuoteMapperTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `quote gets correctly mapped to dbModel`() = runTest {
    val tierQuoteMapper = TierQuoteMapper()
    val result = tierQuoteMapper.quoteToDbModel(testQuote)

    assertk.assertThat(result)
      .isEqualTo(testQuoteDbModel)
  }

  @Test
  fun `dbModel gets correctly mapped to quote`() = runTest {
    val tierQuoteMapper = TierQuoteMapper()
    val result = tierQuoteMapper.dbModelToQuote(testQuoteDbModel)

    assertk.assertThat(result)
      .isEqualTo(testQuote)
  }
}
