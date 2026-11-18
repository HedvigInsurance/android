package com.hedvig.android.molecule.public

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.doesNotContain
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNull
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NavigationRequestTest {
  private data class Screen(val text: String, val next: NavigationRequest<String>?)

  @Test
  fun `a request is taken by exactly one call`() {
    val request = NavigationRequest.of("step")
    assertThat(request.isPending).isTrue()

    assertThat(request.tryConsume()).isTrue()
    assertThat(request.tryConsume()).isFalse()
    assertThat(request.isPending).isFalse()
    assertThat(request.target).isEqualTo("step")
  }

  @Test
  fun `consumers on several threads take a request once between them`() {
    repeat(200) {
      val request = NavigationRequest.of("step")
      val start = CountDownLatch(1)
      val taken = AtomicInteger(0)
      val threads = List(4) {
        thread {
          start.await()
          if (request.tryConsume()) taken.incrementAndGet()
        }
      }
      start.countDown()
      threads.forEach { it.join() }
      assertThat(taken.get()).isEqualTo(1)
    }
  }

  @Test
  fun `a copy of the state that carries a request shares whether it was taken`() {
    val offered = Screen("a", NavigationRequest.of("step"))
    val edited = offered.copy(text = "b")
    assertThat(edited.next!!.tryConsume()).isTrue()
    assertThat(offered.next!!.isPending).isFalse()
  }

  @Test
  fun `a new request for an equal target is a different value, so a state flow emits it`() = runTest {
    val first = NavigationRequest.of("step")
    val second = NavigationRequest.of("step")
    assertThat(second).isNotEqualTo(first)

    val states = MutableStateFlow(Screen("a", first))
    val received = mutableListOf<Screen>()
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { states.take(2).toList(received) }
    states.value = Screen("a", second)
    assertThat(received.map { it.next }).containsExactly(first, second)
  }

  @Test
  fun `a mapped request is the same request with another target`() {
    val request = NavigationRequest.of("https://example.com/c.pdf")
    val mapped = request.map { it.length }
    assertThat(mapped.target).isEqualTo(25)
    assertThat(mapped).isEqualTo(request as NavigationRequest<Any>)

    assertThat(mapped.tryConsume()).isTrue()
    assertThat(request.isPending).isFalse()
    assertThat(request.tryConsume()).isFalse()
  }

  @Test
  fun `the text of a request leaves out its target`() {
    val request = NavigationRequest.of("member@hedvig.com")
    assertThat(request.toString()).doesNotContain("member@hedvig.com")
  }

  @Test
  fun `restoring a tap offer drops it while pending and keeps it once taken`() {
    val pending = NavigationRequest.of("step")
    assertThat(pending.restoredAsTapOffer()).isNull()
    // A screen still showing the state from before the restart cannot perform the dropped offer.
    assertThat(pending.tryConsume()).isFalse()

    val taken = NavigationRequest.of("step").also { it.tryConsume() }
    assertThat(taken.restoredAsTapOffer()).isSameInstanceAs(taken)
    assertThat(taken.restoredAsTapOffer()!!.tryConsume()).isFalse()

    assertThat((null as NavigationRequest<String>?).restoredAsTapOffer()).isNull()
  }

  @Test
  fun `restoring the outcome of a write keeps it as it was, pending or taken`() {
    val pending = NavigationRequest.of("certificate")
    assertThat(pending.restoredAsWriteOutcome()).isSameInstanceAs(pending)
    assertThat(pending.isPending).isTrue()

    val taken = NavigationRequest.of("certificate").also { it.tryConsume() }
    assertThat(taken.restoredAsWriteOutcome()).isSameInstanceAs(taken)
    assertThat(taken.isPending).isFalse()

    assertThat((null as NavigationRequest<String>?).restoredAsWriteOutcome()).isNull()
  }

  @Test
  fun `restoring a redirect keeps it while pending and drops it once taken`() {
    val pending = NavigationRequest.of("step")
    assertThat(pending.restoredAsRedirect()).isSameInstanceAs(pending)
    assertThat(pending.isPending).isTrue()

    val taken = NavigationRequest.of("step").also { it.tryConsume() }
    assertThat(taken.restoredAsRedirect()).isNull()

    assertThat((null as NavigationRequest<String>?).restoredAsRedirect()).isNull()
  }
}
