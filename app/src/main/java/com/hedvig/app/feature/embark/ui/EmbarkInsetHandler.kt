package com.hedvig.app.feature.embark.ui

import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import androidx.annotation.RequiresApi
import com.hedvig.app.util.ControlFocusInsetsAnimationCallback
import com.hedvig.app.util.RootViewDeferringInsetsCallback
import com.hedvig.app.util.TranslateDeferringInsetsAnimationCallback

class EmbarkInsetHandler {

    companion object {
        @RequiresApi(Build.VERSION_CODES.R)
        fun setupInsetsForIme(root: View, focusableView: View, vararg translatableViews: View) {
            val deferringListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsets.Type.systemBars(),
                deferredInsetTypes = WindowInsets.Type.ime(),
                setPaddingTop = false
            )

            root.setWindowInsetsAnimationCallback(deferringListener)
            root.setOnApplyWindowInsetsListener(deferringListener)

            translatableViews.forEach {
                it.setWindowInsetsAnimationCallback(
                    TranslateDeferringInsetsAnimationCallback(
                        view = it,
                        persistentInsetTypes = WindowInsets.Type.systemBars(),
                        deferredInsetTypes = WindowInsets.Type.ime(),
                        dispatchMode = WindowInsetsAnimation.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
                    )
                )
            }

            focusableView.setWindowInsetsAnimationCallback(
                ControlFocusInsetsAnimationCallback(focusableView)
            )
        }
    }
}
