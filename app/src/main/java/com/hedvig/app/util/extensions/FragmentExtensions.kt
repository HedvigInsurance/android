package com.hedvig.app.util.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.util.extensions.view.setupLargeTitle
import kotlinx.android.synthetic.main.app_bar.*

val androidx.fragment.app.Fragment.localBroadcastManager
    get() = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(
        requireContext()
    )

fun androidx.fragment.app.Fragment.setupLargeTitle(
    @StringRes title: Int,
    @FontRes font: Int,
    @DrawableRes icon: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    backAction: (() -> Unit)? = null
) {
    setupLargeTitle(getString(title), font, icon, backgroundColor, backAction)
}

fun androidx.fragment.app.Fragment.setupLargeTitle(
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

fun androidx.fragment.app.Fragment.makeACall(uri: Uri) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = uri
    startActivity(intent)
}

var androidx.fragment.app.Fragment.statusBarColor: Int
    @ColorInt get() = requireActivity().window.statusBarColor
    set(@ColorInt value) {
        requireActivity().window.statusBarColor = value
    }

val androidx.fragment.app.Fragment.screenWidth: Int
    get() = requireActivity().screenWidth
