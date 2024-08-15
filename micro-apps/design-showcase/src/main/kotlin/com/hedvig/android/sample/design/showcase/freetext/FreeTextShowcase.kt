package com.hedvig.android.sample.design.showcase.freetext

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay

@Composable
fun FreeTextShowcase() {

  var isFreeTextVisible by rememberSaveable { mutableStateOf(false) }
  var textValue by rememberSaveable { mutableStateOf<String?>(null) }

  FreeTextOverlay(
      freeTextValue = textValue,
      freeTextHint = "Please let us know more",
      freeTextOnCancelClick = {
          isFreeTextVisible = false
      },
      freeTextOnSaveClick = { s ->
          textValue = s
          isFreeTextVisible = false
      },
      shouldShowOverlay = isFreeTextVisible,
      overlaidContent = {
          Surface(
              modifier = Modifier
                  .fillMaxSize(),
          ) {
              Column(
                  modifier = Modifier
                      .safeContentPadding()
                      .fillMaxSize(),
                  horizontalAlignment = Alignment.CenterHorizontally,
              ) {
                  Spacer(Modifier.height(16.dp))
                  HedvigButton(
                      enabled = true,
                      onClick = {
                          isFreeTextVisible = true
                      },
                  ) {
                      HedvigText("Open free text overlay")
                  }
              }
          }
      },
  )
}
