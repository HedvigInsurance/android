package com.hedvig.android.feature.changeaddress.destination

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.card.ExpandablePlusCard
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.ui.QuoteCard
import com.hedvig.android.feature.changeaddress.ui.offer.Faqs
import hedvig.resources.R
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode

@Composable
internal fun ChangeAddressOfferDestination(
  viewModel: ChangeAddressViewModel,
  openChat: () -> Unit,
  close: () -> Unit,
  onChangeAddressResult: (String?) -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val moveResult = uiState.successfulMoveResult

  LaunchedEffect(moveResult) {
    if (moveResult != null) {
      onChangeAddressResult(uiState.movingDate.input?.toString())
    }
  }
  ChangeAddressOfferScreen(
    uiState = uiState,
    openChat = openChat,
    close = close,
    onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
    onExpandQuote = viewModel::onExpandQuote,
    onConfirmMove = viewModel::onConfirmMove,
  )
}

@Composable
private fun ChangeAddressOfferScreen(
  uiState: ChangeAddressUiState,
  openChat: () -> Unit,
  close: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  onExpandQuote: (MoveQuote) -> Unit,
  onConfirmMove: (MoveIntentId) -> Unit,
) {
  val moveIntentId = uiState.moveIntentId ?: throw IllegalArgumentException("No moveIntentId found!")

  if (uiState.errorMessage != null) {
    ErrorDialog(
      message = uiState.errorMessage,
      onDismiss = onErrorDialogDismissed,
    )
  }

  val scrollState = rememberScrollState()
  HedvigScaffold(
    topAppBarText = "Summary",
    navigateUp = close,
    topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    topAppBarActionType = TopAppBarActionType.CLOSE,
    scrollState = scrollState,
  ) {
    Spacer(Modifier.height(8.dp))
    for (quote in uiState.quotes) {
      QuoteCard(
        movingDate = uiState.movingDate.input?.toString(),
        quote = quote,
        onExpandClicked = { onExpandQuote(quote) },
        isExpanded = quote.isExpanded,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
    if (uiState.quotes.size > 1) {
      QuotesPriceSum(
        quotes = uiState.quotes,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(32.dp))
    HedvigContainedButton(
      text = stringResource(R.string.CHANGE_ADDRESS_ACCEPT_OFFER),
      onClick = { onConfirmMove(moveIntentId) },
      isLoading = uiState.isLoading,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    val coroutineScope = rememberCoroutineScope()
    var whatsIncludedButtonPositionY by remember { mutableStateOf(0f) }
    HedvigTextButton(
      text = "Se vad som ingår",
      onClick = {
        coroutineScope.launch {
          scrollState.animateScrollTo(
            value = whatsIncludedButtonPositionY.toInt(),
            animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
          )
        }
      },
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .onGloballyPositioned { layoutCoordinates ->
          // Find the Y position where this button ends, to scroll right below it on click.
          whatsIncludedButtonPositionY =
            layoutCoordinates.positionInParent().y + layoutCoordinates.size.height
        },
    )
    Spacer(Modifier.height(80.dp))
    for (quote in uiState.quotes) {
      QuoteDetailsAndPdfs(
        quote = quote,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(80.dp))
      CoverageItems(
        quote = quote,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(80.dp))
    }
    Faqs(
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(80.dp))
    Text(
      text = "Hittar du inte det du söker?",
      fontSize = 18.sp,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(24.dp))
    Button(
      shape = MaterialTheme.shapes.squircleMedium,
      onClick = { openChat() },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .wrapContentWidth(),
    ) {
      Text(
        text = stringResource(R.string.open_chat),
        fontSize = 18.sp,
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun QuotesPriceSum(
  quotes: List<MoveQuote>,
  modifier: Modifier = Modifier,
) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Text(
        text = "Total",
        fontSize = 18.sp,
      )
    },
    endSlot = {
      val summedPrice = quotes.map(MoveQuote::premium).reduce(UiMoney::plus)
      Text(
        text = stringResource(R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL, summedPrice.toString()),
        fontSize = 18.sp,
        textAlign = TextAlign.End,
      )
    },
    modifier = modifier,
  )
}

@Composable
private fun QuoteDetailsAndPdfs(
  quote: MoveQuote,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigInfoCard(
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
      Text(quote.insuranceName)
    }
    Spacer(Modifier.height(32.dp))
    CoverageRows(quote)
    Spacer(Modifier.height(32.dp))
    Pdfs(quote)
  }
}

@Composable
private fun CoverageItems(
  quote: MoveQuote,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    HedvigInfoCard(
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
      Text("Vad som täcks")
    }
    Spacer(Modifier.height(16.dp))
    val coverageItems = listOf(
      // todo get coverage items from inside ui state
      Color((0xFF000000..0xFFFFFFFF).random()) to "Eldsvåda",
      Color((0xFF000000..0xFFFFFFFF).random()) to "Vattenskada",
      Color((0xFF000000..0xFFFFFFFF).random()) to "Oväder",
      Color((0xFF000000..0xFFFFFFFF).random()) to "Inbrott",
    )
    var expandedItemIndex by rememberSaveable { mutableStateOf(-1) }
    coverageItems.forEachIndexed { index, (color, coverageText) ->
      ExpandablePlusCard(
        isExpanded = expandedItemIndex == index,
        onClick = {
          if (expandedItemIndex == index) {
            expandedItemIndex = -1
          } else {
            expandedItemIndex = index
          }
        },
        content = {
          Canvas(Modifier.size(16.dp)) {
            drawCircle(color)
          }
          Spacer(Modifier.width(12.dp))
          Text(
            text = coverageText,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.weight(1f, true),
          )
        },
        expandedContent = {
          Text("Information about $coverageText. ${"Lorem Ipsum".repeat(15)}")
        },
      )
      if (index != coverageItems.lastIndex) {
        Spacer(Modifier.height(4.dp))
      }
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.CoverageRows(quote: MoveQuote) {
  // todo add a section in MoveQuote which contains a list of these
  val coverageRowItems = listOf(
    "Försäkrat belopp" to UiMoney(1_000_000.0, CurrencyCode.SEK).toString(),
    "Självrisk" to UiMoney(1_500.0, CurrencyCode.SEK).toString(),
    "Reseskydd" to "45 dagar",
  )
  coverageRowItems.forEachIndexed { index, (firstText, secondText) ->
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Text(firstText, fontSize = 18.sp)
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
        ) {
          Text(secondText, fontSize = 18.sp)
          // TODO if there's a click action here, add it along with the info icon
//          Spacer(Modifier.width(4.dp))
//          Icon(
//            imageVector = Icons.Rounded.Info,
//            contentDescription = null,
//          )
        }
      },
    )
    if (index != coverageRowItems.lastIndex) {
      Spacer(Modifier.height(16.dp))
      Divider()
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun Pdfs(quote: MoveQuote) {
  // todo get pdf info from inside MoveQuote
  val pdfs = listOf(
    "Fullständiga villkor" to "Alla detaljer om skyddet", // also add a link on click
    "Förköpsinformation" to "De nödvändiga detaljerna",
    "Produktfaktablad (IPID)" to "Gör jämförelser enklare",
  )
  pdfs.forEachIndexed { index, (topText, bottomText) ->
    HedvigCard(
      onClick = {}, // Open the pdf link here
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      ) {
        Column(Modifier.weight(1f, true)) {
          Text(
            text = buildAnnotatedString {
              append(topText)
              withStyle(
                SpanStyle(
                  baselineShift = BaselineShift(0.3f),
                  fontSize = 10.sp,
                ),
              ) {
                append(" PDF")
              }
            },
            fontSize = 18.sp,
          )
          CompositionLocalProvider(LocalContentColor.provides(MaterialTheme.colorScheme.onSurfaceVariant)) {
            Text(bottomText, fontSize = 18.sp)
          }
        }
        Spacer(Modifier.width(8.dp))
        Icon(
          painter = painterResource(R.drawable.ic_north_east),
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      }
    }
    if (index != pdfs.lastIndex) {
      Spacer(Modifier.height(8.dp))
    }
  }
}

@Preview(
  name = "lightMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1080px,height=10000px,dpi=440",
)
@Preview(
  name = "darkMode portrait",
  uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
  device = "spec:width=1080px,height=10000px,dpi=440",
)
@Composable
private fun PreviewChangeAddressOfferScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ChangeAddressOfferScreen(
        ChangeAddressUiState(
          moveIntentId = MoveIntentId(""),
          quotes = List(2, MoveQuote::PreviewData),
          movingDate = ValidatedInput(Clock.System.now().toLocalDateTime(TimeZone.UTC).date),
        ),
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
