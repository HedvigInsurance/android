package com.hedvig.app.feature.chat.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.clear
import coil.load
import com.hedvig.android.owldroid.graphql.GifQuery
import com.hedvig.app.R
import com.hedvig.app.databinding.GifItemBinding
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.view.setHapticClickListener
import com.hedvig.app.util.extensions.viewBinding

class GifAdapter(
    private val imageLoader: ImageLoader,
    private val sendGif: (String) -> Unit
) : ListAdapter<GifQuery.Gif, GifAdapter.GifViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = GifViewHolder(
        imageLoader,
        LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.gif_item,
                parent,
                false
            )
    )

    override fun onBindViewHolder(holder: GifViewHolder, position: Int) {
        holder.bind(getItem(position), sendGif)
    }

    override fun onViewRecycled(holder: GifViewHolder) {
        holder.binding.gifImage.clear()
    }

    class GifViewHolder(
        private val imageLoader: ImageLoader,
        view: View
    ) : RecyclerView.ViewHolder(view) {
        val binding by viewBinding(GifItemBinding::bind)
        fun bind(item: GifQuery.Gif, sendGif: (String) -> Unit) {
            binding.apply {
                item.url?.let { url ->
                    gifImage.load(Uri.parse(url), imageLoader)
                    gifImage.setHapticClickListener {
                        sendGif(url)
                    }
                }
            }
        }
    }
}
