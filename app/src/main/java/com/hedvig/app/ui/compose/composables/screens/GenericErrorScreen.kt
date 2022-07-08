package com.hedvig.app.ui.compose.composables.screens

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.HedvigTheme

class GenericErrorScreenView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0
) : AbstractComposeView(context, attrs, defStyle) {

  var onClick by mutableStateOf({})

  @Composable
  override fun Content() {
    HedvigTheme {
      GenericErrorScreen(onRetryButtonClicked = onClick)
    }
  }
}

@Composable
fun GenericErrorScreen(
  modifier: Modifier = Modifier,
  title: String = stringResource(id = R.string.home_tab_error_title),
  description: String = stringResource(id = R.string.home_tab_error_body),
  onRetryButtonClicked: () -> Unit,
) {
  GenericInfoScreen(
    modifier = modifier,
    title = title,
    description = description,
    icon = R.drawable.ic_warning_triangle,
    primaryButtonText = stringResource(id = R.string.home_tab_error_button_text),
    onPrimaryButtonClicked = onRetryButtonClicked,
    onSecondaryButtonClicked = null,
  )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GenericErrorScreenPreview() {
  HedvigTheme {
    Surface(
      color = MaterialTheme.colors.background,
    ) {
      GenericErrorScreen(onRetryButtonClicked = {})
    }
  }
}
