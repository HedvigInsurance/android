package com.hedvig.android.core.ui.genericinfo

import android.content.Context
import android.util.AttributeSet
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.information.AppStateInformationType
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun GenericErrorScreen(
  onRetryButtonClick: () -> Unit,
  modifier: Modifier = Modifier,
  title: String = stringResource(hedvig.resources.R.string.home_tab_error_title),
  description: String = stringResource(hedvig.resources.R.string.home_tab_error_body),
) {
  GenericInfoScreen(
    title = title,
    description = description,
    informationType = AppStateInformationType.Failure,
    primaryButtonText = stringResource(hedvig.resources.R.string.home_tab_error_button_text),
    onPrimaryButtonClick = onRetryButtonClick,
    secondaryButtonText = null,
    onSecondaryButtonClick = null,
    modifier = modifier,
  )
}

class GenericErrorScreenView @JvmOverloads constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyle: Int = 0,
) : AbstractComposeView(context, attrs, defStyle) {

  var onClick by mutableStateOf({})

  @Composable
  override fun Content() {
    HedvigTheme {
      GenericErrorScreen(onRetryButtonClick = onClick, Modifier.padding(top = 48.dp))
    }
  }
}
