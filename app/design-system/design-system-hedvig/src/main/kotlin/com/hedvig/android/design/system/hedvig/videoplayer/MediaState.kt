package com.hedvig.android.design.system.hedvig.videoplayer

import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import kotlin.math.absoluteValue

@Composable
fun rememberMediaState(player: Player?): MediaState = remember { MediaState(initPlayer = player) }.apply {
  this.player = player
}

/**
 * A state object that can be hoisted to control and observe changes for [Media].
 */
@Stable
class MediaState(
  initPlayer: Player? = null,
) {
  /**
   * The player to use, or null to detach the current player.
   * Only players which are accessed on the main thread are supported (`
   * player.getApplicationLooper() == Looper.getMainLooper()`).
   */
  var player: Player?
    set(current) {
      require(current == null || current.applicationLooper == Looper.getMainLooper()) {
        "Only players which are accessed on the main thread are supported."
      }
      val previous = _player
      if (current !== previous) {
        _player = current
        onPlayerChanged(previous, current)
      }
    }
    get() = _player

  /**
   * The state of the [Media]'s [player].
   */
  val playerState: PlayerState? get() = stateOfPlayerState.value

  // region Controller visibility related properties and functions

  /**
   * Whether the controller is showing.
   */
  var isControllerShowing: Boolean
    get() = controllerVisibility.isShowing
    set(value) {
      controllerVisibility = if (value) {
        ControllerVisibility.Visible
      } else {
        ControllerVisibility.Invisible
      }
    }

  /**
   * The current [visibility][ControllerVisibility] of the controller.
   */
  var controllerVisibility: ControllerVisibility by mutableStateOf(ControllerVisibility.Invisible)

  /**
   * Typically, when controller is shown, it will be automatically hidden after a short time has
   * elapsed without user interaction. If [shouldShowControllerIndefinitely] is true, you should
   * consider disabling this behavior, and show the controller indefinitely.
   */
  val shouldShowControllerIndefinitely: Boolean by derivedStateOf {
    playerState?.run {
      controllerAutoShow &&
        !timeline.isEmpty &&
        (
          playbackState == Player.STATE_IDLE ||
            playbackState == Player.STATE_ENDED ||
            !playWhenReady
        )
    } != false
  }

  internal var controllerAutoShow: Boolean by mutableStateOf(true)

  internal fun maybeShowController() {
    if (shouldShowControllerIndefinitely) {
      controllerVisibility = ControllerVisibility.Visible
    }
  }

  // endregion

  // region internally used properties and functions
  private val listener = object : Player.Listener {
    override fun onRenderedFirstFrame() {
      closeShutter = false
    }

    override fun onEvents(player: Player, events: Player.Events) {
      if (events.containsAny(
          Player.EVENT_PLAYBACK_STATE_CHANGED,
          Player.EVENT_PLAY_WHEN_READY_CHANGED,
        )
      ) {
        maybeShowController()
      }
    }
  }
  private var _player: Player? by mutableStateOf(initPlayer)

  private fun onPlayerChanged(previous: Player?, current: Player?) {
    previous?.removeListener(listener)
    stateOfPlayerState.value?.dispose()
    stateOfPlayerState.value = current?.state()
    current?.addListener(listener)
    if (current == null) {
      controllerVisibility = ControllerVisibility.Invisible
    }
  }

  internal val stateOfPlayerState = mutableStateOf(initPlayer?.state())

  internal val contentAspectRatioRaw by derivedStateOf {
    (playerState?.videoSize ?: VideoSize.UNKNOWN).aspectRatio
  }

  @Suppress("ktlint:standard:backing-property-naming")
  private var _contentAspectRatio by mutableFloatStateOf(0f)
  internal var contentAspectRatio
    set(value) {
      val aspectDeformation: Float = value / contentAspectRatio - 1f
      if (aspectDeformation.absoluteValue > 0.01f) {
        // Not within the allowed tolerance, populate the new aspectRatio.
        _contentAspectRatio = value
      }
    }
    get() = _contentAspectRatio

  // true: video track is selected
  // false: non video track is selected
  // null: there isn't any track
  internal val isVideoTrackSelected: Boolean? by derivedStateOf {
    playerState?.tracks
      ?.takeIf { it.groups.isNotEmpty() }
      ?.isTypeSelected(C.TRACK_TYPE_VIDEO)
  }

  internal var closeShutter by mutableStateOf(true)

  // endregion

  init {
    initPlayer?.addListener(listener)
  }
}

/**
 * The visibility state of the controller.
 */
enum class ControllerVisibility(
  val isShowing: Boolean,
) {
  /**
   * All UI controls are visible.
   */
  Visible(true),

  /**
   * A part of UI controls are visible.
   */
  PartiallyVisible(true),

  /**
   * All UI controls are hidden.
   */
  Invisible(false),
}

private val VideoSize.aspectRatio
  get() = if (height == 0) 0f else width * pixelWidthHeightRatio / height
