package com.hedvig.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.app.R
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.peril.view.*

class PerilsAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<PerilsAdapter.PerilViewHolder>() {
    fun setData(subject: String, items: List<PerilCategoryFragment.Peril>) {
        val diff =
            DiffUtil.calculateDiff(PerilDiffCallback(this.subject, this.items, subject, items))
        this.subject = subject
        this.items = items

        diff.dispatchUpdatesTo(this)
    }

    private var items: List<PerilCategoryFragment.Peril> = emptyList()
    private var subject: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PerilViewHolder(parent)
    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PerilViewHolder, position: Int) {
        holder.bind(subject, items[position], fragmentManager)
    }

    class PerilViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.peril, parent, false)
    ) {
        val icon: ImageView = itemView.image
        val text: TextView = itemView.text

        fun bind(
            subject: String,
            peril: PerilCategoryFragment.Peril,
            fragmentManager: FragmentManager
        ) {
            safeLet(peril.id, peril.title, peril.description) { id, title, description ->
                val iconId = PerilIcon.from(id)
                icon.setImageDrawable(icon.context.compatDrawable(iconId))
                text.text = title
                itemView.setHapticClickListener {
                    PerilBottomSheet
                        .newInstance(
                            subject,
                            iconId,
                            title,
                            description
                        )
                        .show(fragmentManager, PerilBottomSheet.TAG)
                }
            }
        }
    }
}

class PerilDiffCallback(
    private val oldSubject: String,
    private val oldItems: List<PerilCategoryFragment.Peril>,
    private val newSubject: String,
    private val newItems: List<PerilCategoryFragment.Peril>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldItems[oldItemPosition].id == newItems[newItemPosition].id

    override fun getOldListSize() = oldItems.size
    override fun getNewListSize() = newItems.size
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldSubject == newSubject && oldItems[oldItemPosition] == newItems[newItemPosition]
}
