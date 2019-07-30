package com.hedvig.app.util

import android.app.Activity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.FragmentNavigator
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class NavigationAnalytics(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val activity: Activity
) : NavController.OnDestinationChangedListener {

    private var lastDestinationName: String? = null
    private var lastDestinationClassName: String? = null
    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        val destinationName = destination.label.toString()
        val destinationClassName = (destination as? FragmentNavigator.Destination)?.className

        if (lastDestinationName == destinationName && lastDestinationClassName == destinationClassName) {
            return
        }

        lastDestinationName = destinationName
        lastDestinationClassName = destinationClassName

        Timber.i("Screen View: %s", destinationName)
        firebaseAnalytics.setCurrentScreen(
            activity,
            destinationName,
            destinationClassName
        )
    }
}
