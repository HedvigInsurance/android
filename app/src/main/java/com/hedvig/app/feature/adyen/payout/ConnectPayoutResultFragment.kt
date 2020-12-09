package com.hedvig.app.feature.adyen.payout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.hedvig.app.R
import com.hedvig.app.databinding.ConnectPayoutResultFragmentBinding
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ConnectPayoutResultFragment : Fragment(R.layout.connect_payout_result_fragment) {
    private val model: AdyenConnectPayoutViewModel by sharedViewModel()
    private val binding by viewBinding(ConnectPayoutResultFragmentBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.close.setHapticClickListener {
            model.close()
        }
    }

    companion object {
        fun newInstance() = ConnectPayoutResultFragment()
    }
}
