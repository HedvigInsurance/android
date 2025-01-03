package ui

import com.hedvig.android.logger.TestLogcatLoggingRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SummaryPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()
//  private val summaryRoute: Summary,
//  private val movingFlowRepository: MovingFlowRepository,
//  private val apolloClient: ApolloClient,

  @Test
  fun `the uiState total is correctly calculated`() = runTest {
//    val tierRepo = FakeChangeTierRepository()
//    val useCase = FakeGetCurrentContractDataUseCase()
//    val presenter = SummaryPresenter(
//      tierRepository = tierRepo,
//      params = summaryParams,
//      getCurrentContractDataUseCase = useCase,
//    )
//    presenter.test(Loading) {
//      useCase.turbine.add(CurrentContractData("currentExposureName").right())
//      tierRepo.quoteTurbine.add(testQuoteWithOneAddon.right())
//      tierRepo.quoteTurbine.add(currentQuote.right())
//      skipItems(1)
//      val state = awaitItem()
//      assertThat(state)
//        .isInstanceOf(SummaryState.Success::class)
//        .prop(SummaryState.Success::total)
//        .isEqualTo(UiMoney(235.0, com.hedvig.android.core.uidata.UiCurrencyCode.SEK))
//    }
  }
}

// val addon = ChangeTierDeductibleAddonQuote(
// addonId = "addonId",
// displayName = "Travel Plus",
// displayItems = listOf(
// ChangeTierDeductibleDisplayItem(
// displayTitle = "Coinsured people",
// displaySubtitle = null,
// displayValue = "Only you",
// ),
// ),
// previousPremium = UiMoney(29.0, SEK),
// premium = UiMoney(30.0, SEK),
// addonVariant = AddonVariant(
// termsVersion = "terms",
// displayName = "addonVariantDisplayName",
// product = "product",
// perils = emptyList(),
// insurableLimits = emptyList(),
// documents = emptyList(),
// ),
// )
