package com.hedvig.app.feature.home.ui.composable

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.hedvig.android.owldroid.graphql.HomeQuery

@Composable
fun ActiveClaimCards(claims: List<HomeQuery.ActiveClaim>) {
    // TODO Horizontal Scroll Pager with dot indicators
    ActiveClaimCard(claims.first())
}

@Composable
private fun ActiveClaimCard(claim: HomeQuery.ActiveClaim) {
    Text("Claim status: ${claim.status}, ID: ${claim.id}")
}
