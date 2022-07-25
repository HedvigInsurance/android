package com.hedvig.app.feature.zignsec.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
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
          GenericErrorScreen(onRetryButtonClick = { model.cancelSignIn() })
        }
      }
    }

  companion object {
    fun newInstance() = ErrorFragment()
  }
}
