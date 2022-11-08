package com.hedvig.app.feature.marketing.marketpicked

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvigOffWhite
import com.hedvig.app.R
import com.hedvig.app.feature.marketing.pickmarket.CtaButtonParams

@Composable
fun MarketPickedScreen(
  ctaButton: @Composable (CtaButtonParams) -> Unit,
  onClickMarket: () -> Unit,
  onClickSignUp: () -> Unit,
  onClickLogIn: () -> Unit,
  @DrawableRes flagRes: Int,
) {
  Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
    IconButton(
      onClick = onClickMarket,
      modifier = Modifier.padding(4.dp), // 4.dp from [androidx.compose.material.AppBar.AppBarHorizontalPadding].
    ) {
      Image(
        painter = painterResource(flagRes),
        contentDescription = null,
      )
    }
    Image(
      painter = painterResource(R.drawable.ic_wordmark_h),
      contentDescription = stringResource(hedvig.resources.R.string.HEDVIG_LOGO_ACCESSIBILITY),
      modifier = Modifier.align(Alignment.Center),
      colorFilter = ColorFilter.tint(hedvigOffWhite),
    )
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      ctaButton(
        CtaButtonParams(
          text = stringResource(hedvig.resources.R.string.MARKETING_GET_HEDVIG),
          onClick = onClickSignUp,
        ),
      )
      LargeOutlinedButton(onClick = onClickLogIn) {
        Text(text = stringResource(hedvig.resources.R.string.MARKETING_SCREEN_LOGIN))
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun MarketPickedPreview() {
  HedvigTheme {
    MarketPickedScreen(
      ctaButton = { ctaButtonParams ->
        Text(ctaButtonParams.toString())
//        LargeContainedTextButton(
//          text = ctaButtonParams.text,
//          onClick = ctaButtonParams.onClick,
//          modifier = ctaButtonParams.modifier,
//          enabled = ctaButtonParams.enabled,
//        )
      },
      onClickMarket = {},
      onClickSignUp = {},
      onClickLogIn = {},
      flagRes = hedvig.resources.R.drawable.ic_flag_se,
    )
  }
}
