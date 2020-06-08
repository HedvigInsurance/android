package com.hedvig.app.feature.referrals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R

class ReferralsAdapter : RecyclerView.Adapter<ReferralsAdapter.ViewHolder>() {
    var items: List<ReferralsModel> = listOf(
        ReferralsModel.Header.LoadingHeader,
        ReferralsModel.LoadingReferral
    )

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ReferralsModel.Header -> R.layout.referrals_header
        is ReferralsModel.Code -> R.layout.referrals_code
        ReferralsModel.LoadingReferral -> TODO()
        is ReferralsModel.Referee -> TODO()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO()
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.HeaderViewModel -> {
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        class HeaderViewModel(parent: ViewGroup) : ViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.referrals_header, parent, false)
        )
    }
}

sealed class ReferralsModel {
    sealed class Header : ReferralsModel() {
        object LoadingHeader : Header()
        data class LoadedHeader(
            private val todo: Void
        )
    }

    sealed class Code : ReferralsModel() {
        object LoadingCode : Code()
        data class LoadedCode(
            private val todo: Void
        ) : Code()
    }

    object LoadingReferral : ReferralsModel()

    data class Referee(
        private val todo: Void
    ) : ReferralsModel()
}
