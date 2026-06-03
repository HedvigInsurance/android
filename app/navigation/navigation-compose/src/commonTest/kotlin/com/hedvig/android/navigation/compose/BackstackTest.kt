package com.hedvig.android.navigation.compose

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.Serializable

@Serializable private data object A : HedvigNavKey

@Serializable private data class B(val id: String) : HedvigNavKey

@Serializable private data object C : HedvigNavKey

private class TestBackstack(override val entries: MutableList<HedvigNavKey>) : Backstack

private fun backstackOf(vararg keys: HedvigNavKey): TestBackstack = TestBackstack(mutableListOf(*keys))

class BackstackTest {
  @Test fun popBackstack_atRoot_returnsFalseAndKeepsRoot() {
    val stack = backstackOf(A)
    assertFalse(stack.popBackstack())
    assertEquals(listOf<HedvigNavKey>(A), stack.entries)
  }

  @Test fun popBackstack_popsTop() {
    val stack = backstackOf(A, B("x"))
    assertTrue(stack.popBackstack())
    assertEquals(listOf<HedvigNavKey>(A), stack.entries)
  }

  @Test fun popUpTo_exclusive_keepsTarget() {
    val stack = backstackOf(A, B("x"), C)
    stack.popUpTo<B>(inclusive = false)
    assertEquals(listOf<HedvigNavKey>(A, B("x")), stack.entries)
  }

  @Test fun popUpTo_inclusive_removesTarget() {
    val stack = backstackOf(A, B("x"), C)
    stack.popUpTo<B>(inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A), stack.entries)
  }

  @Test fun popUpTo_absentTarget_isNoOp() {
    val stack = backstackOf(A, C)
    stack.popUpTo<B>(inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A, C), stack.entries)
  }

  @Test fun navigateAndPopUpTo_popsThenPushes() {
    val stack = backstackOf(A, B("x"), C)
    stack.navigateAndPopUpTo<A>(B("y"), inclusive = false)
    assertEquals(listOf<HedvigNavKey>(A, B("y")), stack.entries)
  }

  @Test fun findLastOrNull_returnsMostRecentOfType() {
    val stack = backstackOf(B("first"), A, B("second"))
    assertEquals(B("second"), stack.findLastOrNull<B>())
    assertNull(backstackOf(A).findLastOrNull<B>())
  }

  @Test fun removeAllOf_removesEveryEntryOfType() {
    val stack = backstackOf(B("1"), A, B("2"), C)
    stack.removeAllOf<B>()
    assertEquals(listOf<HedvigNavKey>(A, C), stack.entries)
  }
}
