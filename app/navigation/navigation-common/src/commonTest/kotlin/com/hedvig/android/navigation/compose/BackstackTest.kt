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

  @Test fun popUpTo_inclusiveOfBase_keepsBaseInsteadOfEmptying() {
    val stack = backstackOf(A, B("x"))
    stack.popUpTo<A>(inclusive = true)
    // Targets popUpToIndex(-1); the default keeps the base (and :app finishes the app). Never empty.
    assertEquals(listOf<HedvigNavKey>(A), stack.entries)
  }

  @Test fun popUpToIndex_keepsEntryAtIndexAsTop() {
    val stack = backstackOf(A, B("x"), C)
    stack.popUpToIndex(1)
    assertEquals(listOf<HedvigNavKey>(A, B("x")), stack.entries)
  }

  @Test fun popUpToIndex_negative_keepsBaseInsteadOfEmptying() {
    val stack = backstackOf(A, B("x"))
    stack.popUpToIndex(-1)
    // Clearing the base would empty the stack; the default keeps the base (and :app finishes the app).
    assertEquals(listOf<HedvigNavKey>(A), stack.entries)
  }

  @Test fun popUpToIndex_atOrBeyondTop_isNoOp() {
    val stack = backstackOf(A, B("x"))
    stack.popUpToIndex(5)
    assertEquals(listOf<HedvigNavKey>(A, B("x")), stack.entries)
  }

  @Test fun navigateAndPopUpTo_exclusive_popsThenPushes() {
    val stack = backstackOf(A, B("x"), C)
    stack.navigateAndPopUpTo<A>(B("y"), inclusive = false)
    assertEquals(listOf<HedvigNavKey>(A, B("y")), stack.entries)
  }

  @Test fun navigateAndPopUpTo_inclusive_replacesTarget() {
    val stack = backstackOf(A, B("x"), C)
    stack.navigateAndPopUpTo<B>(C, inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A, C), stack.entries)
  }

  @Test fun navigateAndPopUpTo_inclusiveOfBase_emptiesThenRepopulates() {
    val stack = backstackOf(A, B("x"))
    stack.navigateAndPopUpTo<A>(C, inclusive = true)
    // Unlike popUpTo, this clears the whole stack and lands on the pushed key — never finishes.
    assertEquals(listOf<HedvigNavKey>(C), stack.entries)
  }

  @Test fun navigateAndPopUpTo_absentTarget_justAppends() {
    val stack = backstackOf(A, B("x"))
    stack.navigateAndPopUpTo<C>(B("y"), inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A, B("x"), B("y")), stack.entries)
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

  @Test fun navigateUp_default_behavesLikePopBackstack() {
    val stack = backstackOf(A, B("x"))
    assertTrue(stack.navigateUp())
    assertEquals(listOf<HedvigNavKey>(A), stack.entries)
  }

  @Test fun navigateUp_atRoot_returnsFalse() {
    val stack = backstackOf(A)
    assertFalse(stack.navigateUp())
    assertEquals(listOf<HedvigNavKey>(A), stack.entries)
  }
}
