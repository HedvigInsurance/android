package com.hedvig.app.feature.insurance.ui.detail.documents

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.insurance.DocumentRow
import com.hedvig.app.feature.documents.DocumentItems

@Composable
fun InsuranceDocumentsTab(
  documents: List<DocumentItems>,
  onDocumentClicked: (DocumentItems.Document) -> Unit,
) {
  val context = LocalContext.current

  Column {
    documents.forEach { document ->
      when (document) {
        is DocumentItems.Document -> DocumentRow(
          modifier = Modifier.padding(bottom = 4.dp),
          title = document.getTitle(context) ?: "",
          subTitle = document.getSubTitle(context) ?: "",
          onClick = {
            onDocumentClicked(document)
          },
        )
        is DocumentItems.Header -> TODO("DocumentItems.Header type should be removed")
        is DocumentItems.CancelInsuranceButton -> TODO()
      }
    }
  }
}

@HedvigPreview
@Composable
fun PreviewInsuranceDocumentsTab() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      InsuranceDocumentsTab(
        documents = listOf(
          DocumentItems.Document(
            title = "Terms & Conditions",
            subtitle = "All details about your coverage",
            uriString = "",
          ),
          DocumentItems.Document(
            title = "Pre-purchase info",
            subtitle = "All pre-pruchase details",
            uriString = "",
          ),
          DocumentItems.Document(
            title = "Productinfo (IPID)",
            subtitle = "Compare your coverage",
            uriString = "",
          ),
        ),
        onDocumentClicked = {},
      )
    }
  }
}
