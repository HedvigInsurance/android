package com.hedvig.app.feature.offer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.app.R
import com.hedvig.app.feature.dashboard.ui.PerilBottomSheet
import com.hedvig.app.feature.dashboard.ui.PerilIcon
import com.hedvig.app.ui.view.HedvigCardView
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.safeLet
import kotlinx.android.synthetic.main.peril_card.view.*

class PerilAdapter(
    private val category: PerilCategoryFragment,
    private val tracker: OfferTracker,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<PerilAdapter.PerilViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PerilViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(R.layout.peril_card, parent, false)
    )

    override fun getItemCount() = category.perils?.size ?: 0

    override fun onBindViewHolder(holder: PerilViewHolder, position: Int) {
        category.perils?.getOrNull(position)?.let { data ->
            holder.peril.apply {
                text = data.title?.replace("-", "")?.replace("\n", "")
                data.id?.let { id ->
                    val drawable = context.compatDrawable(PerilIcon.from(id))
                    setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        drawable,
                        null,
                        null
                    )
                }
            }
            holder.card.setHapticClickListener {
                safeLet(
                    category.title,
                    data.id,
                    data.title,
                    data.description
                ) { name, id, title, description ->
                    tracker.openPeril(id)
                    PerilBottomSheet.newInstance(name, PerilIcon.from(id), title, description)
                        .show(fragmentManager, PerilBottomSheet.TAG)
                }
            }
        }
    }

    class PerilViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val peril: TextView = view.peril
        val card: HedvigCardView = view.perilCard
    }
}
