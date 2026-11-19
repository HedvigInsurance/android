package com.hedvig.android.iosdata.bridge

import assertk.assertThat
import assertk.assertions.containsExactly
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import kotlin.test.Test
import platform.Foundation.NSError

class NativeSuspendOnceTest {
  @Test
  fun `a result that arrives after the caller cancelled is dropped for the cancellation`() {
    var deliverLateResult: () -> Unit = {}
    val slowCall: NativeSuspend<String> = { onResult, _, _ ->
      deliverLateResult = { onResult("late", Unit) }
      ({})
    }
    val delivered = mutableListOf<String>()

    val cancel = slowCall.deliveringOnce()(
      { _, unit ->
        delivered += "result"
        unit
      },
      { _, unit ->
        delivered += "error"
        unit
      },
      { _, unit ->
        delivered += "cancelled"
        unit
      },
    )
    cancel()
    deliverLateResult()

    assertThat(delivered).containsExactly("cancelled")
  }

  @Test
  fun `a call delivers one callback even when the library would deliver two`() {
    val errorThenCancellation: NativeSuspend<String> = { _, onError, onCancelled ->
      onError(NSError.errorWithDomain("KotlinException", 0, null), Unit)
      onCancelled(NSError.errorWithDomain("KotlinException", 0, null), Unit)
      ({})
    }
    val resultThenCancellation: NativeSuspend<String> = { onResult, _, onCancelled ->
      onResult("value", Unit)
      onCancelled(NSError.errorWithDomain("KotlinException", 0, null), Unit)
      ({})
    }
    val delivered = mutableListOf<String>()

    for (call in listOf(errorThenCancellation, resultThenCancellation)) {
      call.deliveringOnce()(
        { _, unit ->
          delivered += "result"
          unit
        },
        { _, unit ->
          delivered += "error"
          unit
        },
        { _, unit ->
          delivered += "cancelled"
          unit
        },
      )
    }

    assertThat(delivered).containsExactly("error", "result")
  }
}
