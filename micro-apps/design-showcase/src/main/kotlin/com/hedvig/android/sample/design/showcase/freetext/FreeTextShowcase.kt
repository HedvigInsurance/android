package com.hedvig.android.sample.design.showcase.freetext

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Height
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplayDefaults.Style
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay

@Composable
fun FreeTextShowcase() {
  var isFreeTextVisible by rememberSaveable { mutableStateOf(false) }
  var textValue1 by rememberSaveable { mutableStateOf<String?>(null) }
  var textValue2 by rememberSaveable { mutableStateOf<String?>(null) }
  var textValue3 by rememberSaveable { mutableStateOf<String?>(null) }
  var chosenOption by rememberSaveable { mutableStateOf<Int?>(null) }
  val maxLength = 200
  FreeTextOverlay(
    freeTextMaxLength = maxLength,
    freeTextValue =
      if (chosenOption != null) {
        if (chosenOption == 1) {
          textValue1 ?: ""
        } else if (chosenOption == 2) {
          textValue2 ?: ""
        } else if (chosenOption == 3) {
          textValue3 ?: ""
        } else {
          ""
        }
      } else {
        ""
      },
    freeTextHint = "Please let us know more",
    freeTextOnCancelClick = {
      isFreeTextVisible = false
    },
    freeTextOnSaveClick = { s ->
      if (chosenOption != null) {
        if (chosenOption == 1) {
          textValue1 = s
        } else if (chosenOption == 2) {
          textValue2 = s
        } else if (chosenOption == 3) {
          textValue3 = s
        }
      }

      isFreeTextVisible = false
    },
    shouldShowOverlay = isFreeTextVisible,
    overlaidContent = {
      Surface(
        modifier = Modifier
          .fillMaxSize(),
        color = HedvigTheme.colorScheme.backgroundPrimary,
      ) {
        Column(
          modifier = Modifier
            .safeContentPadding()
            .fillMaxSize(),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Spacer(Modifier.height(16.dp))
          FreeTextDisplay(
            modifier = Modifier.padding(16.dp),
            onClick = {
              chosenOption = 1
              isFreeTextVisible = true
            },
            style = Style.Default,
            height = Height.Unlimited,
            maxLength = maxLength,
            hasError = true,
            supportingText = "Enter your feedback!",
            freeTextValue = textValue1,
            freeTextPlaceholder = "Tell us what you think. Unlimited height with error",
          )
          Spacer(Modifier.height(16.dp))
          FreeTextDisplay(
            modifier = Modifier.padding(16.dp),
            onClick = {
              chosenOption = 2
              isFreeTextVisible = true
            },
            style = Style.Default,
            height = Height.Limited(),
            maxLength = maxLength,
            hasError = false,
            freeTextValue = textValue2,
            freeTextPlaceholder = "Tell us what you think. Limited height no error",
          )
          Spacer(Modifier.height(16.dp))
          FreeTextDisplay(
            modifier = Modifier.padding(16.dp),
            onClick = {
              chosenOption = 3
              isFreeTextVisible = true
            },
            style = Style.Labeled("New label"),
            height = Height.Limited(),
            maxLength = maxLength,
            hasError = false,
            freeTextValue = textValue3,
            freeTextPlaceholder = "Tell us what you think. Limited height no error with label",
          )
        }
      }
    },
  )
}
