package com.hedvig.app.feature.claims.ui.commonclaim.bulletpoint

import android.net.Uri
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import com.hedvig.app.R
import com.hedvig.app.databinding.ClaimBulletpointRowBinding
import com.hedvig.app.feature.claims.ui.commonclaim.BulletPoint
import com.hedvig.app.util.GenericDiffUtilItemCallback
import com.hedvig.app.util.extensions.inflate
import com.hedvig.app.util.extensions.viewBinding

class BulletPointsAdapter(
    private val baseUrl: String,
    private val imageLoader: ImageLoader
) : ListAdapter<BulletPoint, BulletPointsAdapter.ViewHolder>(GenericDiffUtilItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind(getItem(position), baseUrl, imageLoader)
    }

    class ViewHolder(parent: ViewGroup) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.claim_bulletpoint_row)) {
        private val binding by viewBinding(ClaimBulletpointRowBinding::bind)
        fun bind(
            item: BulletPoint,
            baseUrl: String,
            imageLoader: ImageLoader
        ) {
            binding.apply {
                bulletPointIcon.load(
                    Uri.parse(baseUrl + item.iconUrls.iconByTheme(bulletPointIcon.context)),
                    imageLoader
                )
                bulletPointTitle.text = item.title
                bulletPointDescription.text = item.description
            }
        }
    }
}
