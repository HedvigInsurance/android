package ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.ContractType
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseKey
import com.hedvig.android.feature.addon.purchase.navigation.AddonType
import com.hedvig.android.feature.addon.purchase.navigation.CustomizeAddonKey
import com.hedvig.android.feature.addon.purchase.navigation.SubmitFailureKey
import com.hedvig.android.feature.addon.purchase.navigation.SubmitSuccessKey
import com.hedvig.android.feature.addon.purchase.navigation.SummaryKey
import com.hedvig.android.feature.addon.purchase.navigation.SummaryParameters
import com.hedvig.android.feature.addon.purchase.navigation.navigateToAddonPurchased
import com.hedvig.android.feature.addon.purchase.navigation.navigateToAddonSubmitFailure
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryNavigationEffect
import com.hedvig.android.feature.addon.purchase.ui.summary.AddonSummaryState
import com.hedvig.android.feature.addon.purchase.ui.summary.CostBreakdownWithExtras
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Test

/** [AddonSummaryNavigationEffect] wired to the same backstack calls as the summary entry, on the flow's stack. */
@OptIn(ExperimentalTestApi::class)
class AddonSummaryNavigationTest {
  private val activationDate = LocalDate(2024, 12, 30)
  private val params = SummaryParameters(
    productVariant = ProductVariant(
      displayName = "SE Apartment Rent",
      contractGroup = ContractGroup.RENTAL,
      contractType = ContractType.SE_APARTMENT_RENT,
      partner = null,
      perils = emptyList(),
      insurableLimits = emptyList(),
      documents = emptyList(),
      displayTierName = null,
      tierDescription = null,
      termsVersion = "2023-01-01",
    ),
    contractId = "contractId",
    baseInsuranceCost = ItemCost(UiMoney(100.0, UiCurrencyCode.SEK), UiMoney(100.0, UiCurrencyCode.SEK), emptyList()),
    chosenQuotes = emptyList(),
    activationDate = activationDate,
    currentlyActiveAddons = emptyList(),
    quoteId = "quoteId",
    notificationMessage = null,
    addonType = AddonType.SELECTABLE,
  )
  private val anchor = AddonPurchaseKey(
    insuranceIds = listOf("contractId", "otherContractId"),
    source = AddonBannerSource.INSURANCES_TAB,
  )
  private val flowStack: List<HedvigNavKey> = listOf(
    KeyBelowTheFlow,
    anchor,
    CustomizeAddonKey(insuranceId = "contractId", preselectedAddonDisplayNames = emptyList()),
    SummaryKey(params),
  )
  private val backstack = TestBackstack(flowStack.toMutableList())
  private val content = AddonSummaryState.Content(
    insuranceDisplayName = "SE Apartment Rent",
    insuranceExposure = "Bellmansgatan 19A",
    contractGroup = ContractGroup.RENTAL,
    quotes = emptyList(),
    activationDate = activationDate,
    currentlyActiveAddons = emptyList(),
    notificationMessage = null,
    documents = emptyList(),
    costBreakdownWithExtras = CostBreakdownWithExtras(
      totalMonthlyNet = UiMoney(159.0, UiCurrencyCode.SEK),
      totalMonthlyGross = UiMoney(159.0, UiCurrencyCode.SEK),
      totalExtra = UiMoney(59.0, UiCurrencyCode.SEK),
      displayItems = emptyList(),
    ),
    displayItems = emptyList(),
  )
  private var uiState by mutableStateOf<AddonSummaryState>(content)
  private var recompositionTrigger by mutableIntStateOf(0)
  private var shown by mutableStateOf(true)

  /** Which navigation ran, tagged with the [recompositionTrigger] value of the composition that ran it. */
  private val navigations = mutableListOf<Pair<String, Int>>()

  @Before
  fun setUp() {
    // The effect waits for the resumed lifecycle on Dispatchers.Main, which the JVM has no platform implementation of.
    Dispatchers.setMain(UnconfinedTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun ComposeUiTest.showEffect() {
    setContent {
      val composition = recompositionTrigger
      if (shown) {
        AddonSummaryNavigationEffect(
          uiState = uiState,
          // New callbacks on every recomposition, so the effect is recomposed rather than skipped.
          onPurchased = {
            navigations += "purchased" to composition
            backstack.navigateToAddonPurchased(it)
          },
          onSubmitFailed = {
            navigations += "failed" to composition
            backstack.navigateToAddonSubmitFailure()
          },
        )
      }
    }
    waitForIdle()
  }

  @Test
  fun `a purchase replaces the flow from its anchor with the success screen, once`() = runComposeUiTest {
    showEffect()
    uiState = AddonSummaryState.Submitting
    waitForIdle()
    uiState = AddonSummaryState.Purchased(NavigationRequest.of(activationDate))
    waitForIdle()
    recompositionTrigger++
    waitForIdle()
    assertThat(backstack.entries).containsExactly(KeyBelowTheFlow, SubmitSuccessKey(activationDate))
    // Navigating again would append a second success screen, but the calls are counted too.
    assertThat(navigations).containsExactly("purchased" to 0)
  }

  @Test
  fun `a failure pushes the failure screen over the summary once, and a later failure pushes it again`() =
    runComposeUiTest {
      showEffect()
      uiState = AddonSummaryState.Submitting
      waitForIdle()
      val failed = content.copy(submitFailure = NavigationRequest.of(Unit))
      uiState = failed
      waitForIdle()
      recompositionTrigger++
      waitForIdle()
      assertThat(backstack.entries).isEqualTo(flowStack + SubmitFailureKey)

      // The member closes the failure screen; the summary still holds the taken request.
      backstack.popBackstack()
      uiState = failed.copy(notificationMessage = "changed")
      waitForIdle()
      assertThat(backstack.entries).isEqualTo(flowStack)

      uiState = AddonSummaryState.Submitting
      waitForIdle()
      uiState = content.copy(submitFailure = NavigationRequest.of(Unit))
      waitForIdle()
      assertThat(backstack.entries).isEqualTo(flowStack + SubmitFailureKey)
      assertThat(navigations).containsExactly("failed" to 0, "failed" to 1)
    }

  @Test
  fun `a purchase that landed while the summary was not shown navigates once it is shown again`() = runComposeUiTest {
    showEffect()
    shown = false
    waitForIdle()
    uiState = AddonSummaryState.Purchased(NavigationRequest.of(activationDate))
    waitForIdle()
    assertThat(backstack.entries).isEqualTo(flowStack)

    shown = true
    waitForIdle()
    assertThat(backstack.entries).containsExactly(KeyBelowTheFlow, SubmitSuccessKey(activationDate))
    assertThat(navigations).containsExactly("purchased" to 0)
  }

  /**
   * The flow sits in Home's run and the member switched to a side tab, whose run is on top of it, so the summary is not
   * composed. The outcome waits until the member is back on the summary, and never clears the side tab's run.
   */
  @Test
  fun `a purchase that lands under another tab's run navigates only once the member is back on the summary`() =
    runComposeUiTest {
      showEffect()
      shown = false
      backstack.entries += SideTabKey
      waitForIdle()
      uiState = AddonSummaryState.Purchased(NavigationRequest.of(activationDate))
      waitForIdle()
      assertThat(backstack.entries).isEqualTo(flowStack + SideTabKey)

      backstack.entries.remove(SideTabKey)
      shown = true
      waitForIdle()
      assertThat(backstack.entries).containsExactly(KeyBelowTheFlow, SubmitSuccessKey(activationDate))
      assertThat(navigations).containsExactly("purchased" to 0)
    }

  @Test
  fun `loading, error, the summary and a purchase in flight navigate nowhere`() = runComposeUiTest {
    showEffect()
    for (state in listOf(
      AddonSummaryState.Loading,
      AddonSummaryState.Error,
      content,
      AddonSummaryState.Submitting,
    )) {
      uiState = state
      waitForIdle()
    }
    assertThat(backstack.entries).isEqualTo(flowStack)
    assertThat(navigations).isEmpty()
  }
}

/** A screen of another feature that the addon flow was opened from. */
private data object KeyBelowTheFlow : HedvigNavKey

/** The root of a side tab, whose run sits on top of the flow's run while that tab is shown. */
private data object SideTabKey : HedvigNavKey
