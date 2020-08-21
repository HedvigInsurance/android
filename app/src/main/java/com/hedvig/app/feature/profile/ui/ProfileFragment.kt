package com.hedvig.app.feature.profile.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.hedvig.app.R
import com.hedvig.app.databinding.ProfileFragmentBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ProfileFragment : Fragment(R.layout.profile_fragment) {
    private val binding by viewBinding(ProfileFragmentBinding::bind)
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()

    private var scrollInitialBottomPadding = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollInitialBottomPadding = binding.recycler.paddingBottom

        loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
            binding.recycler.updatePadding(bottom = scrollInitialBottomPadding + bottomTabInset)
        }
    }
}
