package com.hedvig.app.ui.compose.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

@OptIn(ExperimentalUnitApi::class, ExperimentalAnimationApi::class)
@Composable
fun FullScreenProgressOverlay(show: Boolean) {
    AnimatedVisibility(
        visible = show,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500, delayMillis = 400))
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.onPrimary,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val angle by infiniteTransition.animateFloat(
                    initialValue = 0F,
                    targetValue = 360F,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing)
                    )
                )

                Icon(
                    painter = painterResource(id = R.drawable.ic_hedvig_h_progress),
                    modifier = Modifier
                        .rotate(angle)
                        .animateEnterExit(
                            enter = fadeIn(animationSpec = tween(200, delayMillis = 700)),
                            exit = fadeOut(animationSpec = tween(200))
                        )
                        .width(32.dp)
                        .height(32.dp),
                    contentDescription = stringResource(R.string.login_smedium_button_active_resend_code)
                )
            }
        }
    }
}

@Preview
@Composable
fun FullScreenProgressOverlayPreview() {
    HedvigTheme {
        FullScreenProgressOverlay(true)
    }
}
