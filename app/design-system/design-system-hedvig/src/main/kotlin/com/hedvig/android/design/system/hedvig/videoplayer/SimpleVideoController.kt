package com.hedvig.android.design.system.hedvig.videoplayer

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Pause
import com.hedvig.android.design.system.hedvig.icon.Play
import kotlinx.coroutines.delay

/**
 * A simple controller, which consists of a play/pause button and a time bar.
 */
@Composable
fun SimpleVideoController(
  mediaState: MediaState,
  modifier: Modifier = Modifier,
) {
  Crossfade(targetState = mediaState.isControllerShowing, modifier) { isShowing ->
    if (isShowing) {
      val controllerState = rememberControllerState(mediaState)
      var scrubbing by remember { mutableStateOf(false) }
      val hideWhenTimeout = !mediaState.shouldShowControllerIndefinitely && !scrubbing
      var hideEffectReset by remember { mutableStateOf(0) }
      LaunchedEffect(hideWhenTimeout, hideEffectReset) {
        if (hideWhenTimeout) {
          delay(500)
          mediaState.isControllerShowing = false
        }
      }
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Transparent),
      ) {
        IconButton(
          onClick = {
            hideEffectReset++
            controllerState.playOrPause()
          },
          modifier = Modifier
            .align(Alignment.Center),
        ) {
          Icon(
            imageVector =
            if (controllerState.showPause) HedvigIcons.Pause
            else HedvigIcons.Play,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
              .size(52.dp)
          )
        }

        LaunchedEffect(Unit) {
          while (true) {
            delay(200)
            controllerState.triggerPositionUpdate()
          }
        }
        TimeBar(
          controllerState.durationMs,
          controllerState.positionMs,
          controllerState.bufferedPositionMs,
          modifier = Modifier
            .systemGestureExclusion()
            .fillMaxWidth()
            .height(28.dp)
            .align(Alignment.BottomCenter),
          contentPadding = PaddingValues(12.dp),
          scrubberCenterAsAnchor = true,
          onScrubStart = { scrubbing = true },
          onScrubStop = { positionMs ->
            scrubbing = false
            controllerState.seekTo(positionMs)
          },
        )
      }
    }
  }
}
