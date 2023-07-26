package com.hedvig.android.feature.insurances.insurancedetail.documents

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.ArrowNorthEast
import com.hedvig.android.feature.insurances.insurancedetail.ContractDetails
import hedvig.resources.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun DocumentsTab(
  documents: ImmutableList<ContractDetails.Document>,
  onDocumentClicked: (Uri) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(16.dp))
    for ((index, document) in documents.withIndex()) {
      DocumentCard(
        onClick = { onDocumentClicked(document.uri) },
        title = stringResource(
          when (document) {
            is ContractDetails.Document.InsuranceCertificate -> R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE
            is ContractDetails.Document.TermsAndConditions ->
              R.string.insurance_details_view_documents_full_terms_subtitle
          },
        ),
        subtitle = stringResource(
          when (document) {
            is ContractDetails.Document.InsuranceCertificate -> R.string.MY_DOCUMENTS_INSURANCE_TERMS
            is ContractDetails.Document.TermsAndConditions ->
              R.string.insurance_details_view_documents_insurance_letter_subtitle
          },
        ),
      )
      if (index != documents.lastIndex) {
        Spacer(Modifier.height(4.dp))
      }
    }
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
  }
}

@Composable
private fun DocumentCard(
  onClick: () -> Unit,
  title: String?,
  subtitle: String?,
) {
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    modifier = Modifier.padding(horizontal = 16.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Column(Modifier.weight(1f)) {
        Text(
          text = buildAnnotatedString {
            val text = title ?: return@buildAnnotatedString
            append(text)
            append(" ")
            withStyle(
              SpanStyle(
                baselineShift = BaselineShift(0.3f),
                fontSize = 10.sp,
              ),
            ) {
              append("PDF")
            }
          },
        )
        Text(
          text = subtitle ?: "",
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Spacer(Modifier.width(8.dp))
      Icon(
        imageVector = Icons.Hedvig.ArrowNorthEast,
        contentDescription = null,
        modifier = Modifier.size(16.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewDocumentsScreen() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      DocumentsTab(
        documents = persistentListOf(
          ContractDetails.Document.TermsAndConditions(Uri.EMPTY),
          ContractDetails.Document.InsuranceCertificate(Uri.EMPTY),
        ),
        onDocumentClicked = {},
      )
    }
  }
}
