package com.hedvig.app.feature.offer.ui

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.R
import com.hedvig.app.databinding.PerilDetailBinding
import com.hedvig.app.feature.insurance.ui.detail.coverage.PerilBottomSheet
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class PerilsAdapter(
    private val fragmentManager: FragmentManager,
    private val requestBuilder: RequestBuilder<PictureDrawable>,
) : ListAdapter<PerilFragment, PerilsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), fragmentManager, requestBuilder)
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.peril_detail, parent, false)
    ) {
        private val binding by viewBinding(PerilDetailBinding::bind)

        fun bind(
            peril: PerilFragment,
            fragmentManager: FragmentManager,
            requestBuilder: RequestBuilder<PictureDrawable>,
        ) {
            binding.apply {
                label.text = peril.title
                val iconUrl = "${icon.context.getString(R.string.BASE_URL)}${
                    if (icon.context.isDarkThemeActive) {
                        peril.icon.variants.dark.svgUrl
                    } else {
                        peril.icon.variants.light.svgUrl
                    }
                }"

                requestBuilder
                    .load(iconUrl)
                    .into(icon)

                root.setHapticClickListener {
                    PerilBottomSheet
                        .newInstance(root.context, peril)
                        .show(fragmentManager, PerilBottomSheet.TAG)
                }
            }
        }
    }
}
