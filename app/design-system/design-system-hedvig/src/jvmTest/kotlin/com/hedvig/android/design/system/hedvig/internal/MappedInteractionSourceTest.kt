package com.hedvig.android.design.system.hedvig.internal

import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.geometry.Offset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class MappedInteractionSourceTest {
  private val underlying = MutableInteractionSource()
  private val source = MappedInteractionSource(underlying, delta = { Offset(10f, 20f) })

  @Test
  fun `a collection restarted after a missed hover exit first receives that exit`() = runTest {
    val enter = HoverInteraction.Enter()
    val first = collect()
    underlying.emit(enter)
    first.job.cancel()
    underlying.tryEmit(HoverInteraction.Exit(enter))

    val second = collect()

    assertEquals(listOf<Interaction>(enter), first.received)
    assertSame(enter, assertIs<HoverInteraction.Exit>(second.received.single()).enter)
  }

  @Test
  fun `a collection restarted after a missed unfocus first receives that unfocus`() = runTest {
    val focus = FocusInteraction.Focus()
    val first = collect()
    underlying.emit(focus)
    first.job.cancel()
    underlying.tryEmit(FocusInteraction.Unfocus(focus))

    val second = collect()

    assertSame(focus, assertIs<FocusInteraction.Unfocus>(second.received.single()).focus)
  }

  @Test
  fun `a hover and focus that ended while collected are not ended again`() = runTest {
    val enter = HoverInteraction.Enter()
    val focus = FocusInteraction.Focus()
    val first = collect()
    underlying.emit(enter)
    underlying.emit(HoverInteraction.Exit(enter))
    underlying.emit(focus)
    underlying.emit(FocusInteraction.Unfocus(focus))
    first.job.cancel()

    val second = collect()

    assertEquals(emptyList(), second.received)
  }

  @Test
  fun `a press in flight across a restarted collection still ends on its mapped press`() = runTest {
    val press = PressInteraction.Press(Offset(1f, 2f))
    val first = collect()
    underlying.emit(press)
    first.job.cancel()

    val second = collect()
    underlying.emit(PressInteraction.Release(press))

    val mappedPress = assertIs<PressInteraction.Press>(first.received.single())
    assertEquals(Offset(11f, 22f), mappedPress.pressPosition)
    val release = assertIs<PressInteraction.Release>(second.received.single())
    assertSame(mappedPress, release.press)
  }

  private class Collection(val job: Job, val received: List<Interaction>)

  private fun TestScope.collect(): Collection {
    val received = mutableListOf<Interaction>()
    val job = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
      source.interactions.collect { received += it }
    }
    return Collection(job, received)
  }
}
