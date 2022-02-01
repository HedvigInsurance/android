package com.hedvig.app.feature.home.ui.claimstatus.composables

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.app.feature.home.ui.claimstatus.data.PillUiState
import com.hedvig.app.ui.compose.composables.claimprogress.ClaimProgressRow
import com.hedvig.app.ui.compose.composables.claimprogress.ClaimProgressUiState
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.compose.preview.previewList
import java.util.UUID

@Composable
fun ClaimStatusCard(
    uiState: ClaimStatusCardUiState,
    modifier: Modifier = Modifier,
    isClickable: Boolean = false,
) {
    Card(
        modifier = modifier,
        elevation = 4.dp
    ) {
        Column {
            TopInfo(
                pillsUiState = uiState.pillsUiState,
                title = uiState.title,
                subtitle = uiState.subtitle,
                isClickable = isClickable,
                modifier = Modifier.padding(16.dp)
            )
            Divider()
            ClaimProgressRow(
                claimProgressItemsUiState = uiState.claimProgressItemsUiState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ClaimStatusCardPreview() {
    HedvigTheme {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            val claimStatusData = ClaimStatusCardUiState(
                id = UUID.randomUUID().toString(),
                pillsUiState = PillUiState.previewList(),
                title = "All-risk",
                subtitle = "Home Insurance Renter",
                claimProgressItemsUiState = ClaimProgressUiState.previewList(),
            )
            ClaimStatusCard(claimStatusData)
        }
    }
}
