package com.hedvig.android.molecule.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.hedvig.android.molecule.public.AttemptStatus
import com.hedvig.android.molecule.public.Attempts
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.rememberAttempts
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Test

/**
 * [rememberAttempts] against the two hand-written runners it replaces: an effect keyed on the request and reset to
 * null when it ends, and an effect keyed on a counter bumped by every start. Each bug is shown to happen with the hand
 * written runner and not with the block.
 */
class AttemptsTest {
  private fun interface AttemptsFactory {
    @Composable
    fun create(restoredWhileRunning: Boolean, terminal: Boolean, run: suspend (String) -> Boolean): Attempts<String>
  }

  private val block = AttemptsFactory { restoredWhileRunning, terminal, run ->
    rememberAttempts(restoredWhileRunning, terminal, run)
  }
  private val keyedOnRequest = AttemptsFactory { _, _, run -> rememberKeyedOnRequest(run) }
  private val cancelAndRelaunch = AttemptsFactory { _, _, run -> rememberCancelAndRelaunch(run) }

  /** Each run waits for its result on [results]. */
  private class Runs {
    val results = Channel<Boolean>(Channel.UNLIMITED)
    val started = mutableListOf<String>()
    val completed = mutableListOf<String>()
    val cancelled = mutableListOf<String>()

    suspend fun run(request: String): Boolean {
      started += request
      try {
        return results.receive().also { completed += request }
      } catch (e: CancellationException) {
        cancelled += request
        throw e
      }
    }
  }

  /** Starts an attempt for every event and shows the status. */
  private class StartingPresenter(
    private val factory: AttemptsFactory,
    private val runs: Runs,
    private val terminal: Boolean = false,
  ) : MoleculePresenter<String, AttemptStatus> {
    @Composable
    override fun MoleculePresenterScope<String>.present(lastState: AttemptStatus): AttemptStatus {
      val attempts = factory.create(lastState == AttemptStatus.Running, terminal, runs::run)
      CollectEvents { attempts.start(it) }
      return attempts.status
    }
  }

  private suspend fun retryInTheFrameOfTheFailure(factory: AttemptsFactory): Runs {
    val runs = Runs()
    StartingPresenter(factory, runs).testFrames(AttemptStatus.Idle) {
      emit("same")
      frame()
      runs.results.trySend(false)
      runCurrent()
      emit("same")
      frame()
    }
    return runs
  }

  @Test
  fun `block, a retry equal to a failed request, handled before the next frame, runs`() = runTest {
    assertThat(retryInTheFrameOfTheFailure(block).started).containsExactly("same", "same")
  }

  @Test
  fun `keyed on the request, a retry equal to a failed request, handled before the next frame, is lost`() = runTest {
    assertThat(retryInTheFrameOfTheFailure(keyedOnRequest).started).containsExactly("same")
  }

  private suspend fun startWhileRunning(factory: AttemptsFactory): Runs {
    val runs = Runs()
    StartingPresenter(factory, runs).testFrames(AttemptStatus.Idle) {
      emit("first")
      frame()
      emit("second")
      frame()
      runs.results.trySend(true)
      frame()
    }
    return runs
  }

  @Test
  fun `block, a start while one is running is refused and does not cancel it`() = runTest {
    val runs = startWhileRunning(block)
    assertThat(runs.started).containsExactly("first")
    assertThat(runs.completed).containsExactly("first")
    assertThat(runs.cancelled).isEmpty()
  }

  @Test
  fun `cancel and relaunch, a start while one is running cancels it and runs again`() = runTest {
    val runs = startWhileRunning(cancelAndRelaunch)
    assertThat(runs.started).containsExactly("first", "second")
    assertThat(runs.cancelled).containsExactly("first")
  }

  @Test
  fun `block, two starts in one frame run once`() = runTest {
    val runs = Runs()
    StartingPresenter(block, runs).testFrames(AttemptStatus.Idle) {
      emit("first", "second")
      frame()
      assertThat(latest).isEqualTo(AttemptStatus.Running)
    }
    assertThat(runs.started).containsExactly("first")
  }

  private suspend fun startAfterSuccess(factory: AttemptsFactory, terminal: Boolean): Runs {
    val runs = Runs()
    StartingPresenter(factory, runs, terminal).testFrames(AttemptStatus.Idle) {
      emit("first")
      frame()
      runs.results.trySend(true)
      frame()
      emit("second")
      frame()
    }
    return runs
  }

  @Test
  fun `block, a terminal attempt that succeeded refuses every later start`() = runTest {
    assertThat(startAfterSuccess(block, terminal = true).started).containsExactly("first")
  }

  @Test
  fun `block, an attempt that is not terminal starts again after a success`() = runTest {
    assertThat(startAfterSuccess(block, terminal = false).started).containsExactly("first", "second")
  }

  @Test
  fun `block, a terminal attempt that failed can start again`() = runTest {
    val runs = Runs()
    StartingPresenter(block, runs, terminal = true).testFrames(AttemptStatus.Idle) {
      emit("first")
      frame()
      runs.results.trySend(false)
      frame()
      emit("second")
      frame()
    }
    assertThat(runs.started).containsExactly("first", "second")
  }

  @Test
  fun `block, a restart ends the run in flight and the new run shows it interrupted until the next start`() = runTest {
    val runs = Runs()
    StartingPresenter(block, runs).testFrames(AttemptStatus.Idle) {
      emit("first")
      frame()
      assertThat(latest).isEqualTo(AttemptStatus.Running)
      restart()
      frame()
      assertThat(latest).isEqualTo(AttemptStatus.Interrupted)
      assertThat(runs.cancelled).containsExactly("first")

      emit("second")
      frame()
      assertThat(latest).isEqualTo(AttemptStatus.Running)
      assertThat(runs.started).containsExactly("first", "second")
    }
  }

  @Test
  fun `block, a restart from a state without a run in flight starts idle`() = runTest {
    val runs = Runs()
    StartingPresenter(block, runs).testFrames(AttemptStatus.Idle) {
      emit("first")
      frame()
      runs.results.trySend(false)
      frame()
      restart()
      frame()
      assertThat(latest).isEqualTo(AttemptStatus.Idle)
    }
  }

  /**
   * Starts an attempt for every event that passes the guard. The work that must come before each request, such as a
   * local write, is recorded in [preparedFor]; with [prepareInRun] it is done inside the run, otherwise in a coroutine
   * launched by the event handler, which calls `start` after it.
   */
  private class PreparingPresenter(
    private val runs: Runs,
    private val prepareInRun: Boolean,
    private val preparedFor: MutableList<String>,
  ) : MoleculePresenter<String, AttemptStatus> {
    @Composable
    override fun MoleculePresenterScope<String>.present(lastState: AttemptStatus): AttemptStatus {
      val scope = rememberCoroutineScope()
      val attempts = rememberAttempts<String>(restoredWhileRunning = false) { request ->
        if (prepareInRun) prepare(request)
        runs.run(request)
      }
      CollectEvents { request ->
        if (attempts.status == AttemptStatus.Running) return@CollectEvents
        if (prepareInRun) {
          attempts.start(request)
        } else {
          scope.launch {
            prepare(request)
            attempts.start(request)
          }
        }
      }
      return attempts.status
    }

    private suspend fun prepare(request: String) {
      yield()
      preparedFor += request
    }
  }

  private suspend fun twoEventsInOneFrame(prepareInRun: Boolean): List<String> {
    val runs = Runs()
    val preparedFor = mutableListOf<String>()
    PreparingPresenter(runs, prepareInRun, preparedFor).testFrames(AttemptStatus.Idle) {
      emit("first", "second")
      frame()
      frame()
      assertThat(runs.started).containsExactly("first")
    }
    return preparedFor
  }

  @Test
  fun `block, work before the request done inside run happens once for two events in one frame`() = runTest {
    assertThat(twoEventsInOneFrame(prepareInRun = true)).containsExactly("first")
  }

  @Test
  fun `work before the request launched ahead of start happens for both of two events in one frame`() = runTest {
    assertThat(twoEventsInOneFrame(prepareInRun = false)).containsExactly("first", "second")
  }
}

/** The runner the travel certificate steps used before their fix round: keyed on the request, reset to null. */
@Composable
private fun rememberKeyedOnRequest(run: suspend (String) -> Boolean): Attempts<String> {
  val attempts = remember {
    object : Attempts<String> {
      var request: String? by mutableStateOf(null)
      var isRunning: Boolean by mutableStateOf(false)
      override val status: AttemptStatus
        get() = if (isRunning) AttemptStatus.Running else AttemptStatus.Idle

      override fun start(request: String): Boolean {
        if (isRunning) return false
        this.request = request
        isRunning = true
        return true
      }
    }
  }
  val currentRun by rememberUpdatedState(run)
  LaunchedEffect(attempts.request) {
    val request = attempts.request ?: return@LaunchedEffect
    currentRun(request)
    attempts.isRunning = false
    attempts.request = null
  }
  return attempts
}

/** The runner manual charge used before its fix round: a counter bumped by every start, with no refusal. */
@Composable
private fun rememberCancelAndRelaunch(run: suspend (String) -> Boolean): Attempts<String> {
  val attempts = remember {
    object : Attempts<String> {
      var number by mutableIntStateOf(0)
      var request: String? = null
      var isRunning: Boolean by mutableStateOf(false)
      override val status: AttemptStatus
        get() = if (isRunning) AttemptStatus.Running else AttemptStatus.Idle

      override fun start(request: String): Boolean {
        this.request = request
        isRunning = true
        number++
        return true
      }
    }
  }
  val currentRun by rememberUpdatedState(run)
  LaunchedEffect(attempts.number) {
    val request = attempts.request ?: return@LaunchedEffect
    try {
      currentRun(request)
    } finally {
      attempts.isRunning = false
    }
  }
  return attempts
}
