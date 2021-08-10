package com.hedvig.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.databinding.ActivityDevelopmentBinding
import com.hedvig.app.feature.adyen.AdyenMockActivity
import com.hedvig.app.feature.changeaddress.ChangeAddressMockActivity
import com.hedvig.app.feature.chat.ChatMockActivity
import com.hedvig.app.feature.checkout.CheckoutMockActivity
import com.hedvig.app.feature.embark.EmbarkMockActivity
import com.hedvig.app.feature.home.HomeMockActivity
import com.hedvig.app.feature.insurance.InsuranceMockActivity
import com.hedvig.app.feature.loggedin.LoggedInMockActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.marketpicker.MarketPickerMockActivity
import com.hedvig.app.feature.offer.OfferMockActivity
import com.hedvig.app.feature.onboarding.OnboardingMockActivity
import com.hedvig.app.feature.payment.PaymentMockActivity
import com.hedvig.app.feature.profile.ProfileMockActivity
import com.hedvig.app.feature.referrals.ReferralsMockActivity
import com.hedvig.app.feature.tracking.TrackingLogActivity
import com.hedvig.app.feature.trustly.TrustlyMockActivity
import com.hedvig.app.util.extensions.viewBinding
import com.hedvig.app.viewgallery.ViewGalleryActivity
import org.koin.android.ext.android.inject

class DevelopmentActivity : AppCompatActivity(R.layout.activity_development) {
    private val binding by viewBinding(ActivityDevelopmentBinding::bind)
    private val authenticationTokenService: AuthenticationTokenService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.root.adapter = DevelopmentScreenAdapter(authenticationTokenService).also {
            it.submitList(
                listOf(
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Header,
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Tracking") {
                        startActivity(TrackingLogActivity.newInstance(this))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Change address intro") {
                        startActivity(Intent(this, ChangeAddressMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Logged in without welcome-screen") {
                        startActivity(LoggedInActivity.newInstance(this))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Embark") {
                        startActivity(Intent(this, EmbarkMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Onboarding") {
                        startActivity(Intent(this, OnboardingMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Referrals") {
                        startActivity(Intent(this, ReferralsMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Payment") {
                        startActivity(Intent(this, PaymentMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Chat") {
                        startActivity(Intent(this, ChatMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Offer") {
                        startActivity(Intent(this, OfferMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Checkout") {
                        startActivity(Intent(this, CheckoutMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Home") {
                        startActivity(Intent(this, HomeMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Insurance") {
                        startActivity(Intent(this, InsuranceMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Profile") {
                        startActivity(Intent(this, ProfileMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Market Picker") {
                        startActivity(Intent(this, MarketPickerMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Logged in") {
                        startActivity(Intent(this, LoggedInMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Adyen") {
                        startActivity(Intent(this, AdyenMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Trustly") {
                        startActivity(Intent(this, TrustlyMockActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("`VectorDrawable`-gallery") {
                        startActivity(Intent(this, VectorDrawableGalleryActivity::class.java))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Theme-gallery") {
                        startActivity(ViewGalleryActivity.newInstance(this))
                    },
                    DevelopmentScreenAdapter.DevelopmentScreenItem.Footer
                )
            )
        }
    }
}
