package com.hedvig.app.feature.claimdetail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.Scaffold
import com.hedvig.app.R
import com.hedvig.app.feature.claimdetail.model.ClaimDetailResult
import com.hedvig.app.feature.claimdetail.model.ClaimDetailUiState
import com.hedvig.app.ui.compose.composables.CenteredProgressIndicator
import com.hedvig.app.ui.compose.composables.GenericErrorScreen
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewData
import java.util.Locale

@Composable
fun ClaimDetailScreen(
    viewState: ClaimDetailViewState,
    locale: Locale,
    retry: () -> Unit,
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
            is ClaimDetailViewState.Content -> ClaimDetailScreen(
                uiState = viewState.uiState,
                locale = locale,
                onChatClick = onChatClick,
                modifier = Modifier.padding(paddingValues)
            )
            ClaimDetailViewState.Error -> GenericErrorScreen(retry)
            ClaimDetailViewState.Loading -> CenteredProgressIndicator()
        }
    }
}

@Composable
private fun ClaimDetailScreen(
    uiState: ClaimDetailUiState,
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
            title = uiState.claimType,
            subtitle = uiState.insuranceType,
        )
        when (uiState.claimDetailResult) {
            ClaimDetailResult.Open -> {
                Spacer(Modifier.height(16.dp))
            }
            is ClaimDetailResult.Closed -> {
                Spacer(Modifier.height(20.dp))
                ClaimResultSection(uiState.claimDetailResult, locale)
                Spacer(Modifier.height(20.dp))
            }
        }
        SubmittedAndClosedInformation(uiState.submittedAt, uiState.closedAt, locale)
        Spacer(Modifier.height(24.dp))
        ClaimDetailCard(uiState.claimDetailCard, onChatClick)
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
            uiState = ClaimDetailUiState.previewData(),
            locale = Locale.getDefault(),
            onChatClick = {},
        )
    }
}
