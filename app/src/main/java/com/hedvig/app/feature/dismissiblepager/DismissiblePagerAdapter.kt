package com.hedvig.app.feature.dismissiblepager

import android.annotation.SuppressLint
import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// Seems to be an IDE bug.
@SuppressLint("WrongConstant")
class DismissiblePagerAdapter(
    fragmentManager: FragmentManager,
    private val data: List<DismissiblePagerModel>,
    private val context: Context
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) =
        when (val page = data[position]) {
            is DismissiblePagerModel.TitlePage -> {
                DismissiblePageTitleFragment.newInstance(page)
            }
            is DismissiblePagerModel.NoTitlePage -> {
                DismissiblePageNoTitleFragment.newInstance(page)
            }
            DismissiblePagerModel.SwipeOffScreen -> {
                androidx.fragment.app.Fragment()
            }
        }

    override fun getCount() = data.size
}
