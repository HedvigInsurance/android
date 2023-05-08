package com.feature.changeaddress.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.R
import com.hedvig.android.core.designsystem.newtheme.SquircleShape

@Composable
fun AddressInfoCard(modifier: Modifier) {
  Box(
    modifier = modifier
      .background(
        shape = SquircleShape,
        color = Color(0xFFCFE5F2),
      ),
  ) {
    Row(modifier = Modifier.padding(12.dp)) {
      Icon(
        painter = painterResource(R.drawable.ic_info),
        contentDescription = "info",
        modifier = Modifier.offset(y = 2.dp),
      )
      Spacer(modifier = Modifier.padding(start = 8.dp))
      Text(
        text = stringResource(id = hedvig.resources.R.string.CHANGE_ADDRESS_COVERAGE_INFO_TEXT),
        style = MaterialTheme.typography.bodyMedium,
      )
    }
  }
}
