package com.hedvig.android.feature.changeaddress.destination

import android.content.res.Configuration
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigInfoCard
import com.hedvig.android.core.designsystem.material3.squircleExtraSmall
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.core.ui.dialog.ErrorDialog
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productVariant.android.getStringRes
import com.hedvig.android.data.productvariant.InsurableLimit
import com.hedvig.android.feature.changeaddress.ChangeAddressUiState
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.ui.offer.Faqs
import com.hedvig.android.feature.changeaddress.ui.offer.QuoteCard
import hedvig.resources.R
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun ChangeAddressOfferDestination(
  viewModel: ChangeAddressViewModel,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  onChangeAddressResult: (LocalDate?) -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState: ChangeAddressUiState by viewModel.uiState.collectAsStateWithLifecycle()
  val moveResult = uiState.successfulMoveResult

  LaunchedEffect(moveResult) {
    if (moveResult != null) {
      onChangeAddressResult(uiState.movingDate.input)
    }
  }
  ChangeAddressOfferScreen(
    uiState = uiState,
    openChat = openChat,
    navigateUp = navigateUp,
    onErrorDialogDismissed = viewModel::onErrorDialogDismissed,
    onExpandQuote = viewModel::onExpandQuote,
    onConfirmMove = viewModel::onConfirmMove,
    openUrl = openUrl,
  )
}

@Composable
private fun ChangeAddressOfferScreen(
  uiState: ChangeAddressUiState,
  openChat: () -> Unit,
  navigateUp: () -> Unit,
  onErrorDialogDismissed: () -> Unit,
  onExpandQuote: (MoveQuote) -> Unit,
  onConfirmMove: (MoveIntentId) -> Unit,
  openUrl: (String) -> Unit,
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
    topAppBarText = stringResource(id = R.string.CHANGE_ADDRESS_SUMMARY_TITLE),
    navigateUp = navigateUp,
    topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    scrollState = scrollState,
  ) {
    Spacer(Modifier.height(8.dp))
    for (quote in uiState.quotes) {
      QuoteCard(
        movingDate = quote.startDate,
        quote = quote,
        onExpandClicked = { onExpandQuote(quote) },
        isExpanded = quote.isExpanded,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
    if (uiState.quotes.size > 1) {
      VectorInfoCard(
        text = stringResource(id = R.string.CHANGE_ADDRESS_OTHER_INSURANCES_INFO_TEXT),
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
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
    var whatsIncludedButtonPositionY by remember { mutableFloatStateOf(0f) }
    HedvigTextButton(
      text = stringResource(id = R.string.CHANGE_ADDRESS_INCLUDED),
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
        .onPlaced { layoutCoordinates ->
          // Find the Y position where this button ends, to scroll right below it on click.
          whatsIncludedButtonPositionY =
            layoutCoordinates.positionInParent().y + layoutCoordinates.size.height
        },
    )
    Spacer(Modifier.height(80.dp))

    for (quote in uiState.quotes.distinctBy { it.productVariant.contractGroup }) {
      QuoteDetailsAndPdfs(
        quote = quote,
        openUrl = openUrl,
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(40.dp))
    }
    Faqs(
      faqItems = listOf(
        stringResource(id = R.string.CHANGE_ADDRESS_FAQ_DATE_TITLE) to stringResource(
          id = R.string.CHANGE_ADDRESS_FAQ_DATE_LABEL,
        ),
        stringResource(id = R.string.CHANGE_ADDRESS_FAQ_PRICE_TITLE) to stringResource(
          id = R.string.CHANGE_ADDRESS_FAQ_PRICE_LABEL,
        ),
        stringResource(id = R.string.CHANGE_ADDRESS_FAQ_RENTBRF_TITLE) to stringResource(
          id = R.string.CHANGE_ADDRESS_FAQ_RENTBRF_LABEL,
        ),
        stringResource(id = R.string.CHANGE_ADDRESS_FAQ_STORAGE_TITLE) to stringResource(
          id = R.string.CHANGE_ADDRESS_FAQ_STORAGE_LABEL,
        ),
        stringResource(id = R.string.CHANGE_ADDRESS_FAQ_STUDENT_TITLE) to stringResource(
          id = R.string.CHANGE_ADDRESS_FAQ_STUDENT_LABEL,
        ),
      ),
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(64.dp))
    Text(
      text = stringResource(id = R.string.CHANGE_ADDRESS_NO_FIND),
      style = MaterialTheme.typography.bodyLarge,
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
        style = MaterialTheme.typography.bodyLarge,
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun QuotesPriceSum(quotes: List<MoveQuote>, modifier: Modifier = Modifier) {
  HorizontalItemsWithMaximumSpaceTaken(
    startSlot = {
      Text(
        text = stringResource(id = R.string.CHANGE_ADDRESS_TOTAL),
        style = MaterialTheme.typography.bodyLarge,
      )
    },
    endSlot = {
      val summedPrice = quotes.map(MoveQuote::premium).reduce(UiMoney::plus)
      Text(
        text = stringResource(R.string.CHANGE_ADDRESS_PRICE_PER_MONTH_LABEL, summedPrice.toString()),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.End,
      )
    },
    modifier = modifier,
  )
}

@Composable
private fun QuoteDetailsAndPdfs(quote: MoveQuote, openUrl: (String) -> Unit, modifier: Modifier = Modifier) {
  Column(modifier) {
    HedvigInfoCard(
      contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
      shape = MaterialTheme.shapes.squircleExtraSmall,
    ) {
      Text(
        text = quote.productVariant.displayName,
        style = MaterialTheme.typography.bodyMedium,
      )
    }
    Spacer(Modifier.height(32.dp))
    val insurableLimits = quote.productVariant.insurableLimits
    if (insurableLimits.isNotEmpty()) {
      InsurableLimits(insurableLimits)
      Spacer(Modifier.height(32.dp))
    }
    Documents(quote, openUrl)
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.InsurableLimits(insurableLimits: List<InsurableLimit>) {
  insurableLimits.mapIndexed { index, highlight ->
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        Text(
          text = highlight.label,
          style = MaterialTheme.typography.bodyLarge,
        )
      },
      endSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.End,
        ) {
          Text(
            text = highlight.limit,
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      },
      spaceBetween = 18.dp,
    )
    if (index != insurableLimits.lastIndex) {
      Spacer(Modifier.height(16.dp))
      HorizontalDivider()
      Spacer(Modifier.height(16.dp))
    }
  }
}

@Composable
private fun Documents(quote: MoveQuote, openUrl: (String) -> Unit) {
  quote.productVariant.documents.mapIndexed { index, document ->
    if (index > 0) {
      Spacer(Modifier.height(8.dp))
    }
    HedvigCard(
      onClick = { openUrl(document.url) },
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
      ) {
        Column(Modifier.weight(1f, true)) {
          val fontSize = MaterialTheme.typography.bodySmall.fontSize
          Text(
            text = buildAnnotatedString {
              document.type.getStringRes()?.let {
                append(stringResource(id = it))
              }
              withStyle(
                SpanStyle(
                  baselineShift = BaselineShift(0.3f),
                  fontSize = fontSize,
                ),
              ) {
                append(" PDF")
              }
            },
            style = MaterialTheme.typography.bodyLarge,
          )
          Text(
            text = document.displayName,
            style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
          )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
          imageVector = Icons.Hedvig.ArrowNorthEast,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
        )
      }
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
          extraBuildingTypes = emptyList(),
        ),
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
