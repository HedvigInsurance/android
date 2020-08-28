package com.hedvig.app.feature.loggedin.ui

import androidx.recyclerview.widget.RecyclerView

class ScrollPositionListener(
    private val onScroll: (Int) -> Unit
) : RecyclerView.OnScrollListener() {
    private var scrollY = 0
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        scrollY += dy
        onScroll(scrollY)
    }
}
