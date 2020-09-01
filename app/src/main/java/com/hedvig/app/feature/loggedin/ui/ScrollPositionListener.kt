package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class ScrollPositionListener(
    private val onScroll: (Int) -> Unit,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.OnScrollListener() {
    private var scrollY = 0
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        scrollY += dy
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            onScroll(scrollY)
        }
    }
}
