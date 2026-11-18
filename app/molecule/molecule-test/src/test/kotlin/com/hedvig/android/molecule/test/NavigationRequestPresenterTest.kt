package com.hedvig.android.molecule.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.public.restoredAsOffer
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * A screen that offers the next step for its text on Submit, against the same screen built with an acknowledgement
 * event, the mechanism [NavigationRequest] replaces. Each bug is shown to happen with the acknowledgement and not with
 * the request.
 */
class NavigationRequestPresenterTest {
  private sealed interface Event {
    data object Submit : Event

    data class Type(val text: String) : Event

    /** Only the acknowledgement screen has it: the platform says it opened `next`. */
    data object Shown : Event
  }

  private data class Screen(val text: String, val next: NavigationRequest<String>?)

  private class RequestPresenter : MoleculePresenter<Event, Screen> {
    @Composable
    override fun MoleculePresenterScope<Event>.present(lastState: Screen): Screen {
      var screen by remember { mutableStateOf(lastState.copy(next = lastState.next.restoredAsOffer())) }
      CollectEvents { event ->
        when (event) {
          Event.Submit -> {
            if (screen.next?.isPending != true) {
              screen = screen.copy(next = NavigationRequest.of(screen.text))
            }
          }

          is Event.Type -> {
            screen = screen.copy(text = event.text)
          }

          Event.Shown -> {}
        }
      }
      return screen
    }
  }

  private data class AckScreen(val text: String, val next: String?)

  /** Submit is ignored while `next` is set; [Event.Shown] clears it. [keepOfferOnRestart] restores `next` as is. */
  private class AckPresenter(private val keepOfferOnRestart: Boolean = false) : MoleculePresenter<Event, AckScreen> {
    @Composable
    override fun MoleculePresenterScope<Event>.present(lastState: AckScreen): AckScreen {
      var screen by remember { mutableStateOf(if (keepOfferOnRestart) lastState else lastState.copy(next = null)) }
      CollectEvents { event ->
        when (event) {
          Event.Submit -> if (screen.next == null) screen = screen.copy(next = screen.text)
          is Event.Type -> screen = screen.copy(text = event.text)
          Event.Shown -> screen = screen.copy(next = null)
        }
      }
      return screen
    }
  }

  /** What a platform keyed on the acknowledged `next` opens: each change to a non-null value. */
  private fun List<AckScreen>.opened(): List<String> {
    var previous: String? = null
    return mapNotNull { screen -> screen.next.takeIf { it != null && it != previous }.also { previous = screen.next } }
  }

  @Test
  fun `request - a continue in the frame the platform took the last offer gets a new offer`() = runTest {
    RequestPresenter().testFrames(Screen("step", null)) {
      emit(Event.Submit)
      frame()
      val first = checkNotNull(latest.next)
      assertThat(first.tryConsume()).isTrue()

      emit(Event.Submit)
      frame()
      val second = checkNotNull(latest.next)
      assertThat(second.isPending).isTrue()
      assertThat(second.target).isEqualTo("step")
      assertThat(observed.mapNotNull { it.next }.distinct()).containsExactly(first, second)
    }
  }

  @Test
  fun `acknowledgement - an acknowledgement and a continue in one frame leave the screen stuck`() = runTest {
    AckPresenter().testFrames(AckScreen("step", null)) {
      emit(Event.Submit)
      frame()
      emit(Event.Shown, Event.Submit)
      frame()
      // The cleared state was never returned, so the platform saw no change and opened the step only once, and
      // every later continue is ignored because `next` is still set.
      emit(Event.Submit)
      frame()
      assertThat(observed.opened()).containsExactly("step")
      assertThat(latest.next).isEqualTo("step")
    }
  }

  @Test
  fun `request - every state after an offer carries it until the platform takes it`() = runTest {
    RequestPresenter().testFrames(Screen("step", null)) {
      emit(Event.Submit, Event.Type("edited"))
      frame()
      emit(Event.Type("edited again"))
      frame()
      val offers = observed.map { it.next }.dropWhile { it == null }
      assertThat(offers).hasSize(2)
      assertThat(offers.distinct()).hasSize(1)
      assertThat(offers.first()!!.target).isEqualTo("step")
    }
  }

  @Test
  fun `request - a continue while the offer is pending is ignored, and after it was taken offers again`() = runTest {
    RequestPresenter().testFrames(Screen("step", null)) {
      emit(Event.Submit)
      frame()
      val first = checkNotNull(latest.next)
      emit(Event.Type("other"), Event.Submit)
      frame()
      assertThat(latest.next).isEqualTo(first)

      first.tryConsume()
      emit(Event.Submit)
      frame()
      assertThat(latest.next!!.target).isEqualTo("other")
      assertThat(latest.next!!.isPending).isTrue()
    }
  }

  @Test
  fun `request - a restart drops a pending offer and never replays a taken one`() = runTest {
    RequestPresenter().testFrames(Screen("step", null)) {
      emit(Event.Submit)
      frame()
      restart()
      assertThat(latest.next).isNull()

      emit(Event.Submit)
      frame()
      val taken = checkNotNull(latest.next)
      taken.tryConsume()
      restart()
      assertThat(latest.next).isNotNull()
      assertThat(latest.next!!.tryConsume()).isFalse()
    }
  }

  @Test
  fun `acknowledgement - an offer restored as it was is opened again after a restart`() = runTest {
    AckPresenter(keepOfferOnRestart = true).testFrames(AckScreen("step", null)) {
      emit(Event.Submit)
      frame()
      // The platform opened it, then the presenter stopped before the acknowledgement was handled.
      stop()
      emit(Event.Shown)
      start()
      // A screen composed again starts from this state and opens the step a second time.
      assertThat(latest.next).isEqualTo("step")
    }
  }
}
