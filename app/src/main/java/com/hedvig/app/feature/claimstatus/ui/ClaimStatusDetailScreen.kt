package com.hedvig.app.feature.claimstatus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.hedvig.app.R
import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
import com.hedvig.app.feature.claimstatus.ui.composables.ClaimInfo
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack

@Composable
fun ClaimStatusDetailScreen(
    viewState: ClaimStatusDetailViewModel.ViewState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBarWithBack(
                onClick = onBack,
                title = stringResource(R.string.claim_status_title),
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false,
                ),
            )
        },
        bottomBar = {
            Spacer(
                Modifier
                    .navigationBarsHeight()
                    .fillMaxWidth()
            )
        }
    ) { contentPadding ->
        when (viewState) {
            is ClaimStatusDetailViewModel.ViewState.Data -> {
                ClaimStatusDetailScreen(
                    data = viewState.claimStatusDetailData,
                    modifier = Modifier.padding(contentPadding),
                )
            }
            ClaimStatusDetailViewModel.ViewState.Error -> {
                // todo error view?
                Text(stringResource(R.string.home_tab_error_title))
            }
            ClaimStatusDetailViewModel.ViewState.Loading -> {
                // todo loading view?
                Box(Modifier.fillMaxSize()) {
                    Text(stringResource(R.string.home_tab_error_title))
                }
            }
        }
    }
}

@Composable
private fun ClaimStatusDetailScreen(
    data: ClaimStatusDetailData,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            ClaimInfo(
                themedIconUrls = data.claimInfoData.themedIconUrls,
                claimType = data.claimInfoData.claimType,
                insuranceType = data.claimInfoData.insuranceType,
            )
            Spacer(Modifier.height(24.dp))
            ClaimStatusDetailCard()
            Spacer(Modifier.height(56.dp))
            PaymentDetails()
            Spacer(Modifier.height(56.dp))
            Files()
        }
    }
}

@Composable
fun ClaimStatusDetailCard() {
    Text(text = "ClaimStatusDetailCard")
}

@Composable
fun PaymentDetails() {
    Text(text = "PaymentDetails")
}

@Composable
fun Files() {
    Text(text = "Files")
}
