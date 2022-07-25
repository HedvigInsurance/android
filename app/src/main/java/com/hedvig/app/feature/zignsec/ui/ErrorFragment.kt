package com.hedvig.app.feature.zignsec.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ErrorFragment : Fragment() {
  private val model: SimpleSignAuthenticationViewModel by sharedViewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
    returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
    ComposeView(requireContext()).apply {
      setContent {
        HedvigTheme {
          GenericErrorScreen(
            onRetryButtonClick = { model.cancelSignIn() },
            modifier = Modifier
              .padding(16.dp)
              .padding(top = (80 - 16).dp),
          )
        }
      }
    }

  companion object {
    fun newInstance() = ErrorFragment()
  }
}
