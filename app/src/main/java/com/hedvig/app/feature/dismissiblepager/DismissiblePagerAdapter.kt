package com.hedvig.app.feature.dismissiblepager

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class DismissiblePagerAdapter(
    fragmentManager: FragmentManager,
    private val data: List<DismissiblePagerModel>,
    private val context: Context
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) =
        when (val page = data[position]) {
            is DismissiblePagerModel.TitlePage -> {
                DismissiblePageFragment.newInstance(
                    illustration = page.imageUrls.iconByTheme(context),
                    title = page.title,
                    paragraph = page.paragraph
                )
            }
            is DismissiblePagerModel.NoTitlePage -> {
                DismissiblePageFragment.newInstance(
                    illustration = page.imageUrls.iconByTheme(context),
                    paragraph = page.paragraph,
                    title = null
                )
            }
            is DismissiblePagerModel.SwipeOffScreen -> {
                androidx.fragment.app.Fragment()
            }
        }

    override fun getCount() = data.size
}
