package com.hedvig.android.ui.emergency

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.material3.alwaysBlackContainer
import com.hedvig.android.core.designsystem.material3.onAlwaysBlackContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.Hedvig
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.infocard.VectorWarningCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.R

@Composable
fun EmergencyScreen(
  emergencyNumber: String?,
  emergencyUrl: String?,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigScaffold(
    topAppBarText = stringResource(id = R.string.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE),
    navigateUp = navigateUp,
    modifier = modifier,
  ) {
    Spacer(Modifier.height(8.dp))
    VectorWarningCard(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INFO_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigCard(
      colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.alwaysBlackContainer,
        contentColor = MaterialTheme.colorScheme.onAlwaysBlackContainer,
      ),
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth(),
    ) {
      HedvigTheme(darkTheme = true) {
        Column(Modifier.padding(16.dp)) {
          Spacer(Modifier.height(16.dp))
          Icon(
            imageVector = Icons.Hedvig.Hedvig,
            contentDescription = null,
            modifier = Modifier
              .fillMaxWidth()
              .height(80.dp),
          )
          Spacer(Modifier.height(24.dp))
          Text(
            text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_GLOBAL_ASSISTANCE_TITLE),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
          Spacer(Modifier.height(2.dp))
          Text(
            text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_GLOBAL_ASSISTANCE_LABEL),
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(
              lineBreak = LineBreak.Heading,
              color = LocalContentColor.current.copy(alpha = 0.7f),
            ),
            modifier = Modifier.fillMaxWidth(),
          )
          if (emergencyUrl != null) {
            Spacer(Modifier.height(24.dp))
            HedvigContainedSmallButton(
              text = stringResource(
                R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_URL_LABEL,
                emergencyUrl,
              ),
              onClick = {
                openUrl(emergencyUrl)
              },
              modifier = Modifier.fillMaxWidth(),
            )
          }
          if (emergencyNumber != null) {
            val colors = if (emergencyUrl != null) {
              ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onAlwaysBlackContainer,
              )
            } else {
              ButtonDefaults.buttonColors()
            }
            Spacer(Modifier.height(8.dp))
            val context = LocalContext.current
            HedvigContainedSmallButton(
              text = stringResource(
                R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_CALL_LABEL,
                emergencyNumber,
              ),
              colors = colors,
              onClick = {
                try {
                  context.startActivity(
                    Intent(
                      Intent.ACTION_DIAL,
                      Uri.parse("tel:$emergencyNumber"),
                    ),
                  )
                } catch (exception: Throwable) {
                  logcat(LogPriority.ERROR, exception) {
                    "Could not open dial activity in deflect emergency destination"
                  }
                }
              },
              modifier = Modifier.fillMaxWidth(),
            )
          }
          Spacer(Modifier.height(16.dp))
          Text(
            text = stringResource(R.string.SUBMIT_CLAIM_GLOBAL_ASSISTANCE_FOOTNOTE),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.bodySmall.copy(
              color = LocalContentColor.current.copy(alpha = 0.7f),
            ),
          )
          Spacer(Modifier.height(8.dp))
        }
      }
    }
    Spacer(Modifier.height(24.dp))
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INSURANCE_COVER_TITLE),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    Text(
      text = stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_INSURANCE_COVER_LABEL),
      modifier = Modifier.padding(horizontal = 16.dp),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(24.dp))
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(24.dp))
    QuestionsAndAnswers(Modifier.padding(horizontal = 16.dp))
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun QuestionsAndAnswers(modifier: Modifier = Modifier) {
  var expandedItem by rememberSaveable { mutableIntStateOf(-1) }

  val faqList = listOf(
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ1_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ1_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ2_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ2_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ3_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ3_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ4_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ4_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ5_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ5_LABEL),
    stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ6_TITLE) to
      stringResource(R.string.SUBMIT_CLAIM_EMERGENCY_FAQ6_LABEL),
  )
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    faqList.forEachIndexed { index, faqItem ->
      ExpandablePlusCard(
        isExpanded = expandedItem == index,
        onClick = {
          expandedItem = if (expandedItem == index) {
            -1
          } else {
            index
          }
        },
        titleText = faqItem.first,
        expandedText = faqItem.second,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewEmergencyScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      EmergencyScreen(
        emergencyNumber = "123456",
        emergencyUrl = "url",
        navigateUp = {},
        openUrl = {},
      )
    }
  }
}
