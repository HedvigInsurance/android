package com.hedvig.app.feature.profile.ui.tab

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.R
import com.hedvig.app.databinding.ProfileFragmentBinding
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.ScrollPositionListener
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppActivity
import com.hedvig.app.feature.profile.ui.charity.CharityActivity
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.settings.SettingsActivity
import com.hedvig.app.util.extensions.showAlert
import com.hedvig.app.util.extensions.triggerRestartActivity
import com.hedvig.app.util.extensions.viewLifecycle
import com.hedvig.app.util.extensions.viewLifecycleScope
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ProfileFragment : Fragment(R.layout.profile_fragment) {
    private val binding by viewBinding(ProfileFragmentBinding::bind)
    private val model: ProfileViewModel by sharedViewModel()
    private val loggedInViewModel: LoggedInViewModel by sharedViewModel()
    private var scroll = 0

    override fun onResume() {
        super.onResume()
        loggedInViewModel.onScroll(scroll)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scroll = 0

        val adapter = ProfileAdapter(viewLifecycleOwner, model::reload, model::onLogout)
        binding.recycler.apply {

            scroll = 0
            addOnScrollListener(
                ScrollPositionListener(
                    { scrollPosition ->
                        scroll = scrollPosition
                        loggedInViewModel.onScroll(scrollPosition)
                    },
                    viewLifecycleOwner
                )
            )
            this.adapter = adapter
        }

        model
            .data
            .flowWithLifecycle(viewLifecycle)
            .onEach { viewState ->
                when (viewState) {
                    ProfileViewModel.ViewState.Error -> adapter.submitList(listOf(ProfileModel.Error))
                    ProfileViewModel.ViewState.Loading -> {}
                    is ProfileViewModel.ViewState.Success -> {
                        adapter.submitList(buildProfileModelList(viewState.profileUiState))
                    }
                }
            }
            .launchIn(viewLifecycleScope)

        model.events
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    ProfileViewModel.Event.Logout -> requireContext().triggerRestartActivity()
                    is ProfileViewModel.Event.Error -> requireContext().showAlert(
                        title = R.string.error_dialog_title,
                        message = R.string.component_error,
                        positiveAction = {}
                    )
                }
            }
            .launchIn(lifecycleScope)
    }

    private fun buildProfileModelList(profileUiState: ProfileUiState): List<ProfileModel> {
        return buildList {
            add(ProfileModel.Title)
            add(
                ProfileModel.Row(
                    title = getString(R.string.PROFILE_MY_INFO_ROW_TITLE),
                    caption = profileUiState.contactInfoName,
                    icon = R.drawable.ic_contact_information,
                    onClick = {
                        startActivity(Intent(requireContext(), MyInfoActivity::class.java))
                    }
                )
            )
            when (val charityState = profileUiState.charityState) {
                CharityState.DontShow -> {}
                CharityState.NoneSelected -> add(buildCharityRowItem())
                is CharityState.Selected -> add(buildCharityRowItem(charityState.charityName))
            }
            when (val paymentState = profileUiState.paymentState) {
                is PaymentState.Show -> {
                    add(
                        ProfileModel.Row(
                            title = getString(R.string.PROFILE_ROW_PAYMENT_TITLE),
                            caption = getPriceCaption(paymentState),
                            icon = R.drawable.ic_payment,
                            onClick = {
                                startActivity(Intent(requireContext(), PaymentActivity::class.java))
                            }
                        )
                    )
                }
                PaymentState.DontShow -> {}
            }
            add(ProfileModel.Subtitle)
            add(
                ProfileModel.Row(
                    title = getString(R.string.profile_appSettingsSection_row_headline),
                    caption = getString(R.string.profile_appSettingsSection_row_subheadline),
                    icon = R.drawable.ic_profile_settings,
                    onClick = {
                        startActivity(SettingsActivity.newInstance(requireContext()))
                    }
                )
            )
            add(
                ProfileModel.Row(
                    title = getString(R.string.PROFILE_ABOUT_ROW),
                    caption = getString(R.string.profile_tab_about_row_subtitle),
                    icon = R.drawable.ic_info_toolbar,
                    onClick = {
                        startActivity(Intent(requireContext(), AboutAppActivity::class.java))
                    }
                )
            )
            add(ProfileModel.Logout)
        }
    }

    private fun buildCharityRowItem(charityName: String? = null): ProfileModel.Row {
        return ProfileModel.Row(
            title = getString(R.string.PROFILE_MY_CHARITY_ROW_TITLE),
            caption = charityName,
            icon = R.drawable.ic_profile_charity,
            onClick = {
                startActivity(Intent(requireContext(), CharityActivity::class.java))
            }
        )
    }

    private fun getPriceCaption(paymentState: PaymentState.Show): String {
        paymentState.priceCaptionResId ?: return ""
        return getString(paymentState.priceCaptionResId, paymentState.monetaryMonthlyNet)
    }
}
