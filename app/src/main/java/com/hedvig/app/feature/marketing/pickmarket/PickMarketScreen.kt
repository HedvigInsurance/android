package com.hedvig.app.feature.marketing.pickmarket

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.hedvig.app.R
import com.hedvig.app.feature.marketing.PickMarket
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.ui.compose.composables.buttons.LargeContainedButton
import com.hedvig.app.ui.compose.theme.HedvigTheme
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
    data: PickMarket
) {
    var sheet by rememberSaveable { mutableStateOf<PickMarketSheet?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    val insets = LocalWindowInsets.current
    val navigationBarHeight = with(LocalDensity.current) { insets.navigationBars.bottom.toDp() }
    ModalBottomSheetLayout(
        sheetState = modalBottomSheetState,
        sheetContent = {
            BackHandler(
                enabled = modalBottomSheetState.isVisible,
                onBack = {
                    coroutineScope.launch { modalBottomSheetState.hide() }
                },
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(width = 32.dp, height = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(
                        color = colorResource(R.color.color_divider),
                        shape = RoundedCornerShape(20.dp)
                    )

            )
            when (sheet) {
                PickMarketSheet.MARKET -> PickMarketSheetContent(
                    onSelectMarket = { market ->
                        coroutineScope.launch {
                            modalBottomSheetState.hide()
                            onSelectMarket(market)
                        }
                    },
                    data = data
                )
                PickMarketSheet.COUNTRY -> PickLanguageSheetContent(
                    onSelectLanguage = { language ->
                        coroutineScope.launch {
                            modalBottomSheetState.hide()
                            onSelectLanguage(language)
                        }
                    },
                    data = data
                )
                null -> {}
            }
            Spacer(Modifier.height(24.dp + navigationBarHeight))
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
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                PickerRow(
                    onClick = {
                        sheet = PickMarketSheet.MARKET
                        coroutineScope.launch { modalBottomSheetState.show() }
                    },
                    icon = data.market?.flag,
                    header = stringResource(R.string.market_language_screen_market_label),
                    label = data.market?.label?.let { stringResource(it) },
                )
                PickerRow(
                    onClick = {
                        sheet = PickMarketSheet.COUNTRY
                        coroutineScope.launch { modalBottomSheetState.show() }
                    },
                    icon = R.drawable.ic_language,
                    header = stringResource(R.string.market_language_screen_language_label),
                    label = data.language?.getLabel()?.let { stringResource(it) },
                )
                Spacer(Modifier.height(32.dp))
                LargeContainedButton(
                    onClick = onSubmit,
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Text(stringResource(R.string.market_language_screen_continue_button_text))
                }
                Spacer(Modifier.height(24.dp + navigationBarHeight))
            }
        }
    }
}

@Composable
fun PickMarketSheetContent(
    onSelectMarket: (Market) -> Unit,
    data: PickMarket,
) {
    Spacer(Modifier.height(24.dp))
    Text(
        text = stringResource(R.string.market_picker_modal_title),
        modifier = Modifier.padding(horizontal = 16.dp),
        style = MaterialTheme.typography.h5,
    )
    Spacer(Modifier.height(8.dp))
    data.availableMarkets.forEach { market ->
        RadioButtonRow(
            onClick = { onSelectMarket(market) },
            selected = data.market == market,
            text = stringResource(market.label),
        )
    }
}

@Composable
fun PickLanguageSheetContent(
    onSelectLanguage: (Language) -> Unit,
    data: PickMarket,
) {
    val market = data.market ?: return
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
    Language.getAvailableLanguages(market).forEach { language ->
        RadioButtonRow(
            onClick = { onSelectLanguage(language) },
            selected = data.language == language,
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
    @DrawableRes icon: Int?,
    header: String?,
    label: String?,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Spacer(Modifier.width(16.dp))
        icon?.let { ic ->
            Image(
                painter = painterResource(id = ic),
                contentDescription = null,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        } ?: Spacer(Modifier.width(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceAround,

        ) {
            Text(text = header ?: "")
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
fun PickMarketPreview() {
    HedvigTheme {
        PickMarketScreen(
            onSubmit = {},
            onSelectMarket = {},
            onSelectLanguage = {},
            data = PickMarket(
                isLoading = false,
                isValid = false,
                market = Market.SE,
                language = null,
                availableMarkets = emptyList(),
            ),
        )
    }
}
