package com.hedvig.app.feature.marketing.pickmarket

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.RadioButton
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
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.hedvig.app.R
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.ui.compose.theme.separator
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
  val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

  val insets = LocalWindowInsets.current
  val navigationBarHeight = with(LocalDensity.current) { insets.navigationBars.bottom.toDp() }
  ModalBottomSheetLayout(
    sheetState = modalBottomSheetState,
    sheetContent = {
      HedvigTheme {
        BackHandler(
          enabled = modalBottomSheetState.isVisible,
          onBack = {
            coroutineScope.launch { modalBottomSheetState.hide() }
          },
        )
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
        Spacer(Modifier.height(24.dp + navigationBarHeight))
      }
    },
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
        text = stringResource(R.string.market_language_screen_title),
        style = MaterialTheme.typography.h4,
        textAlign = TextAlign.Center,
        modifier = Modifier.align(Alignment.Center),
      )
      Column(
        modifier = Modifier.align(Alignment.BottomCenter),
      ) {
        PickerRow(
          onClick = {
            sheet = PickMarketSheet.MARKET
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
          header = stringResource(R.string.market_language_screen_market_label),
          label = selectedMarket?.label?.let { stringResource(it) },
          enabled = enabled,
        )
        PickerRow(
          onClick = {
            sheet = PickMarketSheet.COUNTRY
            coroutineScope.launch { modalBottomSheetState.show() }
          },
          icon = {
            LanguageFlag()
          },
          header = stringResource(R.string.market_language_screen_language_label),
          label = selectedLanguage?.getLabel()?.let { stringResource(it) },
          enabled = selectedMarket?.let { Language.getAvailableLanguages(it).isNotEmpty() } ?: false,
        )
        Spacer(Modifier.height(32.dp))
        LargeContainedButton(
          onClick = onSubmit,
          modifier = Modifier.padding(horizontal = 16.dp),
          enabled = true,
        ) {
          Text(stringResource(R.string.market_language_screen_continue_button_text))
        }
        Spacer(Modifier.height(24.dp + navigationBarHeight))
      }
    }
  }
}

@Composable
private fun LanguageFlag() {
  Image(
    painter = painterResource(R.drawable.ic_language),
    contentDescription = null,
    colorFilter = tint(LocalContentColor.current),
  )
}

@Composable
fun BottomSheetHandle(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(width = 32.dp, height = 4.dp)
      .background(
        color = MaterialTheme.colors.separator,
        shape = RoundedCornerShape(20.dp),
      ),
  )
}

@Composable
fun PickMarketSheetContent(
  onSelectMarket: (Market) -> Unit,
  selectedMarket: Market?,
  markets: List<Market>,
) {
  Spacer(Modifier.height(24.dp))
  Text(
    text = stringResource(R.string.market_picker_modal_title),
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
fun PickLanguageSheetContent(
  onSelectLanguage: (Language) -> Unit,
  selectedLanguage: Language?,
  selectedMarket: Market?,
) {
  if (selectedMarket == null) {
    return
  }
  Spacer(Modifier.height(24.dp))
  Text(
    text = stringResource(R.string.language_picker_modal_title),
    modifier = Modifier.padding(horizontal = 16.dp),
    style = MaterialTheme.typography.h5,
  )
  Spacer(Modifier.height(16.dp))
  Text(
    text = stringResource(R.string.language_picker_modal_text),
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
fun RadioButtonRow(onClick: () -> Unit, selected: Boolean, text: String) {
  Row(
    modifier = Modifier
      .clickable(onClick = onClick)
      .fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RadioButton(selected = selected, onClick = onClick)
    Spacer(Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.body1,
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

@Composable
fun PickerRow(
  onClick: () -> Unit,
  icon: (@Composable () -> Unit)?,
  header: String?,
  label: String?,
  enabled: Boolean,
) {
  Row(
    modifier = Modifier
      .clickable(enabled = enabled, onClick = onClick)
      .padding(vertical = 8.dp),
  ) {
    Spacer(Modifier.width(16.dp))
    if (icon != null) {
      Box(modifier = Modifier.align(Alignment.CenterVertically)) {
        icon()
      }
    } else {
      Spacer(Modifier.width(24.dp))
    }
    Spacer(Modifier.width(16.dp))
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.SpaceAround,

    ) {
      Text(
        text = header ?: "",
      )
      Text(
        text = label ?: "",
        style = MaterialTheme.typography.caption,
      )
    }
    Spacer(Modifier.weight(1f))
    Image(
      painter = painterResource(R.drawable.ic_arrow_forward),
      contentDescription = null,
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
