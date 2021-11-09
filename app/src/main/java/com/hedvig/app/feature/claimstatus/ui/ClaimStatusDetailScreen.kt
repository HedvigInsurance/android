package com.hedvig.app.feature.claimstatus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun ClaimStatusDetailScreen(claimId: String) {
    Column() {
        Text("Detail screen: claimId:$claimId")
    }
}
