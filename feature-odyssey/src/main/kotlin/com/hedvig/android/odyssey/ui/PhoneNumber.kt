package com.hedvig.android.odyssey.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.ClaimsFlowViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

@Composable
fun PhoneNumber(viewModel: ClaimsFlowViewModel) {
  Column {
    Text("Phone", color = MaterialTheme.colors.onPrimary, fontSize = 40.sp)
    Button(onClick = viewModel::onNext) {
      Text(text = "Next")
    }
  }
}
