package com.hedvig.app.feature.profile.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.iid.FirebaseInstanceId
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.chat.viewmodel.UserViewModel
import com.hedvig.app.feature.loggedin.ui.BaseTabFragment
import com.hedvig.app.feature.profile.ui.aboutapp.AboutAppActivity
import com.hedvig.app.feature.profile.ui.charity.CharityActivity
import com.hedvig.app.feature.profile.ui.feedback.FeedbackActivity
import com.hedvig.app.feature.profile.ui.myinfo.MyInfoActivity
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.service.LoginStatusService.Companion.IS_VIEWING_OFFER
import com.hedvig.app.util.extensions.observe
import com.hedvig.app.util.extensions.setAuthenticationToken
import com.hedvig.app.util.extensions.setIsLoggedIn
import com.hedvig.app.util.extensions.storeBoolean
import com.hedvig.app.util.extensions.triggerRestartActivity
import com.hedvig.app.util.extensions.view.remove
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.view.show
import com.hedvig.app.util.extensions.view.updatePadding
import com.hedvig.app.util.interpolateTextKey
import com.hedvig.app.util.safeLet
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.loading_spinner.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class ProfileFragment : BaseTabFragment() {

    private val userViewModel: UserViewModel by sharedViewModel()
    private val profileViewModel: ProfileViewModel by sharedViewModel()

    override val layout = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileRoot.doOnApplyWindowInsets { view, insets, initialState ->
            val toolbar = activity?.findViewById<Toolbar>(R.id.toolbarRoot)
            val navbar = activity?.findViewById<BottomNavigationView>(R.id.bottomTabs)
            safeLet(toolbar, navbar) { toolbar, navbar ->
                view.updatePadding(
                    top = initialState.paddings.top + toolbar.measuredHeight,
                    bottom = initialState.paddings.bottom + navbar.measuredHeight + insets.systemWindowInsetBottom
                )
            }
        }

        populateData()
    }

    override fun onResume() {
        (view as? NestedScrollView)?.scrollTo(0, 0)
        super.onResume()
    }

    private fun populateData() {
        profileViewModel.data.observe(lifecycleOwner = this) { profileData ->
            loadingSpinner.remove()
            rowContainer.show()
            logout.show()

            profileData?.let { data ->
                setupMyInfoRow(data)
                setupCharity(data)
                setupPayment(data)
            }

            feedbackRow.setHapticClickListener {
                startActivity(Intent(requireContext(), FeedbackActivity::class.java))
            }
            aboutAppRow.setHapticClickListener {
                startActivity(Intent(requireActivity(), AboutAppActivity::class.java))
            }
            logout.setHapticClickListener {
                userViewModel.logout {
                    requireContext().storeBoolean(IS_VIEWING_OFFER, false)
                    requireContext().setAuthenticationToken(null)
                    requireContext().setIsLoggedIn(false)
                    FirebaseInstanceId.getInstance().deleteInstanceId()
                    requireActivity().triggerRestartActivity()
                }
            }
        }
    }

    private fun setupMyInfoRow(profileData: ProfileQuery.Data) {
        val firstName = profileData.member.firstName ?: ""
        val lastName = profileData.member.lastName ?: ""
        myInfoRow.description = "$firstName $lastName"
        myInfoRow.setHapticClickListener {
            startActivity(Intent(requireContext(), MyInfoActivity::class.java))
        }
    }

    private fun setupCharity(profileData: ProfileQuery.Data) {
        charityRow.description = profileData.cashback?.fragments?.cashbackFragment?.name
        charityRow.setHapticClickListener {
            startActivity(Intent(requireContext(), CharityActivity::class.java))
        }
    }

    private fun setupPayment(profileData: ProfileQuery.Data) {
        paymentRow.description = interpolateTextKey(
            resources.getString(R.string.PROFILE_ROW_PAYMENT_DESCRIPTION),
            "COST" to profileData.insuranceCost?.fragments?.costFragment?.monthlyNet?.amount?.toBigDecimal()
                ?.toInt()
        )
        paymentRow.setHapticClickListener {
            startActivity(Intent(requireContext(), PaymentActivity::class.java))
        }
    }
}
