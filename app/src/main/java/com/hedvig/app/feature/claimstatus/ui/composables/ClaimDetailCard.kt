package com.hedvig.app.feature.claimstatus.ui.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
import com.hedvig.app.feature.home.ui.claimstatus.composables.ClaimProgress
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData

@Composable
fun ClaimDetailCard(
    data: ClaimStatusDetailData.CardData,
    openChat: () -> Unit,
) {
    Card(
        elevation = 4.dp
    ) {
        Column {
            TopInfo(data = data)
            Divider()
            ChatPart(openChat = openChat)
        }
    }
}

@Composable
private fun TopInfo(data: ClaimStatusDetailData.CardData) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        ClaimProgress(
            claimProgressData = ClaimProgressData.previewData(), // temp solution
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = data.informationText,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ChatPart(openChat: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = stringResource(R.string.claim_status_contact_generic_subtitle),
                    style = MaterialTheme.typography.caption,
                )
            }
            Text(
                text = stringResource(R.string.claim_status_contact_generic_title),
                style = MaterialTheme.typography.body1,
            )
        }
        ChatIcon(openChat = openChat)
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimDetailCardPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            ClaimDetailCard(
                data = ClaimStatusDetailData.CardData.previewData(),
                openChat = {},
            )
        }
    }
}
