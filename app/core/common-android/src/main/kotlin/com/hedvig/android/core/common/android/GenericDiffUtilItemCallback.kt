package com.hedvig.android.core.common.android

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

class GenericDiffUtilItemCallback<T : Any> : DiffUtil.ItemCallback<T>() {
  override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem == newItem

  @SuppressLint("DiffUtilEquals")
  override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem == newItem
}
