package com.hedvig.app.feature.addressautocompletion.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.hedvig.app.ui.compose.theme.HedvigTheme

class AddressAutoCompleteFragment : Fragment() {

    private val viewModel: AddressAutoCompleteViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HedvigTheme {
                    AddressAutoCompleteScreen()
                }
            }
        }
    }
}
