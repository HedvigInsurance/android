package com.hedvig.android.app.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.feature.editcoinsured.navigation.CoInsuredAddOrRemoveKey
import com.hedvig.android.feature.editcoinsured.navigation.EditCoInsuredTriageKey
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.feature.insurances.navigation.InsurancesKey
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import org.junit.Rule
import org.junit.Test

/**
 * Back and Up on the step the co-insured triage leaves the member on, for each way the triage is reached. The triage
 * replaces itself with the step through `navigateAndPopUpTo<EditCoInsuredTriageKey>(step, inclusive = true)`, which is
 * what the feature's `navigateFromTriage` does when an [EditCoInsuredTriageKey] is on the stack; its co-owners deep link
 * key has the same shape (no deep link ancestry) and is internal to the feature.
 */
internal class EditCoInsuredTriageStackTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val triage = EditCoInsuredTriageKey(contractId = "contract-a", type = CoInsuredFlowType.CoOwners)
  private val step = CoInsuredAddOrRemoveKey("contract-a", CoInsuredFlowType.CoOwners)
  private var finished = false
  private var escapedTo: List<HedvigNavKey>? = null

  private fun controllerWith(vararg keys: HedvigNavKey) = BackstackController(
    mutableStateListOf(*keys),
    mutableStateMapOf(),
    mutableStateOf(null),
    mutableStateOf(null),
    finishApp = { finished = true },
  ).apply { escapeToOwnTask = { escapedTo = it } }

  private fun BackstackController.leaveTriageFor(step: HedvigNavKey) {
    navigateAndPopUpTo<EditCoInsuredTriageKey>(step, inclusive = true)
  }

  @Test
  fun `pushed in app, back and up from the step return to the screen below the triage`() {
    val back = controllerWith(HomeKey, InsurancesKey, triage).apply { leaveTriageFor(step) }
    assertThat(back.entries.toList()).containsExactly(HomeKey, InsurancesKey, step)
    assertThat(back.popBackstack()).isTrue()
    assertThat(back.entries.toList()).containsExactly(HomeKey, InsurancesKey)

    val up = controllerWith(HomeKey, InsurancesKey, triage).apply { leaveTriageFor(step) }
    assertThat(up.navigateUp()).isTrue()
    assertThat(up.entries.toList()).containsExactly(HomeKey, InsurancesKey)
    assertThat(finished).isFalse()
  }

  @Test
  fun `a deep link in our own task lands on Home and the triage, and back and up from the step return to Home`() {
    val back = controllerWith(HomeKey, InsurancesKey).apply {
      navigateToExternalDeepLink(triage, hostedInForeignTask = false)
    }
    assertThat(back.entries.toList()).containsExactly(HomeKey, triage)
    back.leaveTriageFor(step)
    assertThat(back.entries.toList()).containsExactly(HomeKey, step)
    assertThat(back.popBackstack()).isTrue()
    assertThat(back.entries.toList()).containsExactly(HomeKey)

    val up = controllerWith(HomeKey).apply {
      navigateToExternalDeepLink(triage, hostedInForeignTask = false)
      leaveTriageFor(step)
    }
    assertThat(up.navigateUp()).isTrue()
    assertThat(up.entries.toList()).containsExactly(HomeKey)
    assertThat(finished).isFalse()
    assertThat(escapedTo).isNull()
  }

  @Test
  fun `a deep link in another app's task leaves the step alone, back finishes and up re-roots on Home`() {
    val back = controllerWith(HomeKey).apply { navigateToExternalDeepLink(triage, hostedInForeignTask = true) }
    assertThat(back.entries.toList()).containsExactly(triage)
    back.leaveTriageFor(step)
    assertThat(back.entries.toList()).containsExactly(step)
    assertThat(back.popBackstack()).isFalse()
    assertThat(finished).isTrue()
    assertThat(back.entries.toList()).containsExactly(step)

    finished = false
    val up = controllerWith(HomeKey).apply {
      navigateToExternalDeepLink(triage, hostedInForeignTask = true)
      leaveTriageFor(step)
    }
    assertThat(up.navigateUp()).isTrue()
    // Up re-roots on the step's own ancestry, which does not include the triage.
    assertThat(escapedTo).isEqualTo(listOf<HedvigNavKey>(HomeKey))
    assertThat(finished).isFalse()
  }
}
