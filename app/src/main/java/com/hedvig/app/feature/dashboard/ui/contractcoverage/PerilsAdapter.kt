package com.hedvig.app.feature.dashboard.ui.contractcoverage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.app.R
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.peril_detail.view.*

class PerilsAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<PerilsAdapter.ViewHolder>() {
    var items: List<PerilCategoryFragment.Peril> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(PerilDiffCallback(field, value))
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], fragmentManager)
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.peril_detail, parent, false)
    ) {
        private val root = itemView.root
        private val contents = itemView.contents

        fun bind(peril: PerilCategoryFragment.Peril, fragmentManager: FragmentManager) {
            contents.text = peril.title
            // TODO: Set compound drawable
            root.setHapticClickListener {
                PerilBottomSheet
                    .newInstance(peril)
                    .show(fragmentManager, PerilBottomSheet.TAG)
            }
        }
    }

    class PerilDiffCallback(
        private val old: List<PerilCategoryFragment.Peril>,
        private val new: List<PerilCategoryFragment.Peril>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition] == new[newItemPosition]
        override fun getOldListSize() = old.size
        override fun getNewListSize() = new.size
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = areItemsTheSame(oldItemPosition, newItemPosition)
    }
}
