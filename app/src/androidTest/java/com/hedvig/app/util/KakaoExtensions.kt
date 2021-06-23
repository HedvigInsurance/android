package com.hedvig.app.util

import android.app.Activity
import android.app.Instrumentation
import android.view.View
import android.widget.TextView
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.agoda.kakao.bottomnav.KBottomNavigationView
import com.agoda.kakao.common.builders.ViewBuilder
import com.agoda.kakao.common.utilities.getResourceString
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.intent.KIntent
import com.agoda.kakao.picker.date.KDatePicker
import com.agoda.kakao.swiperefresh.KSwipeRefreshLayout
import com.agoda.kakao.text.KTextView
import com.google.android.material.textfield.TextInputLayout
import java.time.LocalDate
import org.hamcrest.Description

fun KTextInputLayout.hasError(@StringRes resId: Int, vararg formatArgs: Any) =
    hasError(context().getString(resId, *formatArgs))

fun KDatePicker.setDate(date: LocalDate) = setDate(date.year, date.monthValue, date.dayOfMonth)

fun KTextView.hasText(@StringRes resId: Int, vararg formatArgs: Any) =
    view.check { view, noViewFoundException ->
        if (view is TextView) {
            val text = view.resources.getString(resId, *formatArgs)
            if (text != view.text) {
                throw AssertionError("Expected view with text: $text, but actual is ${view.text}")
            }
        } else {
            noViewFoundException?.let { throw AssertionError(it) }
        }
    }

fun KTextView.hasPluralText(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any) =
    hasText(context().resources.getQuantityString(resId, quantity, *formatArgs))

fun KView.hasNrOfChildren(quantity: Int) {
    view.check(ViewAssertions.matches(ViewMatchers.hasChildCount(quantity)))
}

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
                throw AssertionError(
                    "Expected placeholder text is $text," +
                        " but actual is ${view.placeholderText}"
                )
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
                throw AssertionError(
                    "Expected helper text is $text," +
                        " but actual is ${view.helperText}"
                )
            }
        } else {
            noViewFoundException?.let { throw AssertionError(it) }
        }
    }
}

fun KTextInputLayout.hasHelperText(@StringRes resId: Int) = hasHelperText(getResourceString(resId))

fun ViewBuilder.withHint(hint: String) = withMatcher(ViewMatchers.withHint(hint))

class TextInputLayoutPlaceholderMatcher(
    private val placeholder: String,
) : BoundedMatcher<View, TextInputLayout>(TextInputLayout::class.java) {
    override fun describeTo(description: Description) {
        description.appendText("with placeholder: $placeholder")
    }

    override fun matchesSafely(item: TextInputLayout) = item.placeholderText == placeholder
}

fun ViewBuilder.withPlaceholder(placeholder: String) = withMatcher(TextInputLayoutPlaceholderMatcher(placeholder))
