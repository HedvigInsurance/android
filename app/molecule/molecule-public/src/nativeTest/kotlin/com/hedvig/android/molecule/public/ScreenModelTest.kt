@file:OptIn(NativeRuntimeApi::class)

package com.hedvig.android.molecule.public

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import kotlin.concurrent.AtomicInt
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi
import kotlin.reflect.KClass
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import platform.Foundation.NSDate
import platform.Foundation.NSLock
import platform.Foundation.NSRunLoop
import platform.Foundation.NSThread
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.runUntilDate

/**
 * Collects [ScreenModel.states] the way Swift does: through the native flow's callbacks, on the test binary's main
 * thread, driving the main run loop ([pumpMainLoop], [pumpMainLoopUntil]) so `Dispatchers.Main` drains.
 *
 * `DisplayLinkClock` produces no frame in the test process, so the fake presenter composes once per start and never
 * recomposes. [FakeViewModel.presenterStarts] counts those starts.
 */
class ScreenModelTest {
  private val logs = CapturingLogger()

  @BeforeTest
  fun installLogger() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstallLogger() {
    LogcatLogger.uninstall()
  }

  @Test
  fun `currentState is the mapped ViewModel state and reading it does not start the presenter`() {
    val starts = AtomicInt(0)
    val model = FakeScreenModel(starts)

    val state = model.currentState
    pumpMainLoop(1.seconds)

    assertThat(state).isEqualTo("screen:initial")
    assertThat(starts.value).isEqualTo(0)
    model.close()
  }

  @Test
  fun `states delivers the mapped state on the main thread and starts the presenter`() {
    val starts = AtomicInt(0)
    val model = FakeScreenModel(starts)
    val collection = Collection(model)

    pumpMainLoopUntil { collection.items.isNotEmpty() && starts.value == 1 }

    assertThat(collection.items).containsExactly("screen:initial")
    assertThat(collection.onMainThread).containsExactly(true)
    assertThat(starts.value).isEqualTo(1)
    model.close()
  }

  @Test
  fun `the first item arrives after the collection starts and repeats currentState`() {
    val model = FakeScreenModel()
    val seeded = model.currentState

    val collection = Collection(model)
    assertThat(collection.items).isEmpty()
    pumpMainLoopUntil { collection.items.isNotEmpty() }

    assertThat(collection.items.first()).isEqualTo(seeded)
    model.close()
  }

  @Test
  fun `no item arrives after cancel returns`() {
    val model = FakeScreenModel()
    val cancelled = Collection(model)
    val control = Collection(model)

    cancelled.cancel()
    pumpMainLoop(1.seconds)

    assertThat(control.items).containsExactly("screen:initial")
    assertThat(cancelled.items).isEmpty()
    assertThat(cancelled.cancellations).isEqualTo(1)
    model.close()
  }

  @Test
  fun `no item arrives after close returns and every collection is cancelled`() {
    val model = FakeScreenModel()
    val first = Collection(model)
    val second = Collection(model)

    model.close()
    pumpMainLoop(1.seconds)

    assertThat(first.items).isEmpty()
    assertThat(second.items).isEmpty()
    assertThat(first.cancellations).isEqualTo(1)
    assertThat(second.cancellations).isEqualTo(1)
    assertThat(first.completions).isEqualTo(0)
  }

  @Test
  fun `a collection started after close is cancelled at once and never starts the presenter`() {
    val starts = AtomicInt(0)
    val model = FakeScreenModel(starts)
    model.close()

    val collection = Collection(model)
    pumpMainLoop(1.seconds)

    assertThat(collection.items).isEmpty()
    assertThat(collection.cancellations).isEqualTo(1)
    assertThat(starts.value).isEqualTo(0)
  }

  @Test
  fun `close clears the ViewModel once and keeps the last state`() {
    val model = FakeScreenModel()
    Collection(model)
    pumpMainLoop(1.seconds)

    model.close()
    model.close()

    assertThat(logs.messages().count { it == "FakeViewModel cleared" }).isEqualTo(1)
    assertThat(model.currentState).isEqualTo("screen:initial")
  }

  @Test
  fun `events beyond the buffer are dropped and logged without throwing`() {
    val model = FakeScreenModel()

    repeat(25) { model.ping() }

    val dropped = logs.entries().count { (priority, message) ->
      priority == LogPriority.WARN && message.startsWith("FakeViewModel dropped an event")
    }
    assertThat(dropped).isEqualTo(5)
    model.close()
  }

  @Test
  fun `events after close are ignored`() {
    val model = FakeScreenModel()
    model.close()

    repeat(25) { model.ping() }

    assertThat(logs.messages().filter { "dropped an event" in it }).isEmpty()
  }

  @Test
  fun `a collection whose cancel is never called keeps the presenter running until close`() {
    val starts = AtomicInt(0)
    val model = FakeScreenModel(starts)
    var droppedItems = 0
    model.states({ _, next, _ ->
      droppedItems++
      next()
    }, { _, _ -> }, { _, _ -> })
    pumpMainLoopUntil { droppedItems == 1 }

    GC.collect()
    pumpMainLoop(6.seconds)
    Collection(model)
    pumpMainLoop(1.seconds)
    assertThat(starts.value).isEqualTo(1)
    assertThat(droppedItems).isEqualTo(1)

    model.close()
    assertThat(logs.messages().count { it == "FakeViewModel cleared" }).isEqualTo(1)
  }

  @Test
  fun `a throwing mapping is logged once and ends the collection with an error`() {
    val model = FakeScreenModel(toState = { throw IllegalStateException("mapping failed") })

    val collection = Collection(model)
    pumpMainLoopUntil { collection.completions == 1 }
    pumpMainLoop(1.seconds)

    assertThat(collection.items).isEmpty()
    assertThat(collection.completionErrors).isEqualTo(1)
    assertThat(collection.cancellations).isEqualTo(0)
    val errors = logs.entries().filter { (priority, message) ->
      priority == LogPriority.ERROR && message == "FakeScreenModel state collection failed"
    }
    assertThat(errors.size).isEqualTo(1)
    model.close()
  }

  @Test
  fun `close while an item waits to be acknowledged clears the ViewModel but never ends that collection`() {
    val model = FakeScreenModel()
    val collection = Collection(model, acknowledgeItems = false)
    pumpMainLoopUntil { collection.items.isNotEmpty() }

    model.close()
    pumpMainLoop(1.seconds)

    // The native flow waits for the acknowledgement without listening for cancellation.
    assertThat(collection.items).containsExactly("screen:initial")
    assertThat(collection.cancellations).isEqualTo(0)
    assertThat(collection.completions).isEqualTo(0)
    assertThat(logs.messages().count { it == "FakeViewModel cleared" }).isEqualTo(1)
  }

  @Test
  fun `a ViewModel cannot be created after close`() {
    val model = FakeScreenModel()
    model.close()

    val result = runCatching { model.secondViewModel() }

    assertThat(
      result.exceptionOrNull()?.message,
    ).isEqualTo("FakeScreenModel is closed, so it cannot create a ViewModel")
  }

  @Test
  fun `a screen model holds one ViewModel`() {
    val model = FakeScreenModel()

    val result = runCatching { model.secondViewModel() }

    assertThat(result.isFailure).isEqualTo(true)
    assertThat(logs.messages().any { "cleared" in it }).isFalse()
    model.close()
  }
}

/** One collection of [ScreenModel.states], recording what reaches each callback. */
private class Collection(model: ScreenModel<String>, acknowledgeItems: Boolean = true) {
  val items = mutableListOf<String>()
  val onMainThread = mutableListOf<Boolean>()
  var completions = 0
  var completionErrors = 0
  var cancellations = 0
  private val cancel = model.states(
    { item, next, _ ->
      items += item
      onMainThread += NSThread.isMainThread
      if (acknowledgeItems) next()
    },
    { error, _ ->
      completions++
      if (error != null) completionErrors++
    },
    { _, _ -> cancellations++ },
  )

  fun cancel() {
    cancel.invoke()
  }
}

private class FakeScreenModel(
  starts: AtomicInt = AtomicInt(0),
  toState: (String) -> String = { "screen:$it" },
) : ScreenModel<String>() {
  private val factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
      @Suppress("UNCHECKED_CAST")
      return FakeViewModel(starts) as T
    }
  }
  private val viewModel = viewModel(factory, FakeViewModel::class, toState)

  fun ping() {
    viewModel.send(Unit)
  }

  fun secondViewModel() {
    viewModel(factory, FakeViewModel::class) { "second:$it" }
  }
}

private class FakeViewModel(presenterStarts: AtomicInt) : MoleculeViewModel<Unit, String>(
  initialState = "initial",
  presenter = FakePresenter(presenterStarts),
)

private class FakePresenter(private val starts: AtomicInt) : MoleculePresenter<Unit, String> {
  @Composable
  override fun MoleculePresenterScope<Unit>.present(lastState: String): String {
    remember { starts.incrementAndGet() }
    return lastState
  }
}

private fun pumpMainLoop(duration: Duration) {
  pumpMainLoopUntil(duration) { false }
}

/** Drives the main run loop in 10 ms slices until [condition] holds or [timeout] passes. */
private fun pumpMainLoopUntil(timeout: Duration = 10.seconds, condition: () -> Boolean): Boolean {
  val start = TimeSource.Monotonic.markNow()
  while (!condition()) {
    if (start.elapsedNow() > timeout) return false
    NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.01))
  }
  return true
}

private class CapturingLogger : LogcatLogger {
  private val lock = NSLock()
  private val captured = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> = locked { captured.toList() }

  fun messages(): List<String> = entries().map { it.second }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    locked { captured += priority to message() }
  }

  private fun <T> locked(block: () -> T): T {
    lock.lock()
    try {
      return block()
    } finally {
      lock.unlock()
    }
  }
}
