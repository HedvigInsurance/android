package com.hedvig.app.feature.marketing.marketpicked

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.hedvig.android.designsystem.component.button.LargeContainedButton
import com.hedvig.android.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.designsystem.theme.HedvigTheme
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.hedvigOffWhite

@Composable
fun MarketPickedScreen(
  onClickMarket: () -> Unit,
  onClickSignUp: () -> Unit,
  onClickLogIn: () -> Unit,
  @DrawableRes flagRes: Int,
) {
  val insets = LocalWindowInsets.current
  val statusBarHeight = with(LocalDensity.current) { insets.statusBars.top.toDp() }
  val navigationBarHeight = with(LocalDensity.current) { insets.navigationBars.bottom.toDp() }
  Box(modifier = Modifier.fillMaxSize()) {
    IconButton(
      onClick = onClickMarket,
      modifier = Modifier
        .padding(
          start = 8.dp,
          top = statusBarHeight,
        ),
    ) {
      Image(
        painter = painterResource(flagRes),
        contentDescription = null,
      )
    }
    Image(
      painter = painterResource(R.drawable.ic_wordmark_h),
      contentDescription = stringResource(R.string.HEDVIG_LOGO_ACCESSIBILITY),
      modifier = Modifier.align(Alignment.Center),
      colorFilter = ColorFilter.tint(hedvigOffWhite),
    )
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(horizontal = 16.dp),
    ) {
      LargeContainedButton(
        onClick = onClickSignUp,
        colors = ButtonDefaults.buttonColors(
          backgroundColor = MaterialTheme.colors.primary,
        ),
      ) {
        Text(
          text = stringResource(R.string.MARKETING_GET_HEDVIG),
        )
      }
      Spacer(Modifier.height(8.dp))
      LargeOutlinedButton(onClick = onClickLogIn) {
        Text(text = stringResource(R.string.MARKETING_SCREEN_LOGIN))
      }
      Spacer(Modifier.height(8.dp + navigationBarHeight))
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MarketPickedPreview() {
  HedvigTheme {
    MarketPickedScreen(
      onClickMarket = {},
      onClickSignUp = {},
      onClickLogIn = {},
      flagRes = R.drawable.ic_flag_se,
    )
  }
}
