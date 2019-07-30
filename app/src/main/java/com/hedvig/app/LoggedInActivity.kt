package com.hedvig.app

import android.content.Context
import android.os.Bundle
import androidx.navigation.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.hedvig.app.util.NavigationAnalytics
import com.ice.restring.Restring
import org.koin.android.ext.android.inject

class LoggedInActivity : BaseActivity() {

    val firebaseAnalytics: FirebaseAnalytics by inject()

    private val navController by lazy { findNavController(R.id.loggedNavigationHost) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logged_in_navigation_host)

        navController.addOnDestinationChangedListener(
            NavigationAnalytics(
                firebaseAnalytics,
                this
            )
        )
    }

    companion object {
        const val EXTRA_IS_FROM_REFERRALS_NOTIFICATION = "extra_is_from_referrals_notification"
        const val EXTRA_IS_FROM_ONBOARDING = "extra_is_from_onboarding"
    }
}
