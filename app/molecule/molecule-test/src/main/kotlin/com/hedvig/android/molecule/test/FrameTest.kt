package com.hedvig.android.molecule.test

import androidx.compose.runtime.BroadcastFrameClock
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/**
 * Runs this presenter in a real [MoleculeViewModel] whose frames the test sends, collected the way a screen collects
 * it. Events and effect results are handled as soon as the test lets coroutines run, and a state is produced only by
 * [FrameTestContext.frame], so two events handled between two frames are seen by the presenter exactly as on a device.
 *
 * Call it from `runTest`: the ViewModel runs on that test's scheduler. The ViewModel stops its presenter 5 seconds
 * after the last collection ends, as in production, so [FrameTestContext.stop] moves virtual time on by 5 seconds.
 */
suspend fun <Event, State> MoleculePresenter<Event, State>.testFrames(
  initialState: State,
  block: suspend FrameTestContext<Event, State>.() -> Unit,
) {
  val scheduler = checkNotNull(currentCoroutineContext()[TestCoroutineScheduler]) {
    "testFrames needs the scheduler of runTest"
  }
  val failures = mutableListOf<Throwable>()
  val frameClock = BroadcastFrameClock()
  val scope = CoroutineScope(
    StandardTestDispatcher(scheduler) + frameClock + SupervisorJob() +
      CoroutineExceptionHandler { _, throwable -> failures += throwable },
  )
  val viewModel = object : MoleculeViewModel<Event, State>(
    initialState = initialState,
    presenter = this,
    coroutineScope = scope,
  ) {}
  val context = FrameTestContextImpl(viewModel, scheduler, frameClock, scope)
  try {
    context.start()
    context.block()
    context.runCurrent()
  } finally {
    scope.cancel()
  }
  failures.firstOrNull()?.let { throw it }
}

interface FrameTestContext<Event, State> {
  /** Queues [events] in order. Nothing runs until [runCurrent] or [frame]. */
  fun emit(vararg events: Event)

  /** Runs every coroutine that is ready, such as the event collector and effects, without producing a frame. */
  fun runCurrent()

  /** Runs what is ready, sends one frame, and runs what is ready again, so the collector receives the new state. */
  fun frame()

  /** The value of the ViewModel's state flow. */
  val latest: State

  /** Every state the collector received, in order. A state replaced before the collector ran is not in it. */
  val observed: List<State>

  /** Ends the collection and lets the presenter stop, as when a screen stops collecting for more than 5 seconds. */
  fun stop()

  /** Starts a collection, which starts a new presenter run from the last produced state if it was stopped. */
  fun start()

  /** [stop] then [start]. */
  fun restart()
}

private class FrameTestContextImpl<Event, State>(
  private val viewModel: MoleculeViewModel<Event, State>,
  private val scheduler: TestCoroutineScheduler,
  private val frameClock: BroadcastFrameClock,
  private val scope: CoroutineScope,
) : FrameTestContext<Event, State> {
  private val observedStates = mutableListOf<State>()
  private var collection: Job? = null
  private var frameTimeNanos = 0L

  override fun emit(vararg events: Event) {
    events.forEach(viewModel::emit)
  }

  override fun runCurrent() {
    scheduler.runCurrent()
  }

  override fun frame() {
    scheduler.runCurrent()
    frameClock.sendFrame(frameTimeNanos)
    frameTimeNanos += FRAME_NANOS
    scheduler.runCurrent()
  }

  override val latest: State
    get() = viewModel.uiState.value

  override val observed: List<State>
    get() = observedStates.toList()

  override fun stop() {
    collection?.cancel()
    collection = null
    scheduler.runCurrent()
    scheduler.advanceTimeBy(STOP_TIMEOUT)
    scheduler.runCurrent()
  }

  override fun start() {
    check(collection == null) { "Already collecting" }
    collection = scope.launch { viewModel.uiState.collect { observedStates += it } }
    scheduler.runCurrent()
  }

  override fun restart() {
    stop()
    start()
  }

  private companion object {
    const val FRAME_NANOS = 16_000_000L
    val STOP_TIMEOUT = 5.seconds
  }
}
