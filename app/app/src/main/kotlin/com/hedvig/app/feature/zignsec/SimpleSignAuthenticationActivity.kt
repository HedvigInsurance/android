package com.hedvig.app.feature.zignsec

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.textfield.HedvigTextField
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.android.market.Market
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import hedvig.resources.R
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import slimber.log.d

class SimpleSignAuthenticationActivity : AppCompatActivity() {
  private val viewModel: SimpleSignAuthenticationViewModel by viewModel { parametersOf(data) }

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
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    d { "SimpleSignAuthenticationActivity with market:$zignSecMarket" }

    onBackPressedDispatcher.addCallback(this) {
      d { "SimpleSignAuthenticationActivity: invoked back. Going back to marketing" }
      remove()
      onBackPressedDispatcher.onBackPressed()
    }

    lifecycleScope.launch {
      lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.subscribeToAuthSuccessEvent()
      }
    }

    var hasErrored by mutableStateOf(false)
    viewModel.events.observe(this) { event ->
      d { "Simple sign event:$event" }
      when (event) {
        SimpleSignAuthenticationViewModel.Event.Success -> goToLoggedIn()
        SimpleSignAuthenticationViewModel.Event.Error -> {
          hasErrored = true
        }
      }
    }

    viewModel.zignSecUrl.observe(this) { zignSecUrl ->
      if (zignSecUrl.contains("failure")) {
        d { "Url loading had \"failure\" in it. Failing authentication" }
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
          Column(
            modifier = Modifier
              .verticalScroll(rememberScrollState())
              .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
          ) {
            TopAppBarWithBack(
              onClick = { onBackPressedDispatcher.onBackPressed() },
              title = stringResource(hedvig.resources.R.string.zignsec_login_screen_title),
              contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                .asPaddingValues(),
            )
            if (hasErrored) {
              GenericErrorScreen(
                onRetryButtonClick = { finish() },
                modifier = Modifier
                  .padding(16.dp)
                  .padding(top = (80 - 16).dp),
              )
            } else {
              Spacer(Modifier.weight(1f))
              InputTextField(
                textInput = textInput,
                setTextInput = { viewModel.setInput(it) },
                onSubmit = {
                  focusManager.clearFocus()
                  startZignSecIfValid()
                },
                zignSecMarket = zignSecMarket,
              )
              Spacer(Modifier.height(16.dp))
              ContinueButton(
                onClick = {
                  focusManager.clearFocus()
                  startZignSecIfValid()
                },
                isValidInput = isValidInput,
                isSubmitting = isSubmitting,
                zignSecMarket = zignSecMarket,
              )
              Spacer(Modifier.height(16.dp))
              Spacer(
                Modifier.windowInsetsPadding(
                  WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                ),
              )
            }
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

@Composable
private fun InputTextField(
  textInput: String,
  setTextInput: (String) -> Unit,
  onSubmit: () -> Unit,
  zignSecMarket: ZignSecMarket,
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
    errorText = null,
    label = {
      Text(
        stringResource(
          when (zignSecMarket) {
            ZignSecMarket.NO -> R.string.simple_sign_login_text_field_helper_text
            ZignSecMarket.DK -> R.string.simple_sign_login_text_field_helper_text_dk
          },
        ),
      )
    },
    placeholder = {
      Text(
        stringResource(
          when (zignSecMarket) {
            ZignSecMarket.NO -> R.string.simple_sign_login_text_field_label
            ZignSecMarket.DK -> R.string.simple_sign_login_text_field_label_dk
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
    singleLine = true,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
  )
}

@Composable
private fun ContinueButton(
  onClick: () -> Unit,
  isValidInput: Boolean?,
  isSubmitting: Boolean?,
  zignSecMarket: ZignSecMarket,
) {
  LargeContainedTextButton(
    text = stringResource(
      when (zignSecMarket) {
        ZignSecMarket.NO -> R.string.simple_sign_sign_in
        ZignSecMarket.DK -> R.string.simple_sign_sign_in_dk
      },
    ),
    onClick = onClick,
    enabled = isValidInput == true && isSubmitting != true,
    modifier = Modifier.padding(horizontal = 16.dp),
  )
}

private enum class ZignSecMarket {
  NO, DK
}
