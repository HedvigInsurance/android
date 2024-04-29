package com.hedvig.android.feature.home.home.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.ui.emergency.FirstVetScreen
import com.hedvig.android.ui.emergency.FirstVetSection

@Composable
internal fun FirstVetDestination(sections: List<FirstVetSection>, navigateUp: () -> Unit, navigateBack: () -> Unit) {
  FirstVetScreen(
    sections = sections,
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}
