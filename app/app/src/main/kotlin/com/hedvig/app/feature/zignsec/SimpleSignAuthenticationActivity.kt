package com.hedvig.app.feature.zignsec

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.android.auth.AuthStatus
import com.hedvig.android.auth.AuthTokenService
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import hedvig.resources.R
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SimpleSignAuthenticationActivity : ComponentActivity() {
  private val viewModel: SimpleSignAuthenticationViewModel by viewModel { parametersOf(data) }
  private val authTokenService: AuthTokenService by inject()

  private val data by lazy {
    intent.parcelableExtra<SimpleSignAuthenticationData>(DATA)
      ?: error("Programmer error: DATA not passed to ${this.javaClass.name}")
  }
  private val zignSecMarket by lazy {
    when (data.market) {
      Market.NO -> ZignSecMarket.NO
      Market.DK -> ZignSecMarket.DK
      else -> error("Can't open SimpleSignAuthenticationActivity with market ${data.market}")
    }
  }

  private val customZignSecTabLauncher = registerForActivityResult(
    object : ActivityResultContract<String, Int>() {
      override fun createIntent(context: Context, input: String): Intent {
        return CustomTabsIntent.Builder()
          .setInitialActivityHeightPx(3000)
          .setToolbarCornerRadiusDp(16)
          .build()
          .intent
          .apply {
            setData(Uri.parse(input))
          }
      }

      override fun parseResult(resultCode: Int, intent: Intent?): Int {
        return resultCode
      }
    },
  ) { /* nothing to do with the result, listening to viewModel.events covers leaving this activity */ }

  @OptIn(ExperimentalLayoutApi::class, ExperimentalComposeUiApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT))
    super.onCreate(savedInstanceState)
    logcat { "SimpleSignAuthenticationActivity with market:$zignSecMarket" }

    onBackPressedDispatcher.addCallback(this) {
      logcat { "SimpleSignAuthenticationActivity: invoked back. Going back to marketing" }
      remove()
      onBackPressedDispatcher.onBackPressed()
    }

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
        viewModel.subscribeToAuthSuccessEvent()
      }
    }
    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
        authTokenService.authStatus.collect { authStatus ->
          if (authStatus != null && authStatus is AuthStatus.LoggedIn) {
            goToLoggedIn()
          }
        }
      }
    }

    var hasErrored by mutableStateOf(false)
    viewModel.events.observe(this) { event ->
      logcat { "Simple sign event:$event" }
      when (event) {
        SimpleSignAuthenticationViewModel.Event.Error -> {
          hasErrored = true
        }
      }
    }

    viewModel.zignSecUrl.observe(this) { zignSecUrl ->
      if (zignSecUrl.contains("failure")) {
        logcat { "Url loading had \"failure\" in it. Failing authentication" }
        viewModel.authFailed()
      }
      customZignSecTabLauncher.launch(zignSecUrl)
    }

    setContent {
      val focusManager = LocalFocusManager.current
      val textInput by viewModel.input.observeAsState("")
      val isValidInput by viewModel.isValid.observeAsState()
      val isSubmitting by viewModel.isSubmitting.observeAsState()

      HedvigTheme {
        Surface(
          color = MaterialTheme.colorScheme.background,
          modifier = Modifier
            .fillMaxSize()
            .clearFocusOnTap(),
        ) {
          Box(propagateMinConstraints = true) {
            Column(
              modifier = Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            ) {
              TopAppBarWithBack(
                onClick = { onBackPressedDispatcher.onBackPressed() },
                title = stringResource(R.string.zignsec_login_screen_title),
              )
              if (hasErrored) {
                HedvigErrorSection(retry = { finish() })
              } else {
                // A layout which makes the button and the textField always visible on top of the IME.
                Layout(
                  modifier = Modifier.weight(1f),
                  content = {
                    InputTextField(
                      textInput = textInput,
                      setTextInput = { viewModel.setInput(it) },
                      onSubmit = {
                        focusManager.clearFocus()
                        startZignSecIfValid()
                      },
                      zignSecMarket = zignSecMarket,
                      modifier = Modifier.layoutId(TextFieldId),
                    )
                    ContinueButton(
                      onClick = {
                        focusManager.clearFocus()
                        startZignSecIfValid()
                      },
                      isValidInput = isValidInput,
                      isSubmitting = isSubmitting,
                      zignSecMarket = zignSecMarket,
                      modifier = Modifier.layoutId(ContinueButtonId),
                    )
                    Spacer(
                      Modifier
                        .layoutId(BottomWindowInsetsId)
                        .windowInsetsPadding(
                          WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                        ),
                    )
                  },
                ) { measurables, constraints ->
                  val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
                  val textFieldPlaceable = measurables.first { it.layoutId == TextFieldId }.measure(looseConstraints)
                  val buttonPlaceable = measurables.first { it.layoutId == ContinueButtonId }.measure(looseConstraints)
                  val insetsPlaceable =
                    measurables.first { it.layoutId == BottomWindowInsetsId }.measure(looseConstraints)
                  val maxWidth = constraints.maxWidth
                  val maxHeight = constraints.maxHeight
                  val spacingHeight = 16.dp.roundToPx() // The space between the three items
                  layout(maxWidth, maxHeight) {
                    val insetsYPosition = maxHeight - insetsPlaceable.height
                    insetsPlaceable.place(0, insetsYPosition)
                    val buttonYPosition = insetsYPosition - spacingHeight - buttonPlaceable.height
                    buttonPlaceable.place(0, buttonYPosition)
                    val textFieldYPosition = minOf(
                      (maxHeight / 2) - (textFieldPlaceable.height / 2),
                      buttonYPosition - spacingHeight - textFieldPlaceable.height,
                    )
                    textFieldPlaceable.place(0, textFieldYPosition)
                  }
                }
              }
            }
            HedvigFullScreenCenterAlignedProgress(show = isSubmitting == true)
          }
        }
      }
    }
  }

  private fun goToLoggedIn() {
    startActivity(
      LoggedInActivity.newInstance(
        this,
        withoutHistory = true,
      ),
    )
  }

  private fun startZignSecIfValid() {
    if (viewModel.isValid.value == true) {
      viewModel.startZignSec()
    }
  }

  companion object {
    private const val DATA = "DATA"

    fun newInstance(context: Context, market: Market) =
      Intent(context, SimpleSignAuthenticationActivity::class.java).apply {
        putExtra(DATA, SimpleSignAuthenticationData(market))
      }
  }
}

private const val TextFieldId = "TextFieldId"
private const val ContinueButtonId = "ContinueButtonId"
private const val BottomWindowInsetsId = "BottomWindowInsetsId"

@Composable
private fun InputTextField(
  textInput: String,
  setTextInput: (String) -> Unit,
  onSubmit: () -> Unit,
  zignSecMarket: ZignSecMarket,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    value = textInput,
    onValueChange = { newInput ->
      val maxLengthAllowed = when (zignSecMarket) {
        ZignSecMarket.NO -> 11
        ZignSecMarket.DK -> 10
      }
      if (newInput.length > maxLengthAllowed) {
        return@HedvigTextField
      }
      setTextInput(newInput)
    },
    label = {
      Text(
        stringResource(
          when (zignSecMarket) {
            ZignSecMarket.NO -> R.string.simple_sign_login_text_field_label
            ZignSecMarket.DK -> R.string.simple_sign_login_text_field_label_dk
          },
        ),
      )
    },
    supportingText = {
      Text(
        stringResource(
          when (zignSecMarket) {
            ZignSecMarket.NO -> R.string.simple_sign_login_text_field_helper_text
            ZignSecMarket.DK -> R.string.simple_sign_login_text_field_helper_text_dk
          },
        ),
      )
    },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Number,
      imeAction = ImeAction.Next,
    ),
    keyboardActions = KeyboardActions(
      onNext = { onSubmit() },
    ),
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

@Composable
private fun ContinueButton(
  onClick: () -> Unit,
  isValidInput: Boolean?,
  isSubmitting: Boolean?,
  zignSecMarket: ZignSecMarket,
  modifier: Modifier = Modifier,
) {
  HedvigContainedButton(
    text = stringResource(
      when (zignSecMarket) {
        ZignSecMarket.NO -> R.string.simple_sign_sign_in
        ZignSecMarket.DK -> R.string.simple_sign_sign_in_dk
      },
    ),
    onClick = onClick,
    enabled = isValidInput == true && isSubmitting != true,
    modifier = modifier.padding(horizontal = 16.dp),
  )
}

private enum class ZignSecMarket {
  NO,
  DK,
}
