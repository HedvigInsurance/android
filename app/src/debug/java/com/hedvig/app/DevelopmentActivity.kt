package com.hedvig.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.feature.chat.ChatMockActivity
import com.hedvig.app.feature.home.HomeMockActivity
import com.hedvig.app.feature.insurance.InsuranceMockActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.offer.OfferMockActivity
import com.hedvig.app.feature.profile.ProfileMockActivity
import com.hedvig.app.feature.referrals.ReferralsMockActivity
import com.hedvig.app.viewgallery.ViewGalleryActivity
import kotlinx.android.synthetic.debug.activity_development.*

class DevelopmentActivity : AppCompatActivity(R.layout.activity_development) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root.adapter = DevelopmentScreenAdapter(
            listOf(
                DevelopmentScreenAdapter.DevelopmentScreenItem.Header,
                DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Logged in without welcome-screen") {
                    startActivity(
                        LoggedInActivity.newInstance(this)
                    )
                },
                DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Logged in with welcome-screen") {
                    startActivity(
                        LoggedInActivity.newInstance(this)
                            .putExtra(LoggedInActivity.EXTRA_IS_FROM_ONBOARDING, true)
                    )
                },
                DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Referrals") {
                    startActivity(Intent(this, ReferralsMockActivity::class.java))
                },
                DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Chat") {
                    startActivity(Intent(this, ChatMockActivity::class.java))
                },
                DevelopmentScreenAdapter.DevelopmentScreenItem.Row("Offer") {
                    startActivity(Intent(this, OfferMockActivity::class.java))
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
