package com.hedvig.android.odyssey.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim

@Composable
fun CommonClaims(
  selectClaim: (SearchableClaim) -> Unit,
  commonClaims: List<SearchableClaim>,
  selectOther: () -> Unit,
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
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(Color(claim.color()))
                  .align(Alignment.CenterVertically),
              )

              Spacer(modifier = Modifier.padding(12.dp))

              Text(claim.displayName, Modifier.align(Alignment.CenterVertically))
            }
          }
          Divider(Modifier.padding(horizontal = 8.dp))
        }

        Column(
          modifier = Modifier.clickable {
            selectOther()
          },
        ) {
          Row(
            modifier = Modifier
              .padding(22.dp)
              .fillMaxWidth(),
          ) {
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0XFFD5CE82))
                .align(Alignment.CenterVertically),
            )

            Spacer(modifier = Modifier.padding(12.dp))

            Text(
              stringResource(id = hedvig.resources.R.string.claims_select_other),
              Modifier.align(Alignment.CenterVertically),
            )
          }
        }
      }
    }
  }
}

private fun SearchableClaim.color() = when (this.displayName) {
  "Stolen bike",
  "Stulen cykel",
  -> 0xFFA49758
  "Trasig telefon",
  "Broken phone",
  -> 0XFFA4C9C6
  "Broken headphones",
  "Trasiga hörlurar",
  -> 0xFFFFBF00
  "Stolen phone",
  "Stulen telefon",
  -> 0XFF98C2DA
  "Stolen computer",
  "Stulen dator",
  -> 0XFF727272
  "Stolen headphones",
  "Stulna hörlurar",
  -> 0XFF727272
  else -> 0XFF4B739B
}

@HedvigPreview
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
          id = "4",
          displayName = "Stolen phone",
          itemType = ItemType(""),
        ),
        SearchableClaim(
          id = "2",
          displayName = "Broken computer",
          itemType = ItemType("COMPUTER"),
        ),
        SearchableClaim(
          id = "4",
          displayName = "Stolen computer",
          itemType = ItemType(""),
        ),
        SearchableClaim(
          id = "4",
          displayName = "Broken headphones",
          itemType = ItemType(""),
        ),
        SearchableClaim(
          id = "4",
          displayName = "Stolen headphones",
          itemType = ItemType(""),
        ),
      ),
      {},
    )
  }
}
