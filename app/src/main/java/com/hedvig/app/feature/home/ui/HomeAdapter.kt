package com.hedvig.app.feature.home.ui

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.databinding.HomeBigTextBinding
import com.hedvig.app.databinding.HomeStartClaimOutlinedBinding
import com.hedvig.app.feature.claims.ui.pledge.HonestyPledgeBottomSheet
import com.hedvig.app.util.GenericDiffUtilCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding
import e
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class HomeAdapter(
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    var items: List<HomeModel> = emptyList()
        set(value) {
            val diff = DiffUtil.calculateDiff(
                GenericDiffUtilCallback(
                    field,
                    value
                )
            )
            field = value
            diff.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        R.layout.home_big_text -> ViewHolder.BigText(parent)
        R.layout.home_start_claim_outlined -> ViewHolder.StartClaimOutlined(parent)
        else -> throw Error("Invalid view type")
    }

    override fun getItemCount() = items.size
    override fun getItemViewType(position: Int) = when (items[position]) {
        is HomeModel.BigText -> R.layout.home_big_text
        HomeModel.StartClaimOutlined -> R.layout.home_start_claim_outlined
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], fragmentManager)
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(data: HomeModel, fragmentManager: FragmentManager): Any?

        fun invalid(data: HomeModel) {
            e { "Invalid data passed to ${this.javaClass.name}::bind - type is ${data.javaClass.name}" }
        }

        class BigText(parent: ViewGroup) : ViewHolder(
            parent.inflate(
                R.layout.home_big_text
            )
        ) {
            private val binding by viewBinding(HomeBigTextBinding::bind)
            private val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            override fun bind(data: HomeModel, fragmentManager: FragmentManager) = with(binding) {
                if (data !is HomeModel.BigText) {
                    return invalid(data)
                }

                when (data) {
                    is HomeModel.BigText.Pending -> {
                        root.text = "${data.name} TODO"
                    }
                    is HomeModel.BigText.ActiveInFuture -> {
                        root.text = "${data.name} ${formatter.format(data.inception)} TODO"
                    }
                    is HomeModel.BigText.Terminated -> {
                        root.text = "${data.name} Terminated TODO"
                    }
                }
            }
        }

        class StartClaimOutlined(parent: ViewGroup) :
            ViewHolder(parent.inflate(R.layout.home_start_claim_outlined)) {
            private val binding by viewBinding(HomeStartClaimOutlinedBinding::bind)
            override fun bind(data: HomeModel, fragmentManager: FragmentManager) = with(binding) {
                if (data != HomeModel.StartClaimOutlined) {
                    return invalid(data)
                }

                root.setHapticClickListener {
                    HonestyPledgeBottomSheet().show(fragmentManager, HonestyPledgeBottomSheet.TAG)
                }
            }
        }
    }
}
