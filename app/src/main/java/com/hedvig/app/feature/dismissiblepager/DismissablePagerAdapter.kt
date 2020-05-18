package com.hedvig.app.feature.dismissiblepager

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class DismissablePagerAdapter(
    fragmentManager: FragmentManager,
    private val data: List<DismissiblePagerPage>,
    private val context: Context
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) = if (position < data.size) {
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

    override fun getCount() = data.size + 1
}
