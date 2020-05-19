package com.hedvig.app.feature.embark.passages

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.hedvig.app.R
import com.hedvig.app.feature.ratings.openPlayStore
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.fragment_embark_upgrade_app.*

class UpgradeAppFragment : Fragment(R.layout.fragment_embark_upgrade_app) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        upgradeApp.setHapticClickListener {
            requireContext().openPlayStore()
        }
    }

    companion object {
        fun newInstance() = UpgradeAppFragment()
    }
}
