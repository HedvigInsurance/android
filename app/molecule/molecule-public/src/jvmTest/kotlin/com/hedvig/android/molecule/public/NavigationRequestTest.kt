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
  fun `the text of a request leaves out its target`() {
    val request = NavigationRequest.of("member@hedvig.com")
    assertThat(request.toString()).doesNotContain("member@hedvig.com")
  }

  @Test
  fun `restoring an offer drops it while pending and keeps it once taken`() {
    val pending = NavigationRequest.of("step")
    assertThat(pending.restoredAsOffer()).isNull()

    val taken = NavigationRequest.of("step").also { it.tryConsume() }
    assertThat(taken.restoredAsOffer()).isSameInstanceAs(taken)
    assertThat(taken.restoredAsOffer()!!.tryConsume()).isFalse()

    assertThat((null as NavigationRequest<String>?).restoredAsOffer()).isNull()
  }
}
