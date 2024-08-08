package com.hedvig.android.feature.help.center.commonclaim

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.ui.emergency.FirstVetScreen
import com.hedvig.android.ui.emergency.FirstVetSection

@Composable
internal fun FirstVetDestination(sections: List<FirstVetSection>, navigateUp: () -> Unit, navigateBack: () -> Unit) {
  FirstVetScreen(sections, navigateUp, navigateBack)
}

@HedvigPreview
@Composable
private fun PreviewCommonClaimDestination() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      FirstVetDestination(
        sections = listOf(
          FirstVetSection(
            buttonTitle = "Button title 1",
            description = "Description 1",
            title = "Title 1",
            url = null,
          ),
          FirstVetSection(
            buttonTitle = "Button title 2",
            description = "Description 2",
            title = "Title 2",
            url = null,
          ),
        ),
        {},
        {},
      )
    }
  }
}
