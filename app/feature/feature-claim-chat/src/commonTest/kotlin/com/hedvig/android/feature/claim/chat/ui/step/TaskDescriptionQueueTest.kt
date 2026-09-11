package com.hedvig.android.feature.claim.chat.ui.step

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class TaskDescriptionQueueTest {
  @Test
  fun `every description is shown even when several arrive at once`() = runTest {
    val descriptions = MutableStateFlow(listOf("one"))

    pacedDescriptions(descriptions, minimumDwell = 100.milliseconds).test {
      assertThat(awaitItem()).isEqualTo("one")

      // One poll delivering two new descriptions at once, the case that showing only the latest drops.
      descriptions.value = listOf("one", "two", "three")

      assertThat(awaitItem()).isEqualTo("two")
      assertThat(awaitItem()).isEqualTo("three")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `a description is held for at least the minimum dwell`() = runTest {
    val descriptions = MutableStateFlow(listOf("one", "two"))
    // Turbine's test block swaps the receiver, so the scheduler has to be captured out here.
    val scheduler = testScheduler

    pacedDescriptions(descriptions, minimumDwell = 500.milliseconds).test {
      assertThat(awaitItem()).isEqualTo("one")
      val beforeSecond = scheduler.currentTime
      assertThat(awaitItem()).isEqualTo("two")
      assertThat(scheduler.currentTime - beforeSecond >= 500).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `an empty list produces nothing`() = runTest {
    val descriptions = MutableStateFlow(emptyList<String>())

    pacedDescriptions(descriptions, minimumDwell = 100.milliseconds).test {
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `descriptions appearing later are still shown in order`() = runTest {
    val descriptions = MutableStateFlow(emptyList<String>())

    pacedDescriptions(descriptions, minimumDwell = 50.milliseconds).test {
      expectNoEvents()

      descriptions.value = listOf("first")
      assertThat(awaitItem()).isEqualTo("first")

      descriptions.value = listOf("first", "second")
      assertThat(awaitItem()).isEqualTo("second")
      cancelAndIgnoreRemainingEvents()
    }
  }
}
