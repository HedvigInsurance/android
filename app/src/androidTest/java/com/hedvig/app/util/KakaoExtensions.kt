package com.hedvig.app.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.test.core.app.ApplicationProvider
import com.agoda.kakao.edit.KTextInputLayout

fun KTextInputLayout.hasError(@StringRes resId: Int) =
    hasError(ApplicationProvider.getApplicationContext<Context>().getString(resId))
