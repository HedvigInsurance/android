package com.hedvig.android.core.ui.insurance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hedvig.android.core.designsystem.newtheme.SquircleShape
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
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = modifier
      .background(
        shape = SquircleShape,
        color = MaterialTheme.colorScheme.background
      )
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .clickable { onClick() },
  ) {
    Column {
      Row {
        Text(title)
        Spacer(modifier = Modifier.padding(1.dp))
        Text("PDF", fontSize = 11.sp)
      }
      Text(subTitle, color = MaterialTheme.colorScheme.secondary)
    }

    Icon(
      imageVector = Hedvig.ArrowNorthEast,
      contentDescription = "link",
    )
  }
}

@Composable
@HedvigPreview
fun PreviewDocumentRow() {
  HedvigTheme {
    DocumentRow(
      title = "Document 1",
      subTitle = "Subtitle 1",
      onClick = {},
    )
  }
}
