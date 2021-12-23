package com.hedvig.app.feature.claimdetail.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.model.ClaimDetailsData
import com.hedvig.app.feature.home.ui.claimstatus.composables.ClaimProgress
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.ui.compose.composables.ChatIcon
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData

@Composable
fun ClaimDetailCard(
    data: ClaimDetailsData.CardData,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column {
            TopSection(
                progressData = data.progress,
                statusParagraph = data.statusParagraph,
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 20.dp,
                )
            )
            Divider()
            BottomSection(
                onChatClick = onChatClick,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun TopSection(
    progressData: List<ClaimProgressData>,
    statusParagraph: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier,
    ) {
        ClaimProgress(
            claimProgressData = progressData,
        )
        Text(
            text = statusParagraph,
            style = MaterialTheme.typography.subtitle1,
        )
    }
}

@Composable
private fun BottomSection(
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
        ChatIcon(
            onClick = onChatClick,
            contentDescription = stringResource(R.string.claim_status_detail_chat_button_description),
        )
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
                ClaimDetailsData.CardData(
                    progress = ClaimProgressData.previewData(),
                    statusParagraph = "StatusParagraph"
                ),
                {}
            )
        }
    }
}
