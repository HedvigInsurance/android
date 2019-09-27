package com.hedvig.app.feature.dismissablepager

import android.content.Context
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.hedvig.app.util.extensions.isDarkThemeActive

class DismissablePagerAdapter(
    fragmentManager: FragmentManager,
    private val data: List<DismissablePagerPage>,
    private val context: Context
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) = if (position < data.size) {
        data[position].let { page ->
            DismissablePageFragment.newInstance(page.imageUrls.iconByTheme(context), page.title, page.paragraph)
        }
    } else {
        androidx.fragment.app.Fragment()
    }

    override fun getCount() = data.size + 1
}
