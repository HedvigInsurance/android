package com.hedvig.app.feature.marketing.pickmarket

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.BottomSheetHandle
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.market.Language
import com.hedvig.android.market.Market
import com.hedvig.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class PickMarketSheet {
  MARKET,
  COUNTRY,
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PickMarketScreen(
  onSubmit: () -> Unit,
  onSelectMarket: (Market) -> Unit,
  onSelectLanguage: (Language) -> Unit,
  selectedMarket: Market?,
  selectedLanguage: Language?,
  markets: List<Market>,
  enabled: Boolean,
) {
  var sheet by rememberSaveable { mutableStateOf<PickMarketSheet?>(null) }
  val coroutineScope = rememberCoroutineScope()
  val modalBottomSheetState = rememberModalBottomSheetState(
    initialValue = ModalBottomSheetValue.Hidden,
    skipHalfExpanded = true,
  )

  ModalBottomSheetLayout(
    sheetState = modalBottomSheetState,
    sheetContent = {
      HedvigTheme { // Use standard theme again inside the sheet.
        BottomSheetContent(
          modalBottomSheetState = modalBottomSheetState,
          coroutineScope = coroutineScope,
          sheet = sheet,
          onSelectMarket = onSelectMarket,
          selectedMarket = selectedMarket,
          markets = markets,
          onSelectLanguage = onSelectLanguage,
          selectedLanguage = selectedLanguage,
          modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal),
          ),
        )
      }
    },
  ) {
    ScreenContent(
      setSheet = { sheet = it },
      coroutineScope = coroutineScope,
      modalBottomSheetState = modalBottomSheetState,
      selectedMarket = selectedMarket,
      selectedLanguage = selectedLanguage,
      onSubmit = onSubmit,
      enabled = enabled,
      modifier = Modifier.safeDrawingPadding(),
    )
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ScreenContent(
  setSheet: (PickMarketSheet?) -> Unit,
  coroutineScope: CoroutineScope,
  modalBottomSheetState: ModalBottomSheetState,
  selectedMarket: Market?,
  selectedLanguage: Language?,
  onSubmit: () -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier,
) {
  Box(modifier.fillMaxSize()) {
    Text(
      text = stringResource(hedvig.resources.R.string.market_language_screen_title),
      style = MaterialTheme.typography.h4,
      textAlign = TextAlign.Center,
      modifier = Modifier.align(Alignment.Center),
    )
    Column(
      modifier = Modifier.align(Alignment.BottomCenter),
    ) {
      PickerRow(
        onClick = {
          setSheet(PickMarketSheet.MARKET)
          coroutineScope.launch { modalBottomSheetState.show() }
        },
        icon = selectedMarket?.flag?.let { flagId ->
          {
            Image(
              painter = painterResource(flagId),
              contentDescription = null,
            )
          }
        },
        header = stringResource(hedvig.resources.R.string.market_language_screen_market_label),
        label = selectedMarket?.label?.let { stringResource(it) },
        enabled = true,
      )
      PickerRow(
        onClick = {
          setSheet(PickMarketSheet.COUNTRY)
          coroutineScope.launch { modalBottomSheetState.show() }
        },
        icon = {
          LanguageFlag()
        },
        header = stringResource(hedvig.resources.R.string.market_language_screen_language_label),
        label = selectedLanguage?.getLabel()?.let { stringResource(it) },
        enabled = selectedMarket?.let { Language.getAvailableLanguages(it).isNotEmpty() } ?: false,
      )
      Spacer(Modifier.height(32.dp))
      LargeContainedButton(
        onClick = onSubmit,
        modifier = Modifier.padding(horizontal = 16.dp),
        enabled = enabled,
      ) {
        Text(stringResource(hedvig.resources.R.string.market_language_screen_continue_button_text))
      }
      Spacer(Modifier.height(24.dp))
    }
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun BottomSheetContent(
  modalBottomSheetState: ModalBottomSheetState,
  coroutineScope: CoroutineScope,
  sheet: PickMarketSheet?,
  onSelectMarket: (Market) -> Unit,
  selectedMarket: Market?,
  markets: List<Market>,
  onSelectLanguage: (Language) -> Unit,
  selectedLanguage: Language?,
  modifier: Modifier = Modifier,
) {
  BackHandler(
    enabled = modalBottomSheetState.isVisible,
    onBack = {
      coroutineScope.launch { modalBottomSheetState.hide() }
    },
  )
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    BottomSheetHandle(modifier = Modifier.align(Alignment.CenterHorizontally))
    when (sheet) {
      PickMarketSheet.MARKET -> PickMarketSheetContent(
        onSelectMarket = { market ->
          coroutineScope.launch {
            modalBottomSheetState.hide()
            onSelectMarket(market)
          }
        },
        selectedMarket = selectedMarket,
        markets = markets,
      )
      PickMarketSheet.COUNTRY -> PickLanguageSheetContent(
        onSelectLanguage = { language ->
          coroutineScope.launch {
            modalBottomSheetState.hide()
            onSelectLanguage(language)
          }
        },
        selectedLanguage = selectedLanguage,
        selectedMarket = selectedMarket,
      )
      null -> {}
    }
    Spacer(Modifier.height(24.dp))
  }
}

@Composable
private fun LanguageFlag() {
  Icon(
    painter = painterResource(R.drawable.ic_language),
    contentDescription = null,
  )
}

@Composable
private fun PickMarketSheetContent(
  onSelectMarket: (Market) -> Unit,
  selectedMarket: Market?,
  markets: List<Market>,
) {
  Spacer(Modifier.height(24.dp))
  Text(
    text = stringResource(hedvig.resources.R.string.market_picker_modal_title),
    modifier = Modifier.padding(horizontal = 16.dp),
    style = MaterialTheme.typography.h5,
  )
  Spacer(Modifier.height(8.dp))
  markets.forEach { market ->
    RadioButtonRow(
      onClick = { onSelectMarket(market) },
      selected = selectedMarket == market,
      text = stringResource(market.label),
    )
  }
}

@Composable
private fun PickLanguageSheetContent(
  onSelectLanguage: (Language) -> Unit,
  selectedLanguage: Language?,
  selectedMarket: Market?,
) {
  if (selectedMarket == null) {
    return
  }
  Spacer(Modifier.height(24.dp))
  Text(
    text = stringResource(hedvig.resources.R.string.language_picker_modal_title),
    modifier = Modifier.padding(horizontal = 16.dp),
    style = MaterialTheme.typography.h5,
  )
  Spacer(Modifier.height(16.dp))
  Text(
    text = stringResource(hedvig.resources.R.string.language_picker_modal_text),
    modifier = Modifier.padding(horizontal = 16.dp),
    style = MaterialTheme.typography.body1,
  )
  Language.getAvailableLanguages(selectedMarket).forEach { language ->
    RadioButtonRow(
      onClick = { onSelectLanguage(language) },
      selected = selectedLanguage == language,
      text = stringResource(language.getLabel()),
    )
  }
}

@Composable
private fun RadioButtonRow(onClick: () -> Unit, selected: Boolean, text: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .height(48.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Spacer(Modifier.width(4.dp))
    RadioButton(
      selected = selected,
      onClick = onClick,
      colors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colors.onSurface,
      ),
    )
    Spacer(Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.body1,
    )
  }
}

@Composable
private fun PickerRow(
  onClick: () -> Unit,
  icon: (@Composable () -> Unit)?,
  header: String,
  label: String?,
  enabled: Boolean,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .height(56.dp)
      .clickable(enabled = enabled, onClick = onClick)
      .padding(horizontal = 16.dp),
  ) {
    Box(Modifier.size(24.dp)) {
      if (icon != null) {
        icon()
      }
    }
    Spacer(Modifier.width(16.dp))
    Column(
      verticalArrangement = Arrangement.SpaceAround,
    ) {
      Text(header)
      if (label != null) {
        Text(
          text = label,
          style = MaterialTheme.typography.caption,
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Icon(
      painter = painterResource(R.drawable.ic_arrow_forward),
      contentDescription = null,
    )
  }
}

@Preview(showBackground = true)
@Composable
fun RadioButtonRowPreview() {
  HedvigTheme {
    RadioButtonRow(
      onClick = {},
      selected = false,
      text = "Sweden",
    )
  }
}

@Preview(showBackground = true)
@Composable
fun PickerRowPreviewEmpty() {
  HedvigTheme {
    PickerRow(onClick = {}, icon = null, header = "asd", label = "efg", enabled = false)
  }
}

@Preview(showBackground = true)
@Composable
fun PickerRowPreview() {
  HedvigTheme {
    PickerRow(onClick = {}, icon = { LanguageFlag() }, header = "asd", label = "efg", enabled = false)
  }
}

@Preview(showBackground = true)
@Composable
fun PickMarketPreview() {
  HedvigTheme {
    PickMarketScreen(
      onSubmit = {},
      onSelectMarket = {},
      onSelectLanguage = {},
      selectedMarket = Market.SE,
      selectedLanguage = Language.SV_SE,
      markets = emptyList(),
      enabled = true,
    )
  }
}
