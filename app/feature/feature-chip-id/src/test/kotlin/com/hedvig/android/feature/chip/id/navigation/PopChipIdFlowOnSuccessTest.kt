package com.hedvig.android.feature.chip.id.navigation

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.compose.Backstack
import kotlinx.serialization.Serializable
import org.junit.Test

internal class PopChipIdFlowOnSuccessTest {
  private class TestBackstack(vararg initial: HedvigNavKey) : Backstack {
    override val entries: MutableList<HedvigNavKey> = initial.toMutableList()
  }

  @Test
  fun `single contract with a caller underneath pops back to the caller`() {
    // The select screen auto-advanced and popped ChipIdKey, so only AddChipIdKey remains.
    val backstack = TestBackstack(CallerKey, AddChipIdKey("c1"))
    var wentHome = false

    backstack.popChipIdFlowOnSuccess { wentHome = true }

    assertThat(backstack.entries).containsExactly(CallerKey)
    assertThat(wentHome).isFalse()
  }

  @Test
  fun `multiple contracts pop the whole flow back to the caller`() {
    val backstack = TestBackstack(CallerKey, ChipIdKey(), AddChipIdKey("c1"))
    var wentHome = false

    backstack.popChipIdFlowOnSuccess { wentHome = true }

    assertThat(backstack.entries).containsExactly(CallerKey)
    assertThat(wentHome).isFalse()
  }

  @Test
  fun `lone deep link with no caller falls back to going home`() {
    val backstack = TestBackstack(AddChipIdKey("c1"))
    var wentHome = false

    backstack.popChipIdFlowOnSuccess { wentHome = true }

    assertThat(wentHome).isTrue()
  }

  @Test
  fun `multiple callers are preserved, only the flow is popped`() {
    val backstack = TestBackstack(CallerKey, OtherCallerKey, AddChipIdKey("c1"))
    var wentHome = false

    backstack.popChipIdFlowOnSuccess { wentHome = true }

    assertThat(backstack.entries).containsExactly(CallerKey, OtherCallerKey)
    assertThat(backstack.entries.last()).isEqualTo(OtherCallerKey)
    assertThat(wentHome).isFalse()
  }
}

@Serializable
private data object CallerKey : HedvigNavKey

@Serializable
private data object OtherCallerKey : HedvigNavKey
