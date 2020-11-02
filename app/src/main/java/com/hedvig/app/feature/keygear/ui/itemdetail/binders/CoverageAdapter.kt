package com.hedvig.app.feature.keygear.ui.itemdetail.binders

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.KeyGearItemDetailCoverageRowBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class CoverageAdapter(
    private val isExceptions: Boolean
) : ListAdapter<String, CoverageAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), isExceptions)
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        parent.inflate(R.layout.key_gear_item_detail_coverage_row)
    ) {
        private val binding by viewBinding(KeyGearItemDetailCoverageRowBinding::bind)
        fun bind(item: String, isExceptions: Boolean) {
            binding.apply {
                label.text = item
                label.setCompoundDrawablesWithIntrinsicBounds(
                    label.context.compatDrawable(
                        if (isExceptions) {
                            R.drawable.ic_cross
                        } else {
                            R.drawable.ic_filled_checkmark_small
                        }
                    ), null, null, null
                )
            }
        }
    }
}
