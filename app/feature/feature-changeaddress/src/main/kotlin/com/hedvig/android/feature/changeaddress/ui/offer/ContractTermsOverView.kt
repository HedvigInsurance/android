package com.hedvig.android.feature.changeaddress.ui.offer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.design.system.R
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
import com.hedvig.android.core.designsystem.theme.HedvigTheme

@Composable
fun ContractTermsOverView(
  contractDisplayName: String,
  contractTerms: ContractTerms,
) {
  Column(
    modifier = Modifier
      .padding(horizontal = 16.dp)
      .fillMaxWidth(),
  ) {
    Text(
      text = contractDisplayName,
      modifier = Modifier
        .background(
          shape = SquircleShape,
          color = Color(0xFFE0F0F9),
        )
        .padding(8.dp),
    )

    Spacer(modifier = Modifier.padding(top = 12.dp))

    contractTerms.insurableLimits.mapIndexed { index, insurableLimit ->
      Spacer(modifier = Modifier.padding(4.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(text = insurableLimit.label)
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(text = insurableLimit.limit)
          Spacer(modifier = Modifier.padding(start = 4.dp))
          Icon(
            painter = painterResource(R.drawable.ic_info),
            contentDescription = "info",
            tint = Color(0xFFB4B4B4),
          )
        }
      }
      Spacer(modifier = Modifier.padding(4.dp))

      if (index < contractTerms.insurableLimits.size - 1) {
        Divider(color = Color(0xFFEAEAEA))
      }
    }

    Spacer(modifier = Modifier.padding(top = 12.dp))

    contractTerms.documents.map { contractTerm ->
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = SpaceBetween,
        modifier = Modifier
          .background(
            shape = SquircleShape,
            color = Color(0xFFF0F0F0),
          )
          .fillMaxWidth()
          .padding(8.dp)
          .clickable { },
      ) {
        Column {
          Text(contractTerm.title)
          Text(contractTerm.description, color = Color(0xFF727272))
        }

        Icon(
          painter = painterResource(R.drawable.ic_info),
          contentDescription = "link",
          tint = Color(0xFF121212),
          modifier = Modifier.offset(y = (-4).dp),
        )
      }
      Spacer(modifier = Modifier.padding(top = 8.dp))
    }

    Spacer(modifier = Modifier.padding(top = 82.dp))

    Text(
      text = "Vad som täcks",
      modifier = Modifier
        .background(
          shape = SquircleShape,
          color = Color(0xFFE0F0F9),
        )
        .padding(8.dp),
    )

    Spacer(modifier = Modifier.padding(top = 16.dp))

    contractTerms.perils.map { peril ->
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = SpaceBetween,
        modifier = Modifier
          .background(shape = SquircleShape, color = Color(0xFFF0F0F0))
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
      ) {
        Row {
          Text(peril.title)
        }
        Icon(
          painter = painterResource(R.drawable.ic_info),
          contentDescription = "link",
          tint = Color(0xFF121212),
        )
      }
      Spacer(modifier = Modifier.padding(top = 4.dp))
    }
  }
}

data class ContractTerms(
  val insurableLimits: List<InsurableLimit>,
  val perils: List<Peril>,
  val documents: List<Document>,
)

data class InsurableLimit(
  val label: String,
  val description: String,
  val limit: String,
)

data class Peril(
  val title: String,
  val description: String,
  val iconUrl: String,
  val isExpanded: Boolean = false,
)

data class Document(
  val title: String,
  val description: String,
  val documentUrl: String,
)

@Preview
@Composable
fun PreviewContractTermsOverView() {
  HedvigTheme {
    Surface {
      ContractTermsOverView(
        contractDisplayName = "Hemförsäkring",
        contractTerms = ContractTerms(
          insurableLimits = listOf(
            InsurableLimit(
              label = "Försäkrat belopp",
              description = "Alla detaljer om skyddet",
              limit = "1 000 000kr",
            ),
            InsurableLimit(
              label = "Självrisk",
              description = "Alla detaljer om skyddet",
              limit = "1500kr",
            ),
            InsurableLimit(
              label = "Reseskydd",
              description = "Alla detaljer om skyddet",
              limit = "45 dagar",
            ),
          ),
          perils = listOf(
            Peril(
              title = "Peril 1",
              description = "peril 1 desc",
              iconUrl = "",
            ),
            Peril(
              title = "Peril 2",
              description = "peril 1 desc",
              iconUrl = "",
            ),
            Peril(
              title = "Peril 3",
              description = "peril 1 desc",
              iconUrl = "",
            ),
            Peril(
              title = "Peril 4",
              description = "peril 1 desc",
              iconUrl = "",
            ),
            Peril(
              title = "Peril 5",
              description = "peril 1 desc",
              iconUrl = "",
            ),
            Peril(
              title = "Peril 6",
              description = "peril 1 desc",
              iconUrl = "",
            ),
            Peril(
              title = "Peril 7",
              description = "peril 1 desc",
              iconUrl = "",
            ),
            Peril(
              title = "Peril 8",
              description = "peril 1 desc",
              iconUrl = "",
            ),
          ),
          documents = listOf(
            Document(
              title = "Document 1",
              description = "Villkor",
              documentUrl = "",
            ),
            Document(
              title = "Document 2",
              description = "Conditions",
              documentUrl = "",
            ),
            Document(
              title = "Document 1",
              description = "Terms",
              documentUrl = "",
            ),
          ),
        ),
      )
    }
  }
}
