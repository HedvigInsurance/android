package com.hedvig.android.core.designsystem.component.information

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import hedvig.resources.R

@Composable
fun HedvigInformationSection(
  title: String,
  modifier: Modifier = Modifier,
  buttonText: String = stringResource(id = R.string.ALERT_OK),
  onButtonClick: (() -> Unit)? = null,
  subTitle: String? = null,
  contentPadding: PaddingValues = WindowInsets.safeDrawing.asPaddingValues(),
  withDefaultVerticalSpacing: Boolean = true,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .padding(contentPadding)
      .padding(horizontal = 16.dp),
  ) {
    if (withDefaultVerticalSpacing) {
      Spacer(Modifier.height(32.dp))
    }
    Icon(
      imageVector = Icons.Hedvig.InfoFilled,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.infoElement,
    )
    Spacer(Modifier.height(16.dp))
    Text(
      text = title,
      textAlign = TextAlign.Center,
      style = LocalTextStyle.current.copy(
        lineBreak = LineBreak.Heading,
      ),
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(2.dp))
    if (subTitle != null) {
      Text(
        text = subTitle,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    if (onButtonClick != null) {
      Spacer(Modifier.height(24.dp))
      HedvigContainedSmallButton(
        text = buttonText,
        onClick = onButtonClick,
      )
    }
    if (withDefaultVerticalSpacing) {
      Spacer(Modifier.height(32.dp))
    }
  }
}

@HedvigPreview
@Composable
private fun HedvigInformationSectionPreview() {
  HedvigTheme {
    Surface {
      HedvigInformationSection(
        title = "Test",
        buttonText = "Click",
        onButtonClick = { },
      )
    }
  }
}
