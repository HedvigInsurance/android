package com.hedvig.android.odyssey.resolution.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.odyssey.R
import com.hedvig.odyssey.designsystem.BlurredFullScreenProgressOverlay

@Composable
fun PayoutProgress(
  title: String,
  onSuccessTitle: String,
  onSuccessMessage: String,
  onContinueMessage: String,
  onContinue: () -> Unit,
  isCompleted: Boolean,
) {
  BlurredFullScreenProgressOverlay {
    AnimatedVisibility(
      modifier = Modifier.align(Alignment.Center),
      enter = fadeIn(),
      exit = fadeOut(),
      visible = !isCompleted,
    ) {
      Column(verticalArrangement = Arrangement.Center) {
        Text(
          modifier = Modifier.padding(18.dp),
          text = title,
          style = TextStyle.Default.copy(fontSize = 22.sp),
        )
      }
    }

    Column(
      modifier = Modifier
        .padding(bottom = 150.dp)
        .align(Alignment.Center),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      AnimatedVisibility(
        enter = scaleIn(
          animationSpec = keyframes {
            durationMillis = 400
            0f at 0
            1.2f at 200 with FastOutSlowInEasing
            1f at 400 with LinearOutSlowInEasing
          },
        ),
        exit = scaleOut(),
        visible = isCompleted,
      ) {
        Image(
          modifier = Modifier
            .padding(16.dp)
            .size(55.dp, 55.dp),
          painter = painterResource(id = R.drawable.ic_check_circle),
          colorFilter = ColorFilter.tint(MaterialTheme.colors.onBackground),
          contentDescription = "checkmark icon",
        )
      }

      AnimatedVisibility(
        enter = fadeIn(
          animationSpec = keyframes {
            delayMillis = 800
            durationMillis = 200
            1f at 200 with LinearOutSlowInEasing
          },
        ),
        exit = fadeOut(),
        visible = isCompleted,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = onSuccessTitle,
            textAlign = TextAlign.Center,
            style = TextStyle.Default.copy(fontSize = 40.sp),
          )
          Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = onSuccessMessage,
            textAlign = TextAlign.Center,
            style = TextStyle.Default.copy(fontSize = 15.sp),
          )
        }
      }
    }

    AnimatedVisibility(
      modifier = Modifier.align(Alignment.BottomCenter),
      enter = fadeIn(
        animationSpec = keyframes {
          delayMillis = 800
          durationMillis = 200
          1f at 200 with LinearOutSlowInEasing
        },
      ),
      exit = fadeOut(),
      visible = isCompleted,
    ) {
      LargeContainedButton(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        onClick = onContinue,
      ) {
        Text(onContinueMessage)
      }
    }
  }
}
