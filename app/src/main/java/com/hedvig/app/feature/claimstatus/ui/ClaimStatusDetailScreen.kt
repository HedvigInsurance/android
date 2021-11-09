package com.hedvig.app.feature.claimstatus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.insets.ui.Scaffold
import com.hedvig.app.R
import com.hedvig.app.ui.compose.composables.appbar.TopAppBarWithBack
import com.hedvig.app.util.compose.debugBorder

@Composable
fun ClaimStatusDetailScreen(
    viewState: ClaimStatusDetailViewModel.Companion.ViewState,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBarWithBack(
                onClick = onBack,
                title = stringResource(R.string.claim_status_title),
                modifier = Modifier
                    .statusBarsPadding()
                    .debugBorder()
            )
        },
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
        )
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .debugBorder(Color.Green, 2.dp)
                .padding(contentPadding)
                .debugBorder(Color.Blue)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(viewState.toString())
                Text(viewState.toString())
                Text(viewState.toString())
            }
        }
    }
}
