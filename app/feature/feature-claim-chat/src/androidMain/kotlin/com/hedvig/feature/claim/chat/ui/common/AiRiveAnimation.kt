package com.hedvig.feature.claim.chat.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.rive.Rive
import app.rive.RiveFileSource
import app.rive.rememberArtboard
import app.rive.rememberRiveFile
import app.rive.rememberRiveWorker
import app.rive.rememberStateMachine
import app.rive.runtime.kotlin.core.Rive

@Composable
internal actual fun AiRiveAnimation(isAnimationComplete: Boolean, modifier: Modifier) {
  val context = LocalContext.current
  Rive.init(context) //todo: move to other place

  val isDark = isSystemInDarkTheme()
  val resourceName = if (isDark) "hedvig_loader_dark" else "hedvig_loader_light"

  val resourceId = context.resources.getIdentifier(
    resourceName,
    "raw",
    context.packageName,
  )

  val riveWorker = rememberRiveWorker()
  val riveFile = if (resourceId != 0) {
    rememberRiveFile(
      RiveFileSource.RawRes.from(resourceId),
      riveWorker,
    )
  } else {
    null
  }

  if (riveFile != null) {
    when (riveFile) {
      is app.rive.Result.Success -> {
        val file = riveFile.value
        val artboard = rememberArtboard(file)

        val stateMachine = rememberStateMachine(artboard, "Loading")

        Rive(
          file = file,
          modifier = modifier,
//          artboard = artboard,
//          stateMachine = stateMachine,
 //         playing = true,
        )
      }

      is app.rive.Result.Loading -> {
        Box(modifier = modifier)
      }

      is app.rive.Result.Error -> {
        Box(modifier = modifier)
      }
    }
  } else {
    Box(modifier = modifier)
  }
}
