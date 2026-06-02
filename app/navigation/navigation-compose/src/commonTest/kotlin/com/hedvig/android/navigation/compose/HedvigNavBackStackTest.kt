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

class HedvigNavBackStackTest {
  @Test fun popBackStack_atRoot_returnsFalseAndKeepsRoot() {
    val stack = mutableListOf<HedvigNavKey>(A)
    assertFalse(stack.popBackStack())
    assertEquals(listOf<HedvigNavKey>(A), stack)
  }

  @Test fun popBackStack_popsTop() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"))
    assertTrue(stack.popBackStack())
    assertEquals(listOf<HedvigNavKey>(A), stack)
  }

  @Test fun popUpTo_exclusive_keepsTarget() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"), C)
    stack.popUpTo<B>(inclusive = false)
    assertEquals(listOf<HedvigNavKey>(A, B("x")), stack)
  }

  @Test fun popUpTo_inclusive_removesTarget() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"), C)
    stack.popUpTo<B>(inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A), stack)
  }

  @Test fun popUpTo_absentTarget_isNoOp() {
    val stack = mutableListOf<HedvigNavKey>(A, C)
    stack.popUpTo<B>(inclusive = true)
    assertEquals(listOf<HedvigNavKey>(A, C), stack)
  }

  @Test fun navigateAndPopUpTo_popsThenPushes() {
    val stack = mutableListOf<HedvigNavKey>(A, B("x"), C)
    stack.navigateAndPopUpTo<A>(B("y"), inclusive = false)
    assertEquals(listOf<HedvigNavKey>(A, B("y")), stack)
  }

  @Test fun findLastOrNull_returnsMostRecentOfType() {
    val stack = mutableListOf<HedvigNavKey>(B("first"), A, B("second"))
    assertEquals(B("second"), stack.findLastOrNull<B>())
    assertNull(mutableListOf<HedvigNavKey>(A).findLastOrNull<B>())
  }

  @Test fun removeAllOf_removesEveryEntryOfType() {
    val stack = mutableListOf<HedvigNavKey>(B("1"), A, B("2"), C)
    stack.removeAllOf<B>()
    assertEquals(listOf<HedvigNavKey>(A, C), stack)
  }
}
