package com.hedvig.android.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.separator

@Composable
fun BottomSheetHandle(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(width = 32.dp, height = 4.dp)
      .background(
        color = MaterialTheme.colors.separator,
        shape = RoundedCornerShape(20.dp),
      ),
  )
}
