package database

import assertk.assertions.isEqualTo
import com.hedvig.android.core.uidata.UiCurrencyCode.SEK
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleDisplayItem
import com.hedvig.android.data.changetier.data.Deductible
import com.hedvig.android.data.changetier.data.Tier
import com.hedvig.android.data.changetier.data.TierDeductibleQuote
import com.hedvig.android.data.changetier.database.TierQuoteMapper
import com.hedvig.android.data.chat.database.ChangeTierDeductibleDisplayItemDbModel
import com.hedvig.android.data.chat.database.ChangeTierQuoteEntity
import com.hedvig.android.data.chat.database.DeductibleDbModel
import com.hedvig.android.data.chat.database.ProductVariantDbModel
import com.hedvig.android.data.chat.database.TierDbModel
import com.hedvig.android.data.chat.database.UiMoneyDbModel
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TierQuoteMapperTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val testQuoteDbModel = ChangeTierQuoteEntity(
    id = "id0",
    premium = UiMoneyDbModel(199.0, SEK),
    deductible = DeductibleDbModel(
      deductibleAmount = UiMoneyDbModel(0.0, SEK),
      deductiblePercentage = 25,
      description = "Endast en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(
      ChangeTierDeductibleDisplayItemDbModel(
        displayValue = "hhh",
        displaySubtitle = "mmm",
        displayTitle = "ioi",
      ),
    ),
    tier = TierDbModel(
      tierName = "BAS",
      tierLevel = 0,
      tierDescription = "Vårt paket med grundläggande villkor.",
      tierDisplayName = "Bas",
    ),
    productVariant = ProductVariantDbModel(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
      tierName = "Bas",
      tierDescription = "Our most basic coverage",
    ),
  )

  private val testQuote = TierDeductibleQuote(
    id = "id0",
    deductible = Deductible(
      UiMoney(0.0, SEK),
      deductiblePercentage = 25,
      description = "Endast en rörlig del om 25% av skadekostnaden.",
    ),
    displayItems = listOf(
      ChangeTierDeductibleDisplayItem(
        displayValue = "hhh",
        displaySubtitle = "mmm",
        displayTitle = "ioi",
      ),
    ),
    premium = UiMoney(199.0, SEK),
    tier = Tier(
      "BAS",
      tierLevel = 0,
      tierDescription = "Vårt paket med grundläggande villkor.",
      tierDisplayName = "Bas",
    ),
    productVariant = ProductVariant(
      displayName = "Test",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = "test",
      perils = listOf(),
      insurableLimits = listOf(),
      documents = listOf(),
      displayTierName = "Bas",
      tierDescription = "Our most basic coverage",
    ),
  )

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
