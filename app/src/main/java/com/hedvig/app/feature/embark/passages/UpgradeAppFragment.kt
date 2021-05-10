package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.databinding.FragmentEmbarkUpgradeAppBinding
import com.hedvig.app.feature.ratings.tryOpenPlayStore
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class UpgradeAppFragment : Fragment(R.layout.fragment_embark_upgrade_app) {
    private val binding by viewBinding(FragmentEmbarkUpgradeAppBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.upgradeApp.setHapticClickListener {
            requireContext().tryOpenPlayStore()
        }
    }

    companion object {
        fun newInstance() = UpgradeAppFragment()
    }
}
