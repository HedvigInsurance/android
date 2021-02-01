package com.hedvig.app.util

import android.app.Activity
import android.app.Instrumentation
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.test.espresso.assertion.ViewAssertions
import com.agoda.kakao.bottomnav.KBottomNavigationView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.picker.date.KDatePicker
import com.agoda.kakao.swiperefresh.KSwipeRefreshLayout
import com.agoda.kakao.text.KTextView
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate

fun KTextInputLayout.hasError(@StringRes resId: Int, vararg formatArgs: Any) =
    hasError(context().getString(resId, *formatArgs))

fun KDatePicker.setDate(date: LocalDate) = setDate(date.year, date.monthValue, date.dayOfMonth)

fun KTextView.hasText(@StringRes resId: Int, vararg formatArgs: Any) =
    hasText(context().getString(resId, *formatArgs))

fun KTextView.hasPluralText(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any) =
    hasText(context().resources.getQuantityString(resId, quantity, *formatArgs))

fun KBottomNavigationView.hasNumberOfMenuItems(matcherNumber: Int) {
    view.check(
        ViewAssertions.matches(HasNumberOfMenuItemsCheck(matcherNumber))
    )
}

fun KIntent.stub() {
    intending(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
}

fun KSwipeRefreshLayout.swipeDownInCenter() = view.perform(CustomViewActions.swipeDownInCenter())

fun KTextInputLayout.hasPlaceholderText(text: String) {
    view.check { view, noViewFoundException ->
        if (view is TextInputLayout) {
            if (text != view.placeholderText) {
                throw AssertionError("Expected placeholder text is $text," +
                    " but actual is ${view.placeholderText}")
            }
        } else {
            noViewFoundException?.let { throw AssertionError(it) }
        }
    }
}

fun KTextInputLayout.hasHelperText(text: String) {
    view.check { view, noViewFoundException ->
        if (view is TextInputLayout) {
            if (text != view.helperText) {
                throw AssertionError("Expected helper text is $text," +
                    " but actual is ${view.helperText}")
            }
        } else {
            noViewFoundException?.let { throw AssertionError(it) }
        }
    }
}
