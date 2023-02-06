package com.hedvig.android.odyssey.search

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.odyssey.model.SearchableClaim
import com.hedvig.android.odyssey.R

@Composable
fun ClaimsSearchResults(
  viewState: SearchViewState,
  onClaimSelected: (SearchableClaim) -> Unit,
  onClaimNotCovered: (String) -> Unit,
  cantFindAddress: () -> Unit,
  closeKeyboard: () -> Unit,
) {
  Column {
    SuggestionsList(
      viewState = viewState,
      onClaimSelected = { claim ->
        closeKeyboard()
        onClaimSelected(claim)
      },
      onClaimNotCovered = onClaimNotCovered,
      cantFindAddress = cantFindAddress,
      contentPadding = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Horizontal.plus(WindowInsetsSides.Bottom))
        .asPaddingValues(),
      modifier = Modifier.weight(1f),
    )
  }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
private fun SuggestionsList(
  viewState: SearchViewState,
  onClaimSelected: (SearchableClaim) -> Unit,
  onClaimNotCovered: (String) -> Unit,
  cantFindAddress: () -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier,
    contentPadding = contentPadding,
  ) {
    items(
      items = viewState.results,
      key = { item: SearchableClaim -> item.id },
      contentType = { "ItemEntry" },
    ) { claim: SearchableClaim ->
      ListItem(
        icon = {
          Image(
            painter = painterResource(claim.icon()),
            contentDescription = "Covered",
            modifier = Modifier
              .size(24.dp),
          )
        },
        secondaryText = {
          if (claim.hasQuickPayout) {
            Text(text = "Quick payout")
          } else if (!claim.isCovered) {
            Text(text = "Not covered")
          } else {
            Text(text = "Start a claim")
          }
        },
        text = { Text(text = claim.displayName) },
        modifier = Modifier
          .animateItemPlacement()
          .clickable {
            if (claim.isCovered) {
              onClaimSelected(claim)
            } else {
              onClaimNotCovered(claim.displayName)
            }
          },
      )
    }

    item(key = "other") {
      ListItem(
        icon = {
          Image(
            painter = painterResource(R.drawable.ic_claim_covered),
            contentDescription = "Covered",
            modifier = Modifier
              .size(24.dp),
          )
        },
        secondaryText = {
          Text(text = "Start a claim")
        },
        text = {
          Text("Other")
        },
        modifier = Modifier.clickable { cantFindAddress() },
      )
    }
  }
}

private fun SearchableClaim.icon(): Int = if (isCovered) {
  R.drawable.ic_claim_covered
} else {
  R.drawable.ic_claim_not_covered
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddressAutoCompleteScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      ClaimsSearchResults(
        SearchViewState(
          input = "test",
          results = listOf(),
        ),
        {},
        {},
        {},
        {},
      )
    }
  }
}
