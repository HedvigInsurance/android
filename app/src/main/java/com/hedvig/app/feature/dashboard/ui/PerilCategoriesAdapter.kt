package com.hedvig.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.compatSetTint
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.extensions.view.animateCollapse
import com.hedvig.app.util.extensions.view.animateExpand
import com.hedvig.app.util.extensions.view.setHapticClickListener
import kotlinx.android.synthetic.main.peril_category.view.*

class PerilCategoriesAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<PerilCategoriesAdapter.PerilCategoryHolder>() {
    var perilCategories: List<PerilCategory> = emptyList()
        set(value) {
            val callback = PerilCategoriesDiffUtilCallback(field, value)
            val diff = DiffUtil.calculateDiff(callback)
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PerilCategoryHolder(parent, fragmentManager)
    override fun getItemCount() = perilCategories.size
    override fun onBindViewHolder(holder: PerilCategoryHolder, position: Int) {
        holder.bind(perilCategories[position])
    }

    class PerilCategoryHolder(parent: ViewGroup, fragmentManager: FragmentManager) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.peril_category, parent, false)
    ) {
        private val icon: ImageView = itemView.catIcon
        private val title: TextView = itemView.categoryTitle
        private val subtitle: TextView = itemView.categorySubtitle
        private val perils: RecyclerView = itemView.perilsContainer
        private val expandCollapse: ImageView = itemView.expandCollapse

        init {
            perils.adapter = PerilsAdapter(fragmentManager)
        }

        fun bind(perilCategory: PerilCategory) {
            // TODO: Bind icon for real
            icon.setImageDrawable(icon.context.compatDrawable(R.drawable.ic_family)?.apply {
                if (icon.context.isDarkThemeActive) {
                    compatSetTint(icon.context.compatColor(R.color.icon_tint))
                }
            })
            title.text = perilCategory.title
            subtitle.text = perilCategory.subtitle
            (perils.adapter as? PerilsAdapter)?.setData("TODO", perilCategory.perils) // TODO: Find where the hell we source the subject
            itemView.setHapticClickListener {
                val isExpanded = perils.height != 0
                if (isExpanded) {
                    perils.animateCollapse(withOpacity = true)
                    expandCollapse
                        .animate()
                        .withLayer()
                        .setDuration(200)
                        .setInterpolator(DecelerateInterpolator())
                        .rotation(0f)
                        .start()
                } else {
                    perils.updateLayoutParams { height = 1 }
                    perils.animateExpand(withOpacity = true)
                    expandCollapse
                        .animate()
                        .withLayer()
                        .setDuration(200)
                        .setInterpolator(DecelerateInterpolator())
                        .rotation(-180f)
                        .start()
                }
            }
        }
    }
}

class PerilCategoriesDiffUtilCallback(
    private val old: List<PerilCategory>,
    private val new: List<PerilCategory>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition].id == new[newItemPosition].id
    override fun getOldListSize() = old.size
    override fun getNewListSize() = new.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = old[oldItemPosition] == new[newItemPosition]
}

