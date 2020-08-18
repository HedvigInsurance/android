package com.hedvig.app.util

import android.content.Context
import android.view.View
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import com.agoda.kakao.bottomnav.KBottomNavigationView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.picker.date.KDatePicker
import com.agoda.kakao.text.KTextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.hamcrest.Description
import org.hamcrest.Matcher
import java.time.LocalDate

fun KTextInputLayout.hasError(@StringRes resId: Int) =
    hasError(ApplicationProvider.getApplicationContext<Context>().getString(resId))

fun KTextInputLayout.hasError(@StringRes resId: Int, vararg formatArgs: Any) =
    hasError(ApplicationProvider.getApplicationContext<Context>().getString(resId, *formatArgs))

fun KDatePicker.setDate(date: LocalDate) = setDate(date.year, date.monthValue, date.dayOfMonth)

fun KTextView.hasText(@StringRes resId: Int, vararg formatArgs: Any) =
    hasText(ApplicationProvider.getApplicationContext<Context>().getString(resId, *formatArgs))

fun KBottomNavigationView.hasNumberOfMenuItems(matcherNumber: Int) {
    view.check(
        ViewAssertions.matches(hasNumberOfMenuItemsCheck(matcherNumber)))
}

private fun KBottomNavigationView.hasNumberOfMenuItemsCheck(matcherNumber: Int): Matcher<View?>? {

    return object : BoundedMatcher<View?, BottomNavigationView>(BottomNavigationView::class.java) {
        override fun describeTo(description: Description) {
            description.appendText("with $matcherNumber number of items")
        }

        override fun matchesSafely(bottomNavigationView: BottomNavigationView): Boolean {
            return matcherNumber == bottomNavigationView.menu.size()
        }
    }
}
