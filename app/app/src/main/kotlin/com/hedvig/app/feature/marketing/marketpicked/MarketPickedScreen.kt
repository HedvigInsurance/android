package com.hedvig.app.feature.marketing.marketpicked

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvig_off_white
import com.hedvig.android.market.Market
import com.hedvig.app.R

@Composable
fun MarketPickedScreen(
  onClickMarket: () -> Unit,
  onClickSignUp: () -> Unit,
  onClickLogIn: () -> Unit,
  market: Market,
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .safeDrawingPadding(),
  ) {
    IconButton(
      onClick = onClickMarket,
      modifier = Modifier.padding(4.dp), // 4.dp from [androidx.compose.material.AppBar.AppBarHorizontalPadding].
    ) {
      Image(
        painter = painterResource(market.flag),
        contentDescription = null,
      )
    }
    Image(
      painter = painterResource(R.drawable.ic_wordmark_h),
      contentDescription = stringResource(hedvig.resources.R.string.HEDVIG_LOGO_ACCESSIBILITY),
      modifier = Modifier.align(Alignment.Center),
      colorFilter = ColorFilter.tint(hedvig_off_white),
    )
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      if (market == Market.SE) {
        LargeContainedButton(
          onClick = onClickSignUp,
          colors = ButtonDefaults.buttonColors(),
        ) {
          Text(
            text = stringResource(hedvig.resources.R.string.MARKETING_GET_HEDVIG),
          )
        }
      }
      LargeOutlinedButton(onClick = onClickLogIn) {
        Text(text = stringResource(hedvig.resources.R.string.MARKETING_SCREEN_LOGIN))
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewMarketPicked() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      MarketPickedScreen(
        onClickMarket = {},
        onClickSignUp = {},
        onClickLogIn = {},
        market = Market.SE,
      )
    }
  }
}
