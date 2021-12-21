package com.hedvig.app.feature.claimdetail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.ClaimDetailViewModel
import com.hedvig.app.feature.claimdetail.model.ClaimDetailsData
import com.hedvig.app.ui.compose.composables.CenteredProgressIndicator
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData
import java.util.Locale

@Composable
fun ClaimDetailScreen(
    viewState: ClaimDetailViewModel.ViewState,
    locale: Locale,
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
                locale,
                onChatClick,
                modifier = Modifier.padding(paddingValues)
            )
            ClaimDetailViewModel.ViewState.Error -> Text(text = "ERROR") // todo classic retry view
            ClaimDetailViewModel.ViewState.Loading -> CenteredProgressIndicator()
        }
    }
}

@Composable
private fun ClaimDetailScreen(
    data: ClaimDetailsData,
    locale: Locale,
    onChatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(Modifier.height(24.dp))
        ClaimType(
            title = data.claimType,
            subtitle = data.insuranceType,
        )
        Spacer(Modifier.height(16.dp))
        SubmittedAndClosedInformation(data.submittedAt, data.closedAt, locale)
        Spacer(Modifier.height(24.dp))
        ClaimDetailCard(data.cardData, onChatClick)
        Spacer(Modifier.height(56.dp))
        // TODO: Conditionally show this section if there is any files
/*
            Text(
                text = stringResource(R.string.claim_status_files),
                style = MaterialTheme.typography.h6,
            )
*/
        // TODO, different PR: actually show files here
        Spacer(Modifier.height(48.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ClaimDetailScreenPreview() {
    HedvigTheme {
        ClaimDetailScreen(
            data = ClaimDetailsData.previewData(),
            locale = Locale.getDefault(),
            onChatClick = {},
        )
    }
}
