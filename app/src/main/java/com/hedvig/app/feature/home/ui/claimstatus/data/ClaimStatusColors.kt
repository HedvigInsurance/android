package com.hedvig.app.feature.home.ui.claimstatus.data

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.hedvig.app.R

object ClaimStatusColors {
    object Pill {
        val paid: Color
            @Composable
            get() = if (isSystemInDarkTheme()) {
                colorResource(R.color.lavender_400)
            } else {
                colorResource(R.color.lavender_200)
            }

        val reopened: Color
            @Composable
            get() = if (isSystemInDarkTheme()) {
                colorResource(R.color.forever_orange_500)
            } else {
                colorResource(R.color.forever_orange_300)
            }
    }

    object Progress {
        val paid: Color
            @Composable
            get() = MaterialTheme.colors.secondary

        val reopened: Color
            @Composable
            get() = colorResource(R.color.forever_orange_500)
    }
}
