package com.hedvig.app.feature.dashboard.ui.contractcoverage

import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestBuilder
import com.hedvig.android.owldroid.fragment.PerilFragment
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.peril_detail.view.*

class PerilsAdapter(
    private val fragmentManager: FragmentManager,
    private val requestBuilder: RequestBuilder<PictureDrawable>
) : RecyclerView.Adapter<PerilsAdapter.ViewHolder>() {
    var items: List<PerilFragment> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(PerilDiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], fragmentManager, requestBuilder)
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.peril_detail, parent, false)
    ) {
        private val root = itemView.root
        private val label = itemView.label
        private val icon = itemView.icon

        fun bind(
            peril: PerilFragment,
            fragmentManager: FragmentManager,
            requestBuilder: RequestBuilder<PictureDrawable>
        ) {
            label.text = peril.title
            val iconUrl = "${BuildConfig.BASE_URL}${if (icon.context.isDarkThemeActive) {
                peril.icon.variants.dark.svgUrl
            } else {
                peril.icon.variants.light.svgUrl
            }}"

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

    class PerilDiffCallback(
        private val old: List<PerilFragment>,
        private val new: List<PerilFragment>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            old[oldItemPosition] == new[newItemPosition]

        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            areItemsTheSame(oldItemPosition, newItemPosition)
    }
}
