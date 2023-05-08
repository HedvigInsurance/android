package com.feature.changeaddress.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack

@Composable
fun ChangeAddressResult(
  onFinish: () -> Unit,
) {
  Surface(Modifier.fillMaxSize()) {
    Column {
      TopAppBarWithBack(
        onClick = onFinish,
        title = "Ny address",
      )
      Text("Address changed successfully!")
      LargeContainedButton(
        onClick = { onFinish() },
      ) {
        Text(text = "Stäng")
      }
    }
  }
}
