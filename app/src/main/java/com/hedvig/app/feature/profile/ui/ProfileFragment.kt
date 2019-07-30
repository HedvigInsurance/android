package com.hedvig.app.feature.profile.ui

import androidx.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.chat.UserViewModel
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppActivity
import com.hedvig.app.util.extensions.*
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.interpolateTextKey
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ProfileFragment : BaseTabFragment() {

    private val userViewModel: UserViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    override val layout = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateData()
        loadReferralFeature()
    }

    private fun loadReferralFeature() {
        profileViewModel.remoteConfigData.observe(this) { remoteConfigData ->
            remoteConfigData?.let { rcd ->
                if (!rcd.referralsEnabled || rcd.newReferralsEnabled) {
                    return@let
                }

                profileReferralRow.setHighlighted()
                profileReferralRow.name = interpolateTextKey(
                    resources.getString(R.string.PROFILE_ROW_REFERRAL_TITLE),
                    "INCENTIVE" to rcd.referralsIncentiveAmount.toString()
                )
                profileReferralRow.setOnClickListener {
                    navController.proxyNavigate(R.id.action_loggedInFragment_to_referralFragment)
                }
                profileReferralRow.show()
            }
        }
    }

    private fun populateData() {
        profileViewModel.data.observe(this, Observer { profileData ->
            loadingSpinner.remove()
            rowContainer.show()
            logout.show()

            profileData?.let { data ->
                setupMyInfoRow(data)
                setupMyHomeRow(data)
                setupCoinsured(data)
                setupCharity(data)
                setupPayment(data)
                setupCertificateUrl(data)
            }

            feedbackRow.setOnClickListener {
                navController.proxyNavigate(R.id.action_loggedInFragment_to_feedbackFragment)
            }
            aboutAppRow.setOnClickListener {
                startActivity(Intent(requireActivity(), AboutAppActivity::class.java))
            }
            logout.setOnClickListener {
                userViewModel.logout {
                    requireContext().setAuthenticationToken(null)
                    requireContext().setIsLoggedIn(false)
                    FirebaseInstanceId.getInstance().deleteInstanceId()
                    requireActivity().triggerRestartActivity()
                }
            }
        })
    }

    private fun setupMyInfoRow(profileData: ProfileQuery.Data) {
        val firstName = profileData.member.firstName ?: ""
        val lastName = profileData.member.lastName ?: ""
        myInfoRow.description = "$firstName $lastName"
        myInfoRow.setOnClickListener {
            navController.proxyNavigate(R.id.action_loggedInFragment_to_myInfoFragment)
        }
    }

    private fun setupMyHomeRow(profileData: ProfileQuery.Data) {
        myHomeRow.description = profileData.insurance.address
        myHomeRow.setOnClickListener {
            navController.proxyNavigate(R.id.action_loggedInFragment_to_myHomeFragment)
        }
    }

    private fun setupCoinsured(profileData: ProfileQuery.Data) {
        val personsInHousehold = profileData.insurance.personsInHousehold ?: 1

        if (personsInHousehold <= 1) {
            return
        }


        coinsuredRow.description = interpolateTextKey(
            resources.getString(R.string.PROFILE_MY_COINSURED_ROW_SUBTITLE),
            "amountCoinsured" to "${personsInHousehold - 1}"
        )
        coinsuredRow.setOnClickListener {
            navController.proxyNavigate(R.id.action_loggedInFragment_to_coinsuredFragment)
        }
        coinsuredRow.show()
    }

    private fun setupCharity(profileData: ProfileQuery.Data) {
        charityRow.description = profileData.cashback?.name
        charityRow.setOnClickListener {
            navController.proxyNavigate(R.id.action_loggedInFragment_to_charityFragment)
        }
    }

    private fun setupPayment(profileData: ProfileQuery.Data) {
        paymentRow.description = interpolateTextKey(
            resources.getString(R.string.PROFILE_ROW_PAYMENT_DESCRIPTION),
            "COST" to profileData.insurance.cost?.fragments?.costFragment?.monthlyNet?.amount?.toBigDecimal()?.toInt()
        )
        paymentRow.setOnClickListener {
            navController.proxyNavigate(R.id.action_loggedInFragment_to_paymentFragment)
        }
    }

    private fun setupCertificateUrl(profileData: ProfileQuery.Data) {
        profileData.insurance.certificateUrl?.let { policyUrl ->
            insuranceCertificateRow.show()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl))
            insuranceCertificateRow.setOnClickListener {
                startActivity(intent)
            }
        }
    }
}
