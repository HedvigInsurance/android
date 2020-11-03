package com.hedvig.app.feature.profile.ui.tab

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialFadeThrough
import com.hedvig.app.R
import com.hedvig.app.databinding.ProfileFragmentBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.profile.service.ProfileTracker
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppActivity
import com.hedvig.app.feature.profile.ui.charity.CharityActivity
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.extensions.viewBinding
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ProfileFragment : Fragment(R.layout.profile_fragment) {
    private val binding by viewBinding(ProfileFragmentBinding::bind)
    private val model: ProfileViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private var scroll = 0
    private val tracker: ProfileTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialFadeThrough()
        exitTransition = MaterialFadeThrough()
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scroll = 0

        binding.recycler.apply {
            val scrollInitialBottomPadding = paddingBottom
            val scrollInitialTopPadding = paddingTop

            var hasInsetForToolbar = false

            loggedInViewModel.toolbarInset.observe(viewLifecycleOwner) { toolbarInset ->
                updatePadding(top = scrollInitialTopPadding + toolbarInset)
                if (!hasInsetForToolbar) {
                    hasInsetForToolbar = true
                    scrollToPosition(0)
                }
            }

            addOnScrollListener(
                ScrollPositionListener(
                    { scrollPosition ->
                        scroll = scrollPosition
                        loggedInViewModel.onScroll(scrollPosition)
                    },
                    viewLifecycleOwner
                )
            )

            loggedInViewModel.bottomTabInset.observe(viewLifecycleOwner) { bottomTabInset ->
                updatePadding(bottom = scrollInitialBottomPadding + bottomTabInset)
            }

            adapter = ProfileAdapter(viewLifecycleOwner)
        }

        model.data.observe(viewLifecycleOwner) { data ->
            (binding.recycler.adapter as? ProfileAdapter)?.submitList(listOf(
                ProfileModel.Title,
                ProfileModel.Row(
                    getString(R.string.PROFILE_MY_INFO_ROW_TITLE),
                    "${data.member.firstName} ${data.member.lastName}",
                    R.drawable.ic_contact_information
                ) {
                    tracker.myInfoRow()
                    startActivity(Intent(requireContext(), MyInfoActivity::class.java))
                },
                ProfileModel.Row(
                    getString(R.string.PROFILE_MY_CHARITY_ROW_TITLE),
                    data.cashback?.fragments?.cashbackFragment?.name ?: "",
                    R.drawable.ic_profile_charity
                ) {
                    tracker.charityRow()
                    startActivity(Intent(requireContext(), CharityActivity::class.java))
                },
                ProfileModel.Row(
                    getString(R.string.PROFILE_ROW_PAYMENT_TITLE),
                    data.insuranceCost?.fragments?.costFragment?.monthlyNet?.fragments?.monetaryAmountFragment?.toMonetaryAmount()
                        ?.let { monthlyNet ->
                            getString(
                                R.string.PROFILE_ROW_PAYMENT_DESCRIPTION,
                                monthlyNet.format(requireContext())
                            )
                        } ?: "",
                    R.drawable.ic_payment
                ) {
                    tracker.paymentRow()
                    startActivity(Intent(requireContext(), PaymentActivity::class.java))
                },
                ProfileModel.Subtitle,
                ProfileModel.Row(
                    getString(R.string.profile_appSettingsSection_row_headline),
                    getString(R.string.profile_appSettingsSection_row_subheadline),
                    R.drawable.ic_profile_settings
                ) {
                    tracker.settings()
                    startActivity(SettingsActivity.newInstance(requireContext()))
                },
                ProfileModel.Row(
                    getString(R.string.PROFILE_ABOUT_ROW),
                    getString(R.string.profile_tab_about_row_subtitle),
                    R.drawable.ic_info_toolbar
                ) {
                    tracker.aboutAppRow()
                    startActivity(Intent(requireContext(), AboutAppActivity::class.java))
                },
                ProfileModel.Logout
            )
            )
        }
    }
}

