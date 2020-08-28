package com.hedvig.app.util

import android.view.View
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.Description

class HasNumberOfMenuItemsCheck(private val matcherNumber: Int) : BoundedMatcher<View?, BottomNavigationView>(BottomNavigationView::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("with $matcherNumber number of items")
    }

    override fun matchesSafely(bottomNavigationView: BottomNavigationView): Boolean {
        return matcherNumber == bottomNavigationView.menu.size()
    }
}
