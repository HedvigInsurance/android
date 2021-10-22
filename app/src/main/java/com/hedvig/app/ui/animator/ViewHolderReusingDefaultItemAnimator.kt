package com.hedvig.app.ui.animator

import androidx.recyclerview.widget.DefaultItemAnimator

/**
 * Animator which does not let [RecyclerView][androidx.recyclerview.widget.RecyclerView] run the default animation when
 * [areContentsTheSame][androidx.recyclerview.widget.DiffUtil.Callback.areContentsTheSame] returns true. This way the
 * item itself can handle their own animation when
 * [onBindViewHolder][androidx.recyclerview.widget.RecyclerView.Adapter.onBindViewHolder] is called.
 *
 * This allows for example [ViewHolder][androidx.recyclerview.widget.RecyclerView.ViewHolder] items that contain a
 * single [ComposeView][androidx.compose.ui.platform.ComposeView] item to handle its own animation.
 */
class ViewHolderReusingDefaultItemAnimator : DefaultItemAnimator() {
    init {
        supportsChangeAnimations = false
    }
}
