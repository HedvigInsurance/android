package com.hedvig.app.feature.crossselling.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigOutlinedTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.R
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CrossSellingResultScreen(
  crossSellingResult: CrossSellingResult,
  clock: Clock,
  dateFormatter: DateTimeFormatter,
  openChat: () -> Unit,
  closeResultScreen: () -> Unit,
) {
  HedvigTheme {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
    ) {
      InformationSection(
        crossSellingResult = crossSellingResult,
        clock = clock,
        dateFormatter = dateFormatter,
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(top = 96.dp)
          .fillMaxWidth(),
      )
      ButtonsSection(
        crossSellingResult = crossSellingResult,
        openChat = openChat,
        closeResultScreen = closeResultScreen,
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(bottom = 16.dp)
          .fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun InformationSection(
  crossSellingResult: CrossSellingResult,
  clock: Clock,
  dateFormatter: DateTimeFormatter,
  modifier: Modifier,
) {
  val icon: Painter = when (crossSellingResult) {
    is CrossSellingResult.Success -> painterResource(
      com.hedvig.android.core.design.system.R.drawable.ic_checkmark_in_circle,
    )
    CrossSellingResult.Error -> painterResource(R.drawable.ic_x_in_circle)
  }
  val titleText = when (crossSellingResult) {
    CrossSellingResult.Error -> stringResource(hedvig.resources.R.string.purchase_confirmation_error_title)
    is CrossSellingResult.Success -> {
      stringResource(
        hedvig.resources.R.string.purchase_confirmation_new_insurance_today_app_state_title,
        crossSellingResult.insuranceType,
      )
    }
  }
  val subtitleText = when (crossSellingResult) {
    CrossSellingResult.Error -> stringResource(hedvig.resources.R.string.purchase_confirmation_error_subtitle)
    is CrossSellingResult.Success -> {
      when {
        crossSellingResult.startingDate <= LocalDate.now(clock) -> {
          stringResource(
            hedvig.resources.R.string.purchase_confirmation_new_insurance_today_app_state_description,
            crossSellingResult.insuranceType,
          )
        }
        else -> {
          val activationDate = crossSellingResult.startingDate.format(dateFormatter)
          stringResource(
            hedvig.resources.R.string.purchase_confirmation_new_insurance_active_in_future_app_state_description,
            crossSellingResult.insuranceType,
            activationDate,
          )
        }
      }
    }
  }
  InformationSection(icon, titleText, subtitleText, modifier)
}

@Composable
private fun InformationSection(
  icon: Painter,
  title: String,
  description: String,
  modifier: Modifier,
) {
  Column(
    modifier = modifier,
  ) {
    Icon(
      painter = icon,
      contentDescription = null,
      modifier = Modifier.size(32.dp),
    )
    Spacer(Modifier.height(20.dp))
    Text(
      title,
      style = MaterialTheme.typography.headlineSmall,
    )
    Spacer(Modifier.height(24.dp))
    Text(
      description,
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@Composable
private fun ButtonsSection(
  crossSellingResult: CrossSellingResult,
  openChat: () -> Unit,
  closeResultScreen: () -> Unit,
  modifier: Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    when (crossSellingResult) {
      is CrossSellingResult.Error -> {
        HedvigContainedButton(
          onClick = openChat,
        ) {
          Icon(
            painter = painterResource(R.drawable.ic_chat_on_primary),
            contentDescription = null,
          )
          Spacer(Modifier.width(8.dp))
          Text(stringResource(hedvig.resources.R.string.purchase_confirmation_error_open_chat_button))
        }
        HedvigOutlinedTextButton(
          onClick = closeResultScreen,
          text = stringResource(hedvig.resources.R.string.purchase_confirmation_error_close_button),
        )
      }
      is CrossSellingResult.Success -> {
        HedvigContainedButton(
          onClick = closeResultScreen,
          text = stringResource(hedvig.resources.R.string.purchase_confirmation_accident_insurance_done_button),
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewCrossSellingResultScreen(
  @PreviewParameter(ActivityResultProvider::class) crossSellingResult: CrossSellingResult,
) {
  HedvigTheme {
    Surface {
      CrossSellingResultScreen(
        crossSellingResult,
        Clock.systemDefaultZone(),
        DateTimeFormatter.ISO_LOCAL_DATE,
        {},
        {},
      )
    }
  }
}

private class ActivityResultProvider : CollectionPreviewParameterProvider<CrossSellingResult>(
  listOf(
    CrossSellingResult.Error,
    CrossSellingResult.Success(LocalDate.now(), "Accident Insurance"),
    CrossSellingResult.Success(LocalDate.now().plusDays(2), "Accident Insurance"),
  ),
)
