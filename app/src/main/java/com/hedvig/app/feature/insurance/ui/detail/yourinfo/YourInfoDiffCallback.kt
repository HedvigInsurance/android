package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import androidx.recyclerview.widget.DiffUtil

class YourInfoDiffCallback : DiffUtil.ItemCallback<YourInfoModel>() {
    override fun areItemsTheSame(oldItem: YourInfoModel, newItem: YourInfoModel) =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: YourInfoModel, newItem: YourInfoModel) =
        oldItem == newItem
}
