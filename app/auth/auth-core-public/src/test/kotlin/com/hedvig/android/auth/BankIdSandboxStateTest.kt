package com.hedvig.android.auth

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.auth.di.buildConstants
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.TestPreferencesDataStore
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BankIdSandboxStateTest {
  @get:Rule
  val testFolder = TemporaryFolder()

  @Test
  fun `a non-production build starts with the sandbox off and can turn it on and off`() = runTest {
    val state = sandboxState(TestPreferencesDataStore(testFolder.newFolder(), backgroundScope), isProduction = false)
    runCurrent()
    assertThat(state.isAvailable).isTrue()
    assertThat(state.headers()).isEmpty()

    state.setEnabled(true)
    runCurrent()
    assertThat(state.isEnabled.value).isTrue()
    assertThat(state.headers()).isEqualTo(mapOf("Hedvig-Bankid-Sandbox" to "true"))

    state.setEnabled(false)
    runCurrent()
    assertThat(state.isEnabled.value).isFalse()
    assertThat(state.headers()).isEmpty()
  }

  @Test
  fun `a production build cannot turn the sandbox on`() = runTest {
    val state = sandboxState(TestPreferencesDataStore(testFolder.newFolder(), backgroundScope), isProduction = true)

    state.setEnabled(true)
    runCurrent()

    assertThat(state.isAvailable).isFalse()
    assertThat(state.isEnabled.value).isFalse()
    assertThat(state.headers()).isEmpty()
  }

  @Test
  fun `a production build ignores a sandbox value stored by a staging build`() = runTest {
    val dataStore = TestPreferencesDataStore(testFolder.newFolder(), backgroundScope)
    sandboxState(dataStore, isProduction = false).setEnabled(true)

    val productionState = sandboxState(dataStore, isProduction = true)
    runCurrent()

    assertThat(productionState.isEnabled.value).isFalse()
    assertThat(productionState.headers()).isEmpty()
  }

  private fun TestScope.sandboxState(dataStore: TestPreferencesDataStore, isProduction: Boolean) = BankIdSandboxState(
    dataStore,
    buildConstants(isProduction = isProduction),
    ApplicationScope(backgroundScope),
  )
}
