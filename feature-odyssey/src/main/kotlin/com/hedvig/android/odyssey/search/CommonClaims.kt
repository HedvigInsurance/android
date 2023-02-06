package com.hedvig.android.odyssey.search

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim
import com.hedvig.android.odyssey.model.icon
import com.hedvig.android.odyssey.R

@Composable
fun CommonClaims(
  selectClaim: (SearchableClaim) -> Unit,
  commonClaims: List<SearchableClaim>,
  showAll: () -> Unit,
) {
  Column {
    Surface(
      modifier = Modifier.padding(
        start = 22.dp,
        end = 22.dp,
        bottom = 8.dp,
      ),
      shape = RoundedCornerShape(8.dp),
    ) {
      Column {
        commonClaims.forEachIndexed { index, claim ->
          Column(
            modifier = Modifier.clickable {
              selectClaim(claim)
            },
          ) {
            Row(
              modifier = Modifier
                .padding(22.dp)
                .fillMaxWidth(),
            ) {
              val iconUrl = claim.icon(isSystemInDarkTheme())
              if (iconUrl != null) {
                AsyncImage(
                  model = ImageRequest.Builder(LocalContext.current)
                    .data(claim.icon(isSystemInDarkTheme()))
                    .crossfade(true)
                    .build(),
                  contentDescription = "Icon",
                  imageLoader = imageLoader,
                  modifier = Modifier
                    .width(22.dp)
                    .height(22.dp),
                )
              }

              Spacer(modifier = Modifier.padding(12.dp))

              Text(claim.displayName)
            }
          }
          Divider(Modifier.padding(horizontal = 8.dp))
        }

        Column(
          modifier = Modifier.clickable {
            showAll()
          },
        ) {
          Row(
            modifier = Modifier
              .padding(22.dp)
              .fillMaxWidth(),
          ) {
            Image(
              painter = painterResource(id = R.drawable.ic_claim_covered),
              contentDescription = "Other",
              modifier = Modifier.size(22.dp),
            )

            Spacer(modifier = Modifier.padding(12.dp))

            Text("Other")
          }
        }
      }
    }
  }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCommonClaims() {
  HedvigTheme {
    CommonClaims(
      selectClaim = {},
      commonClaims = listOf(
        SearchableClaim(
          id = "1",
          displayName = "Broken phone",
          itemType = ItemType("PHONE"),
        ),
        SearchableClaim(
          id = "2",
          displayName = "Broken computer",
          itemType = ItemType("COMPUTER"),
        ),
        SearchableClaim(
          id = "3",
          displayName = "Stolen bike",
          itemType = ItemType("BIKE"),
        ),
        SearchableClaim(
          id = "4",
          displayName = "Delayed Luggage",
          itemType = ItemType(""),
        ),
      ),
      {},
    )
  }
}
