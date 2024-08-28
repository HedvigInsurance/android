package com.hedvig.android.sample.design.showcase.accordion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.AccordionData
import com.hedvig.android.design.system.hedvig.AccordionDefaults
import com.hedvig.android.design.system.hedvig.AccordionList
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface

@Composable
fun AccordionShowCase() {
  Surface(
    modifier = Modifier
      .fillMaxSize()
      .safeContentPadding(),
    color = HedvigTheme.colorScheme.backgroundPrimary,
  ) {
    Column(
      Modifier
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
    ) {
      AccordionList(
        items = List(4) { index ->
          AccordionData(description = "lalalalalala llala", title = "Label $index")
        },
        size = AccordionDefaults.Size.Large,
      )
      Spacer(Modifier.height(32.dp))
      AccordionList(
        items = List(4) { index ->
          AccordionData(description = "lalalalalala", title = "Label $index")
        },
        size = AccordionDefaults.Size.Small,
      )
    }
  }
}
