package com.hedvig.android.odyssey.search

import android.content.res.Configuration
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.odyssey.model.ItemType
import com.hedvig.android.odyssey.model.SearchableClaim

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
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFA4C9C6))
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
            showAll()
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
                .background(Color(0xFFFFBF00))
                .align(Alignment.CenterVertically),
            )

            Spacer(modifier = Modifier.padding(12.dp))

            Text("Other", Modifier.align(Alignment.CenterVertically))
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
