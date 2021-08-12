package com.hedvig.app.feature.dismissiblepager

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

// Seems to be an IDE bug.
@SuppressLint("WrongConstant")
class DismissiblePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle,
    private val data: List<DismissiblePagerModel>,
) : FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun createFragment(position: Int) =
        when (val page = data[position]) {
            is DismissiblePagerModel.TitlePage -> {
                DismissiblePageTitleFragment.newInstance(page)
            }
            is DismissiblePagerModel.NoTitlePage -> {
                DismissiblePageNoTitleFragment.newInstance(page)
            }
            DismissiblePagerModel.SwipeOffScreen -> {
                Fragment()
            }
        }

    override fun getItemCount() = data.size
}
