package com.hedvig.android.molecule.public

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

class MoleculeViewModelTest {
  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `the presenter emits a new state after an event`() = runTest {
    val viewModel = CounterViewModel()

    viewModel.uiState.test {
      assertThat(awaitItem()).isEqualTo(0)
      viewModel.emit(CounterEvent.Increment)
      assertThat(awaitItem()).isEqualTo(1)
    }
  }
}

private sealed interface CounterEvent {
  data object Increment : CounterEvent
}

private class CounterViewModel : MoleculeViewModel<CounterEvent, Int>(
  initialState = 0,
  presenter = CounterPresenter(),
)

private class CounterPresenter : MoleculePresenter<CounterEvent, Int> {
  @Composable
  override fun MoleculePresenterScope<CounterEvent>.present(lastState: Int): Int {
    var count by remember { mutableIntStateOf(lastState) }
    CollectEvents { event ->
      when (event) {
        CounterEvent.Increment -> count++
      }
    }
    return count
  }
}
