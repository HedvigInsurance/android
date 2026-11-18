package com.hedvig.android.molecule.test

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasMessage
import assertk.assertions.isEqualTo
import com.hedvig.android.molecule.public.MoleculePresenter
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FrameTestTest {
  /** [run] counts presenter runs, [count] the events handled across them. */
  private data class Counted(val run: Int, val count: Int)

  private val countingPresenter = MoleculePresenter<Unit, Counted> { lastState ->
    val run = remember { lastState.run + 1 }
    var count by remember { mutableIntStateOf(lastState.count) }
    CollectEvents { count++ }
    Counted(run, count)
  }

  @Test
  fun `the first state comes from the initial state without a frame`() = runTest {
    countingPresenter.testFrames(Counted(run = 0, count = 5)) {
      assertThat(latest).isEqualTo(Counted(run = 1, count = 5))
    }
  }

  @Test
  fun `events emitted together are all handled before the next frame produces one state`() = runTest {
    countingPresenter.testFrames(Counted(run = 0, count = 0)) {
      emit(Unit, Unit)
      runCurrent()
      assertThat(latest).isEqualTo(Counted(run = 1, count = 0))

      frame()
      assertThat(latest).isEqualTo(Counted(run = 1, count = 2))
      // The initial state first, as the state flow holds it before the presenter's first state.
      assertThat(observed).containsExactly(
        Counted(run = 0, count = 0),
        Counted(run = 1, count = 0),
        Counted(run = 1, count = 2),
      )
    }
  }

  @Test
  fun `the immediate harness recomposes between two events, so it cannot show them landing together`() = runTest {
    countingPresenter.test(Counted(run = 0, count = 0)) {
      assertThat(awaitItem()).isEqualTo(Counted(run = 1, count = 0))
      sendEvent(Unit)
      sendEvent(Unit)
      assertThat(awaitItem()).isEqualTo(Counted(run = 1, count = 1))
      assertThat(awaitItem()).isEqualTo(Counted(run = 1, count = 2))
    }
  }

  @Test
  fun `an effect's result is produced only by the next frame`() = runTest {
    val results = Channel<Int>(Channel.UNLIMITED)
    val presenter = MoleculePresenter<Unit, Int> { lastState ->
      var value by remember { mutableIntStateOf(lastState) }
      LaunchedEffect(Unit) {
        for (result in results) value = result
      }
      value
    }
    presenter.testFrames(0) {
      results.trySend(1)
      results.trySend(2)
      runCurrent()
      assertThat(latest).isEqualTo(0)

      frame()
      assertThat(latest).isEqualTo(2)
      assertThat(observed).containsExactly(0, 2)
    }
  }

  @Test
  fun `restart starts a new run from the last produced state`() = runTest {
    countingPresenter.testFrames(Counted(run = 0, count = 0)) {
      emit(Unit, Unit)
      frame()
      restart()
      assertThat(latest).isEqualTo(Counted(run = 2, count = 2))
      emit(Unit)
      frame()
      assertThat(latest).isEqualTo(Counted(run = 2, count = 3))
    }
  }

  @Test
  fun `an event emitted while stopped waits for the next run`() = runTest {
    countingPresenter.testFrames(Counted(run = 0, count = 0)) {
      stop()
      emit(Unit)
      frame()
      assertThat(latest).isEqualTo(Counted(run = 1, count = 0))

      start()
      frame()
      assertThat(latest).isEqualTo(Counted(run = 2, count = 1))
    }
  }

  @Test
  fun `a stop shorter than the timeout keeps the presenter running and handling events`() = runTest {
    countingPresenter.testFrames(Counted(run = 0, count = 0)) {
      stop(4.seconds)
      emit(Unit)
      frame()
      assertThat(latest).isEqualTo(Counted(run = 1, count = 1))

      start()
      assertThat(latest).isEqualTo(Counted(run = 1, count = 1))
    }
  }

  @Test
  fun `a stop as long as the timeout stops the presenter`() = runTest {
    countingPresenter.testFrames(Counted(run = 0, count = 0)) {
      stop(5.seconds)
      start()
      assertThat(latest).isEqualTo(Counted(run = 2, count = 0))
    }
  }

  @Test
  fun `a presenter that throws fails the test`() = runTest {
    val presenter = MoleculePresenter<Unit, Int> { lastState ->
      CollectEvents { error("presenter failed") }
      lastState
    }
    assertFailure {
      presenter.testFrames(0) {
        emit(Unit)
        frame()
      }
    }.hasMessage("presenter failed")
  }
}
