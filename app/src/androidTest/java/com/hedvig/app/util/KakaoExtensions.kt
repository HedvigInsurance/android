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
import com.agoda.kakao.text.KTextView
import java.time.LocalDate

fun KTextInputLayout.hasError(@StringRes resId: Int) =
    hasError(context().getString(resId))

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
