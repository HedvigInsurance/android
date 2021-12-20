package com.hedvig.app.feature.claimdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.model.ClaimDetailData
import com.hedvig.app.feature.home.ui.claimstatus.composables.ClaimProgress
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.LoadingScreen
import com.hedvig.app.util.compose.preview.previewData

@Composable
fun ClaimDetailScreen(
    viewState: ClaimDetailViewModel.ViewState,
    onUpClick: () -> Unit,
    onChatClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBarWithBack(
                onClick = onUpClick,
                title = stringResource(R.string.claim_status_title),
            )
        }
    ) { paddingValues ->
        when (viewState) {
            is ClaimDetailViewModel.ViewState.Content -> ClaimDetailScreen(
                viewState.data,
                onChatClick,
                modifier = Modifier.padding(paddingValues)
            )
            ClaimDetailViewModel.ViewState.Error -> Text(text = "ERROR") // todo classic retry view
            ClaimDetailViewModel.ViewState.Loading -> LoadingScreen()
        }
    }
}

@Composable
private fun ClaimDetailScreen(
    data: ClaimDetailData,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(24.dp))
            Row {
                Image(
                    painter = painterResource(R.drawable.ic_claim),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = data.claimType,
                    style = MaterialTheme.typography.h6,
                )
            }
            Spacer(Modifier.height(16.dp))
            Row {
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        text = stringResource(R.string.claim_status_detail_submitted),
                        style = MaterialTheme.typography.caption,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = data.submittedText,
                        style = MaterialTheme.typography.body1,
                    )
                }
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        text = stringResource(R.string.claim_status_detail_closed),
                        style = MaterialTheme.typography.caption,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = data.closedText,
                        style = MaterialTheme.typography.body1,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Card {
                Column {
                    Spacer(Modifier.height(16.dp))
                    ClaimProgress(
                        claimProgressData = data.progress,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = data.statusParagraph,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.subtitle1,
                    )
                    Spacer(Modifier.height(24.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.claim_status_contact_generic_subtitle),
                                style = MaterialTheme.typography.caption,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.claim_status_contact_generic_title),
                                style = MaterialTheme.typography.body1,
                            )
                        }
                        IconButton(onClick = onChatClick) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colors.background,
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_chat_black),
                                    contentDescription = stringResource(
                                        R.string.claim_status_detail_chat_button_description
                                    ),
                                    contentScale = ContentScale.Inside,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            Spacer(Modifier.height(56.dp))

            // TODO: Conditionally show this section if there is any files
/*
            Text(
                text = stringResource(R.string.claim_status_files),
                style = MaterialTheme.typography.h6,
            )
*/

            // TODO, different PR: actually show files here

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClaimDetailScreenPreview() {
    HedvigTheme {
        ClaimDetailScreen(
            data = ClaimDetailData(
                claimType = "Insurance case",
                submittedText = "1 min ago",
                closedText = "—",
                progress = ClaimProgressData.previewData(),
                statusParagraph = "We have received your claim and will start reviewing it soon."
            ),
            onChatClick = {},
        )
    }
}
