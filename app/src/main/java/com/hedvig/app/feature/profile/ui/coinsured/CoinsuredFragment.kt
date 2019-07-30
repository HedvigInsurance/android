package com.hedvig.app.feature.profile.ui.coinsured

import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.CustomTypefaceSpan
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatFont
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.concat
import com.hedvig.app.util.extensions.setupLargeTitle
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import kotlinx.android.synthetic.main.fragment_coinsured.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class CoinsuredFragment : androidx.fragment.app.Fragment() {

    private val profileViewModel: ProfileViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_coinsured, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLargeTitle(R.string.PROFILE_COINSURED_TITLE, R.font.circular_bold, R.drawable.ic_back) {
            requireActivity().findNavController(R.id.loggedNavigationHost).popBackStack()
        }

        coinsuredSphere.drawable.compatSetTint(requireContext().compatColor(R.color.purple))

        loadData()
    }

    private fun loadData() {
        profileViewModel.data.observe(this, Observer { profileData ->
            loadingSpinner.remove()
            sphereContainer.show()
            textContainer.show()

            loadingAnimation.show()
            loadingAnimation.useHardwareAcceleration(true)
            loadingAnimation.playAnimation()

            profileData?.insurance?.personsInHousehold?.let { personsInHousehold ->
                val label = resources.getString(R.string.PROFILE_COINSURED_QUANTITY_LABEL)
                val partOne = SpannableString("${personsInHousehold - 1}\n")
                val partTwo = SpannableString(label)
                partOne.setSpan(
                    CustomTypefaceSpan(requireContext().compatFont(R.font.soray_extrabold)),
                    0,
                    1,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
                )
                partTwo.setSpan(AbsoluteSizeSpan(16, true), 0, label.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)

                sphereText.text = partOne.concat(partTwo)
            }
        })
    }
}
