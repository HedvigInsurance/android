package com.hedvig.android.core.ui.insurance

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun DocumentRow(
  modifier: Modifier = Modifier,
  title: String,
  subTitle: String,
  onClick: () -> Unit,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
      Column(Modifier.weight(1f, true)) {
        Text(
          text = buildAnnotatedString {
            append(title)
            withStyle(
              SpanStyle(
                baselineShift = BaselineShift(0.3f),
                fontSize = 10.sp,
              ),
            ) {
              append(" PDF")
            }
          },
          fontSize = 18.sp,
        )
        CompositionLocalProvider(LocalContentColor.provides(MaterialTheme.colorScheme.onSurfaceVariant)) {
          Text(subTitle, fontSize = 18.sp)
        }
      }
      Icon(
        imageVector = Icons.Hedvig.ArrowNorthEast,
        contentDescription = null,
      )
    }
  }
}

@Composable
@HedvigPreview
fun PreviewDocumentRow() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      DocumentRow(
        title = "Document 1",
        subTitle = "Subtitle 1",
        onClick = {},
      )
    }
  }
}
