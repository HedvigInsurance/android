package com.hedvig.app.util.extensions

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.hedvig.app.util.extensions.view.setupLargeTitle
import kotlinx.android.synthetic.main.app_bar.*

fun Fragment.setupLargeTitle(
    @StringRes title: Int,
    @FontRes font: Int,
    @DrawableRes icon: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    backAction: (() -> Unit)? = null
) {
    setupLargeTitle(getString(title), font, icon, backgroundColor, backAction)
}

fun Fragment.setupLargeTitle(
    title: String,
    @FontRes font: Int,
    @DrawableRes icon: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    backAction: (() -> Unit)? = null
) {
    appBarLayout.setupLargeTitle(
        title,
        font,
        (requireActivity() as AppCompatActivity),
        icon,
        backgroundColor,
        backAction
    )
}

val Fragment.screenWidth: Int
    get() = requireActivity().screenWidth
