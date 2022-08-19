package com.hedvig.app.feature.marketing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import coil.ImageLoader
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvigBlack
import com.hedvig.android.core.designsystem.theme.hedvigOffWhite
import com.hedvig.android.core.ui.debugBorder
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.android.market.createOnboardingUri
import com.hedvig.app.R
import com.hedvig.app.authenticate.LoginDialog
import com.hedvig.app.feature.marketing.data.MarketingBackground
import com.hedvig.app.feature.marketing.marketpicked.MarketPickedScreen
import com.hedvig.app.feature.marketing.pickmarket.CtaButtonParams
import com.hedvig.app.feature.marketing.pickmarket.PickMarketScreen
import com.hedvig.app.feature.marketing.ui.BackgroundImage
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationActivity
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.makeToast
import com.hedvig.hanalytics.LoginMethod
import e
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MarketingActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)
    val viewModel = getViewModel<MarketingViewModel>()
    val imageLoader = get<ImageLoader>()
    setContent {
      HedvigTheme(
        colorOverrides = { colors ->
          colors.copy(
            primary = hedvigOffWhite,
            onPrimary = hedvigBlack,
            secondary = hedvigOffWhite,
            onBackground = hedvigOffWhite,
          )
        },
      ) {
        val marketingBackground by viewModel.marketingBackground.collectAsState()
        val state by viewModel.state.collectAsState()
        MarketingScreen(
          marketingBackground = marketingBackground,
          state = state,
          imageLoader = imageLoader,
          submitMarketAndLanguage = viewModel::submitMarketAndLanguage,
          setMarket = viewModel::setMarket,
          setLanguage = viewModel::setLanguage,
          onFlagClick = viewModel::onFlagClick,
          onClickSignUp = { market ->
            viewModel.onClickSignUp()
            openOnboarding(market)
          },
          onClickLogIn = { market ->
            viewModel.onClickLogIn()
            onClickLogin(state, market)
          },
        )
      }
    }
  }

  private fun openOnboarding(market: Market) {
    val baseUrl = getString(R.string.WEB_BASE_URL).substringAfter("//")
    val uri = market.createOnboardingUri(this, baseUrl)
    val browserIntent = Intent(Intent.ACTION_VIEW, uri)

    if (browserIntent.resolveActivity(packageManager) != null) {
      startActivity(browserIntent)
    } else {
      e { "Tried to launch $uri but the phone has nothing to support such an intent." }
      makeToast(hedvig.resources.R.string.general_unknown_error)
    }
  }

  private fun onClickLogin(
    state: MarketingViewState,
    market: Market,
  ) = when (state.loginMethod) {
    LoginMethod.BANK_ID_SWEDEN -> LoginDialog().show(
      supportFragmentManager,
      LoginDialog.TAG,
    )
    LoginMethod.NEM_ID, LoginMethod.BANK_ID_NORWAY -> {
      startActivity(
        SimpleSignAuthenticationActivity.newInstance(
          this@MarketingActivity,
          market,
        ),
      )
    }
    LoginMethod.OTP -> {
      // Not implemented
    }
    null -> {}
  }

  companion object {
    fun newInstance(context: Context, withoutHistory: Boolean = false) =
      Intent(context, MarketingActivity::class.java).apply {
        if (withoutHistory) {
          addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
      }
  }
}

@Composable
private fun MarketingScreen(
  marketingBackground: MarketingBackground?,
  state: MarketingViewState,
  imageLoader: ImageLoader,
  submitMarketAndLanguage: () -> Unit,
  setMarket: (Market) -> Unit,
  setLanguage: (Language) -> Unit,
  onFlagClick: () -> Unit,
  onClickSignUp: (market: Market) -> Unit,
  onClickLogIn: (market: Market) -> Unit,
) {
  val ctaButton = remember {
    movableContentOf<CtaButtonParams> { ctaButtonParams ->
      LargeContainedTextButton(
        text = ctaButtonParams.text,
        onClick = ctaButtonParams.onClick,
        enabled = ctaButtonParams.enabled,
        modifier = ctaButtonParams.modifier.animatePlacementInWindow().debugBorder(),
      )
    }
  }
  Box(Modifier.fillMaxSize()) {
    BackgroundImage(marketingBackground, imageLoader)
    val selectedMarket = state.selectedMarket
//    Crossfade(selectedMarket) { market ->
    selectedMarket.let { market ->
      if (market == null) {
        PickMarketScreen(
          ctaButton = ctaButton,
          onSubmit = submitMarketAndLanguage,
          onSelectMarket = setMarket,
          onSelectLanguage = setLanguage,
          selectedMarket = state.market,
          selectedLanguage = state.language,
          markets = state.availableMarkets,
          enabled = state.canSetMarketAndLanguage(),
        )
      } else {
        MarketPickedScreen(
          ctaButton = ctaButton,
          onClickMarket = onFlagClick,
          onClickSignUp = { onClickSignUp(market) },
          onClickLogIn = { onClickLogIn(market) },
          flagRes = market.flag,
        )
      }
    }
    if (state.isLoading) {
      CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
  }
}

fun Modifier.animatePlacementInWindow(): Modifier = composed {
  val coroutineScope = rememberCoroutineScope()
  var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
  var animatable by remember {
    mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
  }
  this
    .onPlaced {
      targetOffset = it.positionInWindow().round()
    }
    .offset {
      val anim = animatable ?: Animatable(targetOffset, IntOffset.VectorConverter).also { animatable = it }
      if (anim.targetValue != targetOffset) {
        coroutineScope.launch {
          anim.animateTo(targetOffset, spring(stiffness = Spring.StiffnessVeryLow))
        }
      }
      animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
    }
}
