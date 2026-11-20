package com.hedvig.android.feature.greencard.ui.importantinfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults
import com.hedvig.android.design.system.hedvig.RadioGroupSize
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import hedvig.resources.Res
import hedvig.resources.TERMINATION_FLOW_IMPORTANT_INFORMATION_TEXT
import hedvig.resources.TERMINATION_FLOW_IMPORTANT_INFORMATION_TITLE
import hedvig.resources.TERMINATION_FLOW_I_UNDERSTAND_TEXT
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GreenCardImportantInfoDestination(navigateUp: () -> Unit, onContinue: () -> Unit) {
  var isChecked by rememberSaveable { mutableStateOf(false) }
  GreenCardImportantInfoScreen(
    isChecked = isChecked,
    onCheckedChange = { isChecked = !isChecked },
    navigateUp = navigateUp,
    onContinue = onContinue,
  )
}

@Composable
private fun GreenCardImportantInfoScreen(
  isChecked: Boolean,
  onCheckedChange: () -> Unit,
  navigateUp: () -> Unit,
  onContinue: () -> Unit,
) {
  HedvigScaffold(navigateUp = navigateUp) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      stringResource(Res.string.TERMINATION_FLOW_IMPORTANT_INFORMATION_TITLE),
      null,
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    Column(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      InfoParagraph(
        // TODO: Add "You don't need a green card within the EU" / "Du behöver inget grönt kort inom EU" to Lokalise
        title = "You don't need a green card within the EU",
        // TODO: Add "Your insurance is valid in all EU and EEA countries without a green card." / "Din försäkring gäller i alla EU- och EES-länder utan grönt kort." to Lokalise
        text = "Your insurance is valid in all EU and EEA countries without a green card.",
      )
      Spacer(Modifier.height(16.dp))
      InfoParagraph(
        // TODO: Add "Some countries are not covered" / "Vissa länder omfattas inte" to Lokalise
        title = "Some countries are not covered",
        // TODO: Add "The green card isn't valid in Russia (RUS), Ukraine (UA), Belarus (BY) and Iran (IR)." / "Det gröna kortet gäller inte i Ryssland (RUS), Ukraina (UA), Belarus (BY) och Iran (IR)." to Lokalise
        text = "The green card isn't valid in Russia (RUS), Ukraine (UA), Belarus (BY) and Iran (IR).",
      )
      Spacer(Modifier.height(16.dp))
//      InfoParagraph(
//        // TODO: Add "The card must be printed on green paper" / "Kortet måste skrivas ut på grönt papper" to Lokalise
//        title = "The card must be printed on green paper",
//        // TODO: Add "We'll post a printed green card to the address you enter in the next step. If you need it sooner, you can print it yourself in colour, but remember that the paper must be green." / "Vi skickar ett tryckt grönt kort till adressen du anger i nästa steg. Behöver du det tidigare kan du skriva ut det själv i färg, men kom ihåg att papperet måste vara grönt." to Lokalise
//        text = "We'll post a printed green card to the address you enter in the next step. If you need it sooner, " +
//          "you can print it yourself in colour, but remember that the paper must be green.",
//      )
      InfoParagraph(
        // TODO: Add "The card must be printed on green paper" / "Kortet måste skrivas ut på grönt papper" to Lokalise
        title = "The digital version of the card is valid",
        // TODO: Add "We'll post a printed green card to the address you enter in the next step. If you need it sooner, you can print it yourself in colour, but remember that the paper must be green." / "Vi skickar ett tryckt grönt kort till adressen du anger i nästa steg. Behöver du det tidigare kan du skriva ut det själv i färg, men kom ihåg att papperet måste vara grönt." to Lokalise
        text = "We'll send it you in Kivra. You will also be able to download it here in app right away.",
      )
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(24.dp))
    ImportantInfoCheckBox(
      isChecked = isChecked,
      onCheckedChange = onCheckedChange,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = onContinue,
      enabled = isChecked,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun InfoParagraph(title: String, text: String) {
  Column {
    HedvigText(title)
    HedvigText(text, color = HedvigTheme.colorScheme.textSecondary)
  }
}

@Composable
private fun ImportantInfoCheckBox(isChecked: Boolean, onCheckedChange: () -> Unit, modifier: Modifier = Modifier) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        HedvigTheme(darkTheme = false) {
          Checkbox(
            option = CheckboxOption(
              text = stringResource(Res.string.TERMINATION_FLOW_I_UNDERSTAND_TEXT),
            ),
            selected = isChecked,
            onCheckboxSelected = onCheckedChange,
            size = RadioGroupSize.Small,
            colors = RadioGroupDefaults.colors.copy(containerColor = HedvigTheme.colorScheme.fillNegative),
          )
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewGreenCardImportantInfo() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      GreenCardImportantInfoScreen(
        isChecked = false,
        onCheckedChange = {},
        navigateUp = {},
        onContinue = {},
      )
    }
  }
}
