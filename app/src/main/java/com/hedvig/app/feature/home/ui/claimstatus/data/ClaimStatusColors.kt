package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.app.R
import com.hedvig.app.util.compose.DarkAndLightColor

object ClaimStatusColors {
    object Pill {
        val paid: DarkAndLightColor = DarkAndLightColor(
            dark = R.color.lavender_400,
            light = R.color.lavender_200,
        )

        val reopened: DarkAndLightColor = DarkAndLightColor(
            dark = R.color.forever_orange_500,
            light = R.color.forever_orange_300,
        )
    }

    object Progress {
        val paid: DarkAndLightColor = DarkAndLightColor(R.color.colorSecondary)

        val reopened: DarkAndLightColor = DarkAndLightColor(R.color.forever_orange_500)
    }
}
