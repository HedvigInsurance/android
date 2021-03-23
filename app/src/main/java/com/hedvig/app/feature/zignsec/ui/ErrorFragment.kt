package com.hedvig.app.feature.zignsec.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialSharedAxis
import com.hedvig.app.R
import com.hedvig.app.databinding.GenericErrorBinding
import com.hedvig.app.feature.zignsec.SimpleSignAuthenticationViewModel
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ErrorFragment : Fragment(R.layout.generic_error) {
    private val binding by viewBinding(GenericErrorBinding::bind)
    private val model: SimpleSignAuthenticationViewModel by sharedViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.retry.setHapticClickListener {
            model.restart()
        }
    }

    companion object {
        fun newInstance() = ErrorFragment()
    }
}
