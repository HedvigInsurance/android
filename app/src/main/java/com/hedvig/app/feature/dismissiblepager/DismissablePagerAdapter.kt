package com.hedvig.app.feature.dismissiblepager

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class DismissablePagerAdapter(
    fragmentManager: FragmentManager,
    private val data: List<DismissiblePagerModel>,
    private val context: Context,
    private val showInvisiblePage: Boolean
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) = if (!showInvisiblePage) {
        when (val page = data[position]) {
            is DismissiblePagerModel.TitlePage -> {
                DismissablePageFragment.newInstance(
                    page.imageUrls.iconByTheme(context),
                    page.title,
                    page.paragraph
                )
            }

        }
        data[position].let { page ->
            DismissablePageFragment.newInstance(
                page.imageUrls.iconByTheme(context),
                page.title,
                page.paragraph
            )
        }
    } else {
        if (position < data.size) {
            data[position].let { page ->
                DismissablePageFragment.newInstance(
                    page.imageUrls.iconByTheme(context),
                    page.title,
                    page.paragraph
                )
            }
        } else {
            androidx.fragment.app.Fragment()
        }
    }

    override fun getCount() = if (showInvisiblePage) {
        data.size + 1
    } else {
        data.size
    }
}
